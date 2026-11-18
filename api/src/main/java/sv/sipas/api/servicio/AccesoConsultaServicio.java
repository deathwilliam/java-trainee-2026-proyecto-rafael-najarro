package sv.sipas.api.servicio;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.sipas.api.entidad.Cliente;
import sv.sipas.api.entidad.CuentaServicio;
import sv.sipas.api.entidad.Pago;
import sv.sipas.api.excepcion.AccesoDenegadoException;
import sv.sipas.api.repositorio.UsuarioRolRepositorio;

@Service
@Transactional(readOnly = true)
public class AccesoConsultaServicio {

    private final UsuarioRolRepositorio usuarioRolRepositorio;

    public AccesoConsultaServicio(UsuarioRolRepositorio usuarioRolRepositorio) {
        this.usuarioRolRepositorio = usuarioRolRepositorio;
    }

    public void validarUsuarioId(Long usuarioId) {
        if (usuarioId == null || usuarioId <= 0) {
            throw new IllegalArgumentException("El identificador del usuario debe ser mayor que cero.");
        }
    }

    private boolean tieneRol(Long usuarioId, String codigo) {
        validarUsuarioId(usuarioId);
        return usuarioRolRepositorio.existsByUsuario_IdAndRol_CodigoAndRol_ActivoTrue(usuarioId, codigo);
    }

    public boolean esAdmin(Long usuarioId) {
        return tieneRol(usuarioId, "ADMIN");
    }

    public boolean esAsesor(Long usuarioId) {
        return tieneRol(usuarioId, "ASESOR");
    }

    public boolean esCliente(Long usuarioId) {
        return tieneRol(usuarioId, "CLIENTE");
    }

    private boolean esPropietario(Cliente cliente, Long usuarioId) {
        return cliente.getUsuario() != null
                && cliente.getUsuario().getId().equals(usuarioId);
    }

    public void validarCliente(Cliente cliente, Long usuarioId) {
        boolean permitido = esAdmin(usuarioId)
                || (esCliente(usuarioId)
                && esPropietario(cliente, usuarioId));

        if (!permitido) {
            throw new AccesoDenegadoException("No tiene permiso para consultar este cliente.");
        }
    }

    public void validarCuenta(CuentaServicio cuenta, Long usuarioId) {
        boolean permitido = esAdmin(usuarioId)
                || esAsesor(usuarioId)
                || (esCliente(usuarioId)
                && esPropietario(cuenta.getCliente(), usuarioId));

        if (!permitido) {
            throw new AccesoDenegadoException("No tiene permiso para consultar esta cuenta de servicio.");
        }
    }

    public void validarPago(Pago pago, Long usuarioId) {
        boolean sesionPropia = pago.getSesionCaja()
                .getEmpleado()
                .getUsuario()
                .getId()
                .equals(usuarioId);

        boolean clientePropietario = esPropietario(
                pago.getFactura().getCuentaServicio().getCliente(),
                usuarioId);

        boolean permitido = esAdmin(usuarioId)
                || (esAsesor(usuarioId) && sesionPropia)
                || (esCliente(usuarioId) && clientePropietario);

        if (!permitido) {
            throw new AccesoDenegadoException("No tiene permiso para consultar este pago.");
        }
    }
}