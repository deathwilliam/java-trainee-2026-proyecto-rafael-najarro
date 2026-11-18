package sv.sipas.api.servicio;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.sipas.api.dto.MovimientoCajaRespuesta;
import sv.sipas.api.dto.RetiroRespuesta;
import sv.sipas.api.entidad.BitacoraOperacion;
import sv.sipas.api.entidad.MovimientoCaja;
import sv.sipas.api.entidad.SesionCaja;
import sv.sipas.api.entidad.Usuario;
import sv.sipas.api.entidad.enums.ResultadoOperacion;
import sv.sipas.api.entidad.enums.TipoMovimientoCaja;
import sv.sipas.api.entidad.enums.TipoOperacion;
import sv.sipas.api.excepcion.OperacionRechazadaException;
import sv.sipas.api.excepcion.RecursoNoEncontradoException;
import sv.sipas.api.repositorio.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static sv.sipas.api.servicio.DatosMantenimiento.*;

@Service
public class MovimientoCajaServicio {

    private final MovimientoCajaRepositorio movimientoCajaRepositorio;
    private final UsuarioRepositorio usuarioRepositorio;
    private final UsuarioRolRepositorio usuarioRolRepositorio;
    private final SesionCajaRepositorio sesionCajaRepositorio;
    private final BitacoraOperacionRepositorio bitacoraOperacionRepositorio;
    private final SesionCajaServicio sesionCajaServicio;

    public MovimientoCajaServicio(MovimientoCajaRepositorio movimientoCajaRepositorio,
                                  UsuarioRepositorio usuarioRepositorio,
                                  UsuarioRolRepositorio usuarioRolRepositorio,
                                  SesionCajaRepositorio sesionCajaRepositorio,
                                  BitacoraOperacionRepositorio bitacoraOperacionRepositorio,
                                  SesionCajaServicio sesionCajaServicio) {
        this.movimientoCajaRepositorio = movimientoCajaRepositorio;
        this.usuarioRepositorio = usuarioRepositorio;
        this.usuarioRolRepositorio = usuarioRolRepositorio;
        this.sesionCajaRepositorio = sesionCajaRepositorio;
        this.bitacoraOperacionRepositorio = bitacoraOperacionRepositorio;
        this.sesionCajaServicio = sesionCajaServicio;
    }

    @Transactional
    public RetiroRespuesta retirar(Long usuarioId, Long sesionCajaId, BigDecimal monto, String motivo) {
        validarId(usuarioId, "usuario");
        validarId(sesionCajaId, "sesión de caja");
        validarMonto(monto);
        motivo = validarTexto(motivo, "motivo del retiro", 200);

        Usuario usuario = usuarioRepositorio.findById(usuarioId)
                .orElseThrow(() -> new OperacionRechazadaException("El usuario no existe."));

        if (!usuario.getActivo()) {
            throw new OperacionRechazadaException("El usuario está inactivo.");
        }

        if (!usuarioRolRepositorio.existsByUsuario_IdAndRol_CodigoAndRol_ActivoTrue(usuarioId, "ADMIN")) {
            throw new OperacionRechazadaException("El usuario no tiene el rol ADMIN activo.");
        }

        SesionCaja sesion = sesionCajaRepositorio.buscarParaOperacion(sesionCajaId)
                .orElseThrow(() -> new OperacionRechazadaException("La sesión de caja no existe."));

        if (!sesion.estaAbierta()) {
            throw new OperacionRechazadaException("La sesión de caja ya está cerrada.");
        }

        BigDecimal totalIngresos = movimientoCajaRepositorio.sumarPorSesionYTipo(sesionCajaId, TipoMovimientoCaja.INGRESO_COBRO);
        BigDecimal totalRetiros = movimientoCajaRepositorio.sumarPorSesionYTipo(sesionCajaId, TipoMovimientoCaja.RETIRO);
        BigDecimal saldoAnterior = sesion.getFondoInicial().add(totalIngresos).subtract(totalRetiros);

        if (monto.compareTo(saldoAnterior) > 0) {
            throw new OperacionRechazadaException("Saldo insuficiente para realizar el retiro: saldo disponible $"
                    + saldoAnterior.toPlainString() + ", monto solicitado $" + monto.toPlainString() + ".");
        }

        MovimientoCaja retiro = MovimientoCaja.crearRetiro(sesion, usuario, monto, motivo);
        retiro = movimientoCajaRepositorio.save(retiro);

        bitacoraOperacionRepositorio.save(
                BitacoraOperacion.registrar(
                        usuario,
                        TipoOperacion.RETIRO_EFECTIVO,
                        "movimiento_caja",
                        retiro.getId(),
                        "Retiro de efectivo",
                        ResultadoOperacion.EXITOSO));

        BigDecimal saldoRestante = saldoAnterior.subtract(monto);
        return RetiroRespuesta.de(retiro, saldoAnterior, saldoRestante);
    }

    @Transactional(readOnly = true)
    public List<MovimientoCajaRespuesta> listarPorSesion(Long sesionCajaId,
                                                         Long usuarioId) {
        sesionCajaServicio.validarAccesoPorSesionId(sesionCajaId, usuarioId);

        return movimientoCajaRepositorio
                .findBySesionCaja_IdOrderByFechaHoraAsc(sesionCajaId)
                .stream()
                .map(MovimientoCajaRespuesta::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public MovimientoCajaRespuesta buscarPorId(Long movimientoId,
                                               Long usuarioId) {
        validarId(movimientoId, "movimiento");
        validarId(usuarioId, "usuario");

        Optional<MovimientoCaja> movimientoCaja = movimientoCajaRepositorio.findById(movimientoId);
        if (movimientoCaja.isEmpty()) {
            throw new RecursoNoEncontradoException("El movimiento de caja no existe.");
        }

        Long sesionId = movimientoCaja.get().getSesionCaja().getId();
        sesionCajaServicio.validarAccesoPorSesionId(sesionId, usuarioId);
        return MovimientoCajaRespuesta.de(movimientoCaja.get());
    }
}
