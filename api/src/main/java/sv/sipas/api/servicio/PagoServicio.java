package sv.sipas.api.servicio;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.sipas.api.dto.CobroRespuesta;
import sv.sipas.api.entidad.Cliente;
import sv.sipas.api.entidad.Empleado;
import sv.sipas.api.entidad.Pago;
import sv.sipas.api.excepcion.AccesoDenegadoException;
import sv.sipas.api.excepcion.RecursoNoEncontradoException;
import sv.sipas.api.repositorio.ClienteRepositorio;
import sv.sipas.api.repositorio.EmpleadoRepositorio;
import sv.sipas.api.repositorio.PagoRepositorio;

import java.util.List;

@Service
public class PagoServicio {

    private final PagoRepositorio pagoRepositorio;
    private final EmpleadoRepositorio empleadoRepositorio;
    private final ClienteRepositorio clienteRepositorio;
    private final SesionCajaServicio sesionCajaServicio;
    private final AccesoConsultaServicio accesoConsultaServicio;

    public PagoServicio(PagoRepositorio pagoRepositorio,
                        EmpleadoRepositorio empleadoRepositorio,
                        ClienteRepositorio clienteRepositorio,
                        SesionCajaServicio sesionCajaServicio,
                        AccesoConsultaServicio accesoConsultaServicio) {
        this.pagoRepositorio = pagoRepositorio;
        this.empleadoRepositorio = empleadoRepositorio;
        this.clienteRepositorio = clienteRepositorio;
        this.sesionCajaServicio = sesionCajaServicio;
        this.accesoConsultaServicio = accesoConsultaServicio;
    }

    @Transactional(readOnly = true)
    public CobroRespuesta buscarPorComprobante(String numeroComprobante, Long usuarioId) {
        accesoConsultaServicio.validarUsuarioId(usuarioId);

        if (numeroComprobante == null || numeroComprobante.isBlank()) {
            throw new IllegalArgumentException("El número de comprobante es obligatorio.");
        }

        Pago pago = pagoRepositorio
                .findByNumeroComprobante(numeroComprobante.trim())
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un pago para el comprobante indicado."));

        return respuestaAutorizada(pago, usuarioId);
    }

    @Transactional(readOnly = true)
    public CobroRespuesta buscarPorFactura(Long facturaId, Long usuarioId) {
        accesoConsultaServicio.validarUsuarioId(usuarioId);
        validarId(facturaId, "la factura");

        Pago pago = pagoRepositorio.findByFactura_Id(facturaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un pago para la factura indicada."));

        return respuestaAutorizada(pago, usuarioId);
    }

    @Transactional(readOnly = true)
    public CobroRespuesta buscarPorId(Long pagoId, Long usuarioId) {
        accesoConsultaServicio.validarUsuarioId(usuarioId);
        validarId(pagoId, "el pago");

        Pago pago = pagoRepositorio.findById(pagoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("El pago no existe."));

        return respuestaAutorizada(pago, usuarioId);
    }

    @Transactional(readOnly = true)
    public List<CobroRespuesta> listarPorSesion(Long sesionCajaId, Long usuarioId) {
        sesionCajaServicio.validarAccesoPorSesionId(
                sesionCajaId, usuarioId);

        return pagoRepositorio.findBySesionCaja_Id(sesionCajaId)
                .stream()
                .map(CobroRespuesta::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CobroRespuesta> listarPagos(Long sesionCajaId, Long empleadoId,
                                            Long clienteId, Long usuarioId) {
        accesoConsultaServicio.validarUsuarioId(usuarioId);

        if (sesionCajaId != null) {
            validarId(sesionCajaId, "la sesión de caja");
        }
        if (empleadoId != null) {
            validarId(empleadoId, "el empleado");
        }
        if (clienteId != null) {
            validarId(clienteId, "el cliente");
        }

        boolean esAdmin = accesoConsultaServicio.esAdmin(usuarioId);
        boolean esAsesor = accesoConsultaServicio.esAsesor(usuarioId);
        boolean esCliente = accesoConsultaServicio.esCliente(usuarioId);

        Long empleadoIdConsulta = empleadoId;
        Long clienteIdConsulta = clienteId;

        if (esAdmin) {
            // El ADMIN conserva los filtros recibidos.
        } else if (esAsesor) {
            Empleado empleado = empleadoRepositorio
                    .findByUsuario_Id(usuarioId)
                    .orElseThrow(() -> new AccesoDenegadoException("El usuario no tiene un empleado asociado."));

            if (empleadoId != null
                    && !empleadoId.equals(empleado.getId())) {
                throw new AccesoDenegadoException("No tiene permiso para consultar pagos de otro empleado.");
            }

            empleadoIdConsulta = empleado.getId();
        } else if (esCliente) {
            Cliente cliente = clienteRepositorio
                    .findByUsuario_Id(usuarioId)
                    .orElseThrow(() -> new AccesoDenegadoException("El usuario no tiene un cliente asociado."));

            if (clienteId != null
                    && !clienteId.equals(cliente.getId())) {
                throw new AccesoDenegadoException("No tiene permiso para consultar pagos de otro cliente.");
            }

            clienteIdConsulta = cliente.getId();
        } else {
            throw new AccesoDenegadoException("No tiene permiso para consultar pagos.");
        }

        if (sesionCajaId != null && !esCliente) {
            sesionCajaServicio.validarAccesoPorSesionId(sesionCajaId, usuarioId);
        }

        if (!esCliente && empleadoIdConsulta != null
                && !empleadoRepositorio.existsById(empleadoIdConsulta)) {
            throw new RecursoNoEncontradoException("El empleado no existe.");
        }

        if (!esCliente && clienteIdConsulta != null
                && !clienteRepositorio.existsById(clienteIdConsulta)) {
            throw new RecursoNoEncontradoException("El cliente no existe.");
        }

        return pagoRepositorio.buscarConFiltros(sesionCajaId, empleadoIdConsulta, clienteIdConsulta)
                .stream()
                .map(CobroRespuesta::de)
                .toList();
    }

    private CobroRespuesta respuestaAutorizada(Pago pago, Long usuarioId) {
        accesoConsultaServicio.validarPago(pago, usuarioId);
        return CobroRespuesta.de(pago);
    }

    private void validarId(Long id, String campo) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El identificador de " + campo + " debe ser mayor que cero.");
        }
    }
}
