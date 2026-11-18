package sv.sipas.api.servicio;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.sipas.api.dto.CobroRespuesta;
import sv.sipas.api.dto.FacturaRespuesta;
import sv.sipas.api.entidad.*;
import sv.sipas.api.entidad.enums.EstadoFactura;
import sv.sipas.api.entidad.enums.ResultadoOperacion;
import sv.sipas.api.entidad.enums.TipoOperacion;
import sv.sipas.api.excepcion.OperacionRechazadaException;
import sv.sipas.api.excepcion.RecursoNoEncontradoException;
import sv.sipas.api.excepcion.SaldoInsuficienteException;
import sv.sipas.api.repositorio.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class FacturaServicio {

    private static final DateTimeFormatter FORMATO_FECHA_COMPROBANTE = DateTimeFormatter.BASIC_ISO_DATE;

    private final FacturaRepositorio facturaRepositorio;
    private final SesionCajaRepositorio sesionCajaRepositorio;
    private final PagoRepositorio pagoRepositorio;
    private final MovimientoCajaRepositorio movimientoCajaRepositorio;
    private final UsuarioRepositorio usuarioRepositorio;
    private final BitacoraOperacionRepositorio bitacoraOperacionRepositorio;
    private final UsuarioRolRepositorio usuarioRolRepositorio;
    private final CuentaServicioRepositorio cuentaServicioRepositorio;
    private final AccesoConsultaServicio accesoConsultaServicio;

    public FacturaServicio(FacturaRepositorio facturaRepositorio,
                           SesionCajaRepositorio sesionCajaRepositorio,
                           PagoRepositorio pagoRepositorio,
                           MovimientoCajaRepositorio movimientoCajaRepositorio,
                           UsuarioRepositorio usuarioRepositorio,
                           BitacoraOperacionRepositorio bitacoraOperacionRepositorio,
                           UsuarioRolRepositorio usuarioRolRepositorio,
                           CuentaServicioRepositorio cuentaServicioRepositorio,
                           AccesoConsultaServicio accesoConsultaServicio) {
        this.facturaRepositorio = facturaRepositorio;
        this.sesionCajaRepositorio = sesionCajaRepositorio;
        this.pagoRepositorio = pagoRepositorio;
        this.movimientoCajaRepositorio = movimientoCajaRepositorio;
        this.usuarioRepositorio = usuarioRepositorio;
        this.bitacoraOperacionRepositorio = bitacoraOperacionRepositorio;
        this.usuarioRolRepositorio = usuarioRolRepositorio;
        this.cuentaServicioRepositorio = cuentaServicioRepositorio;
        this.accesoConsultaServicio = accesoConsultaServicio;
    }

    @Transactional
    public CobroRespuesta cobrarFactura(Long usuarioId, Long facturaId,
                                        Long sesionCajaId, BigDecimal efectivoRecibido) {
        if (facturaId == null || facturaId <= 0) {
            throw new IllegalArgumentException("El identificador de la factura debe ser mayor que cero.");
        }

        if (sesionCajaId == null || sesionCajaId <= 0) {
            throw new IllegalArgumentException("El identificador de la sesión de caja debe ser mayor que cero.");
        }

        if (efectivoRecibido == null || efectivoRecibido.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El efectivo recibido debe ser mayor que cero.");
        }

        SesionCaja sesionCaja = sesionCajaRepositorio.buscarParaOperacion(sesionCajaId)
                .orElseThrow(() -> new OperacionRechazadaException("La sesión de caja no existe."));
        Factura facturaACobrar = facturaRepositorio.buscarParaCobro(facturaId)
                .orElseThrow(() -> new OperacionRechazadaException("La factura no existe."));

        Usuario usuario = usuarioRepositorio.findById(usuarioId)
                .orElseThrow(() -> new OperacionRechazadaException("El usuario no existe."));

        //R1: factura cuyo estado sea pendiente
        if (!facturaACobrar.estaPendiente()) {
            throw new OperacionRechazadaException("La factura no está pendiente de pago.");
        }

        //R2: cuenta servicio activa
        if (!facturaACobrar.getCuentaServicio().estaActiva()) {
            throw new OperacionRechazadaException("La cuenta de servicio está inactiva.");
        }

        //R3: servicio y proveedor activos
        if (!facturaACobrar.getCuentaServicio().getServicio().getActivo() ||
                !facturaACobrar.getCuentaServicio().getServicio().getProveedor().getActivo()) {
            throw new OperacionRechazadaException("El servicio no está habilitado para cobros.");
        }

        //R4: sesion caja abierta, pertenecer al usuario autenticado y con rol asesor,
        // usuario, empleado, caja y sucursal activos
        if (!sesionCaja.estaAbierta()
                || !sesionCaja.getEmpleado().getUsuario().getId().equals(usuario.getId())
                || !usuarioRolRepositorio.existsByUsuario_IdAndRol_CodigoAndRol_ActivoTrue(usuario.getId(),
                "ASESOR")
                || !usuario.getActivo()
                || !sesionCaja.getEmpleado().getActivo()
                || !sesionCaja.getCaja().getActivo()
                || !sesionCaja.getCaja().getSucursal().getActivo()) {
            throw new OperacionRechazadaException("No tiene una sesión de caja habilitada para cobrar.");
        }

        //R5: efectivo recibido mayor o igual a monto + comision_aplicada
        BigDecimal total = facturaACobrar.getMonto().add(facturaACobrar.getCuentaServicio().getServicio().getComision());
        if (efectivoRecibido.compareTo(total) < 0) {
            throw new SaldoInsuficienteException(facturaACobrar.getNumeroFactura(),
                    efectivoRecibido, total);
        }

        //R6: una factura puede tener como máximo un pago
        if (pagoRepositorio.existsByFactura_Id(facturaId)) {
            throw new OperacionRechazadaException("La factura ya tiene un pago registrado.");
        }

        Pago pagoFactura = new Pago(
                facturaACobrar,
                sesionCaja,
                String.format("SIPAS-%s-%06d",
                        LocalDate.now().format(FORMATO_FECHA_COMPROBANTE),
                        pagoRepositorio.obtenerSiguienteNumeroComprobante()),
                facturaACobrar.getCuentaServicio().getServicio().getComision(),
                efectivoRecibido);
        pagoRepositorio.save(pagoFactura);

        facturaACobrar.marcarPagada();
        facturaRepositorio.save(facturaACobrar);

        movimientoCajaRepositorio.save(MovimientoCaja.crearIngreso(pagoFactura, usuario));

        bitacoraOperacionRepositorio.save(
                BitacoraOperacion.registrar(
                        usuario,
                        TipoOperacion.COBRO_FACTURA,
                        "pago",
                        pagoFactura.getId(),
                        "Cobro de factura",
                        ResultadoOperacion.EXITOSO));

        return new CobroRespuesta(
                pagoFactura.getNumeroComprobante(),
                facturaACobrar.getCuentaServicio().getServicio().getProveedor().getNombre(),
                facturaACobrar.getCuentaServicio().getServicio().getNombre(),
                facturaACobrar.getCuentaServicio().getReferencia(),
                facturaACobrar.getNumeroFactura(),
                facturaACobrar.getMonto(),
                pagoFactura.getComisionAplicada(),
                pagoFactura.calcularTotal(),
                pagoFactura.getEfectivoRecibido(),
                pagoFactura.calcularCambio(),
                pagoFactura.getFechaHora()
        );
    }

    @Transactional(readOnly = true)
    public List<FacturaRespuesta> listarPorCuentaYEstado(Long cuentaServicioId, EstadoFactura estado, Long usuarioId) {

        accesoConsultaServicio.validarUsuarioId(usuarioId);

        if (cuentaServicioId == null || cuentaServicioId <= 0) {
            throw new IllegalArgumentException("El identificador de la cuenta de servicio debe ser mayor que cero.");
        }

        CuentaServicio cuenta = cuentaServicioRepositorio
                .findById(cuentaServicioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("La cuenta de servicio no existe."));

        accesoConsultaServicio.validarCuenta(cuenta, usuarioId);

        List<Factura> facturas;

        if (estado == null) {
            facturas = facturaRepositorio.findByCuentaServicio_Id(cuentaServicioId);
        } else {
            facturas = facturaRepositorio
                    .findByCuentaServicio_IdAndEstado(cuentaServicioId, estado);
        }

        return facturas.stream()
                .map(FacturaRespuesta::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public FacturaRespuesta consultarPorId(Long facturaId, Long usuarioId) {
        accesoConsultaServicio.validarUsuarioId(usuarioId);

        if (facturaId == null || facturaId <= 0) {
            throw new IllegalArgumentException("El identificador de la factura debe ser mayor que cero.");
        }

        Factura factura = facturaRepositorio.findById(facturaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("La factura no existe."));

        accesoConsultaServicio.validarCuenta(factura.getCuentaServicio(), usuarioId);
        return FacturaRespuesta.de(factura);
    }
}
