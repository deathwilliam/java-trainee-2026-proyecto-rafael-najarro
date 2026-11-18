package sv.sipas.api.servicio;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.sipas.api.dto.ResumenSesionCajaRespuesta;
import sv.sipas.api.dto.SesionCajaRespuesta;
import sv.sipas.api.entidad.BitacoraOperacion;
import sv.sipas.api.entidad.Caja;
import sv.sipas.api.entidad.Empleado;
import sv.sipas.api.entidad.SesionCaja;
import sv.sipas.api.entidad.enums.EstadoSesionCaja;
import sv.sipas.api.entidad.enums.ResultadoOperacion;
import sv.sipas.api.entidad.enums.TipoMovimientoCaja;
import sv.sipas.api.entidad.enums.TipoOperacion;
import sv.sipas.api.excepcion.AccesoDenegadoException;
import sv.sipas.api.excepcion.OperacionRechazadaException;
import sv.sipas.api.excepcion.RecursoNoEncontradoException;
import sv.sipas.api.repositorio.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SesionCajaServicio {

    private final CajaRepositorio cajaRepositorio;
    private final EmpleadoRepositorio empleadoRepositorio;
    private final SesionCajaRepositorio sesionCajaRepositorio;
    private final MovimientoCajaRepositorio movimientoCajaRepositorio;
    private final UsuarioRolRepositorio usuarioRolRepositorio;
    private final BitacoraOperacionRepositorio bitacoraOperacionRepositorio;

    public SesionCajaServicio(CajaRepositorio cajaRepositorio,
                              EmpleadoRepositorio empleadoRepositorio,
                              SesionCajaRepositorio sesionCajaRepositorio,
                              MovimientoCajaRepositorio movimientoCajaRepositorio,
                              UsuarioRolRepositorio usuarioRolRepositorio,
                              BitacoraOperacionRepositorio bitacoraOperacionRepositorio) {
        this.cajaRepositorio = cajaRepositorio;
        this.empleadoRepositorio = empleadoRepositorio;
        this.sesionCajaRepositorio = sesionCajaRepositorio;
        this.movimientoCajaRepositorio = movimientoCajaRepositorio;
        this.usuarioRolRepositorio = usuarioRolRepositorio;
        this.bitacoraOperacionRepositorio = bitacoraOperacionRepositorio;
    }

    @Transactional
    public Long abrirCaja(Long cajaId, Long usuarioId, BigDecimal fondoInicial) {
        if (cajaId == null || cajaId <= 0) {
            throw new IllegalArgumentException("El identificador de la caja debe ser mayor que cero.");
        }
        if (usuarioId == null || usuarioId <= 0) {
            throw new IllegalArgumentException("El identificador del usuario debe ser mayor que cero.");
        }
        if (fondoInicial == null || fondoInicial.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El fondo inicial debe ser mayor o igual que cero.");
        }

        Caja caja = cajaRepositorio.findById(cajaId)
                .orElseThrow(() -> new OperacionRechazadaException("La caja no existe."));
        Empleado empleado = empleadoRepositorio.findByUsuario_Id(usuarioId)
                .orElseThrow(() -> new OperacionRechazadaException("El empleado no existe."));

        if (!empleado.getActivo()
                || !empleado.getUsuario().getActivo()
                || !caja.getActivo()
                || !caja.getSucursal().getActivo()) {
            throw new OperacionRechazadaException("El empleado o la caja no están habilitados.");
        }

        if (!empleado.getSucursal().getId().equals(caja.getSucursal().getId())) {
            throw new OperacionRechazadaException("El empleado no pertenece a la sucursal de la caja.");
        }

        if (sesionCajaRepositorio.existsByCaja_IdAndEstado(cajaId, EstadoSesionCaja.ABIERTA)) {
            throw new OperacionRechazadaException("La caja ya tiene una sesión abierta.");
        }

        if (sesionCajaRepositorio.existsByEmpleado_IdAndEstado(empleado.getId(), EstadoSesionCaja.ABIERTA)) {
            throw new OperacionRechazadaException("El empleado ya tiene una sesión abierta.");
        }

        SesionCaja sesion = sesionCajaRepositorio.save(SesionCaja.abrir(caja, empleado, fondoInicial));

        bitacoraOperacionRepositorio.save(
                BitacoraOperacion.registrar(
                        empleado.getUsuario(),
                        TipoOperacion.APERTURA_CAJA,
                        "sesion_caja",
                        sesion.getId(),
                        "Apertura de sesión de caja",
                        ResultadoOperacion.EXITOSO));

        return sesion.getId();
    }

    @Transactional
    public ResumenSesionCajaRespuesta cerrarCaja(Long sesionCajaId, BigDecimal efectivoContado, Long usuarioId) {
        if (sesionCajaId == null || sesionCajaId <= 0) {
            throw new IllegalArgumentException("El identificador de la sesión de caja debe ser mayor que cero.");
        }
        if (efectivoContado == null || efectivoContado.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El efectivo contado debe ser mayor o igual que cero.");
        }
        if (usuarioId == null || usuarioId <= 0) {
            throw new IllegalArgumentException("El identificador del usuario debe ser mayor que cero.");
        }

        SesionCaja sesion = sesionCajaRepositorio.buscarParaOperacion(sesionCajaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("La sesión de caja no existe."));

        if (!sesion.getEmpleado().getUsuario().getId().equals(usuarioId)) {
            throw new AccesoDenegadoException("No tiene permiso para cerrar esta sesión de caja.");
        }
        if (!sesion.estaAbierta()) {
            throw new OperacionRechazadaException("La sesión de caja ya está cerrada.");
        }

        sesion.cerrar(efectivoContado, LocalDateTime.now());
        sesionCajaRepositorio.save(sesion);

        bitacoraOperacionRepositorio.save(
                BitacoraOperacion.registrar(
                        sesion.getEmpleado().getUsuario(),
                        TipoOperacion.CIERRE_CAJA,
                        "sesion_caja",
                        sesion.getId(),
                        "Cierre de sesión de caja",
                        ResultadoOperacion.EXITOSO));

        return consultarResumen(sesionCajaId, usuarioId);
    }

    @Transactional(readOnly = true)
    public ResumenSesionCajaRespuesta consultarResumen(Long sesionCajaId, Long usuarioId) {
        if (sesionCajaId == null || sesionCajaId <= 0) {
            throw new IllegalArgumentException("El identificador de la sesión de caja debe ser mayor que cero.");
        }
        if (usuarioId == null || usuarioId <= 0) {
            throw new IllegalArgumentException("El identificador del usuario debe ser mayor que cero.");
        }

        SesionCaja sesion = sesionCajaRepositorio.findById(sesionCajaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("La sesión de caja no existe."));
        validarAccesoSesion(sesion, usuarioId);

        BigDecimal totalIngresos = movimientoCajaRepositorio.sumarPorSesionYTipo(sesionCajaId, TipoMovimientoCaja.INGRESO_COBRO);
        BigDecimal totalRetiros = movimientoCajaRepositorio.sumarPorSesionYTipo(sesionCajaId, TipoMovimientoCaja.RETIRO);
        BigDecimal saldoEsperado = sesion.getFondoInicial().add(totalIngresos).subtract(totalRetiros);
        BigDecimal diferencia = sesion.estaAbierta() ? null : sesion.getEfectivoContado().subtract(saldoEsperado);

        return ResumenSesionCajaRespuesta.de(
                sesion,
                totalIngresos,
                totalRetiros,
                saldoEsperado,
                diferencia);
    }

    @Transactional(readOnly = true)
    public List<SesionCajaRespuesta> consultarSesionPorEmpleado(Long empleadoId, EstadoSesionCaja estado, Long usuarioId) {

        if (usuarioId == null || usuarioId <= 0) {
            throw new IllegalArgumentException("El identificador del usuario debe ser mayor que cero.");
        }
        if (empleadoId != null && empleadoId <= 0) {
            throw new IllegalArgumentException("El identificador del empleado debe ser mayor que cero.");
        }

        boolean esAdmin = usuarioRolRepositorio.existsByUsuario_IdAndRol_CodigoAndRol_ActivoTrue(usuarioId, "ADMIN");
        boolean esAsesor = usuarioRolRepositorio.existsByUsuario_IdAndRol_CodigoAndRol_ActivoTrue(usuarioId, "ASESOR");
        Long empleadoIdConsulta = empleadoId;

        if (esAdmin) {
            if (empleadoId != null && !empleadoRepositorio.existsById(empleadoId)) {
                throw new RecursoNoEncontradoException("El empleado no existe.");
            }
        } else if (esAsesor) {
            Empleado empleado = empleadoRepositorio.findByUsuario_Id(usuarioId)
                    .orElseThrow(() -> new AccesoDenegadoException("El usuario no tiene un empleado asociado."));

            if (empleadoId != null && !empleadoId.equals(empleado.getId())) {
                throw new AccesoDenegadoException("No tiene permiso para consultar las sesiones de otro empleado.");
            }

            empleadoIdConsulta = empleado.getId();
        } else {
            throw new AccesoDenegadoException("No tiene permiso para consultar sesiones de caja.");
        }

        List<SesionCaja> sesiones;

        if (empleadoIdConsulta == null) {
            if (estado == null) {
                sesiones = sesionCajaRepositorio.findAll();
            } else {
                sesiones = sesionCajaRepositorio.findByEstado(estado);
            }
        } else {
            if (estado == null) {
                sesiones = sesionCajaRepositorio
                        .findByEmpleado_Id(empleadoIdConsulta);
            } else {
                sesiones = sesionCajaRepositorio
                        .findByEmpleado_IdAndEstado(empleadoIdConsulta, estado);
            }
        }

        return sesiones.stream()
                .map(SesionCajaRespuesta::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public SesionCajaRespuesta consultarSesionPorId(Long sesionCajaId, Long usuarioId) {
        if (sesionCajaId == null || sesionCajaId <= 0) {
            throw new IllegalArgumentException("El identificador de la sesión de caja debe ser mayor que cero.");
        }
        if (usuarioId == null || usuarioId <= 0) {
            throw new IllegalArgumentException("El identificador del usuario debe ser mayor que cero.");
        }

        Optional<SesionCaja> sesionCaja = sesionCajaRepositorio.findById(sesionCajaId);

        if (sesionCaja.isEmpty()) {
            throw new RecursoNoEncontradoException("La sesión de caja no existe.");
        }

        validarAccesoSesion(sesionCaja.get(), usuarioId);
        return SesionCajaRespuesta.de(sesionCaja.get());
    }

    private void validarAccesoSesion(SesionCaja sesionCaja, Long usuarioId) {
        boolean esAdmin = usuarioRolRepositorio.existsByUsuario_IdAndRol_CodigoAndRol_ActivoTrue(usuarioId, "ADMIN");
        boolean esAsesor = usuarioRolRepositorio.existsByUsuario_IdAndRol_CodigoAndRol_ActivoTrue(usuarioId, "ASESOR");

        boolean esPropietario = sesionCaja
                .getEmpleado()
                .getUsuario()
                .getId()
                .equals(usuarioId);

        boolean tienePermiso = esAdmin || (esAsesor && esPropietario);

        if (!tienePermiso) {
            throw new AccesoDenegadoException("No tiene permiso para consultar esta sesión de caja.");
        }
    }

    @Transactional(readOnly = true)
    public void validarAccesoPorSesionId(Long sesionCajaId, Long usuarioId) {
        if (sesionCajaId == null || sesionCajaId <= 0) {
            throw new IllegalArgumentException("El identificador de la sesión de caja debe ser mayor que cero.");
        }
        if (usuarioId == null || usuarioId <= 0) {
            throw new IllegalArgumentException("El identificador del usuario debe ser mayor que cero.");
        }

        Optional<SesionCaja> sesionCaja = sesionCajaRepositorio.findById(sesionCajaId);

        if (sesionCaja.isEmpty()) {
            throw new RecursoNoEncontradoException("La sesión de caja no existe.");
        }

        validarAccesoSesion(sesionCaja.get(), usuarioId);
    }
}
