package sv.sipas.api.servicio;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.sipas.api.dto.ClienteRespuesta;
import sv.sipas.api.dto.CuentaServicioRespuesta;
import sv.sipas.api.entidad.Cliente;
import sv.sipas.api.excepcion.AccesoDenegadoException;
import sv.sipas.api.excepcion.RecursoNoEncontradoException;
import sv.sipas.api.repositorio.ClienteRepositorio;
import sv.sipas.api.repositorio.CuentaServicioRepositorio;

import java.util.List;

@Service
public class ClienteServicio {

    private final ClienteRepositorio clienteRepositorio;
    private final CuentaServicioRepositorio cuentaServicioRepositorio;
    private final AccesoConsultaServicio accesoConsultaServicio;

    public ClienteServicio(ClienteRepositorio clienteRepositorio,
                           CuentaServicioRepositorio cuentaServicioRepositorio,
                           AccesoConsultaServicio accesoConsultaServicio) {
        this.clienteRepositorio = clienteRepositorio;
        this.cuentaServicioRepositorio = cuentaServicioRepositorio;
        this.accesoConsultaServicio = accesoConsultaServicio;
    }

    @Transactional(readOnly = true)
    public List<ClienteRespuesta> listarClientes(Long usuarioId) {
        if (!accesoConsultaServicio.esAdmin(usuarioId)) {
            throw new AccesoDenegadoException("No tiene permiso para listar clientes.");
        }

        return clienteRepositorio.findAll().stream()
                .map(ClienteRespuesta::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public ClienteRespuesta consultarClientePorId(Long clienteId, Long usuarioId) {
        Cliente cliente = obtenerClienteAutorizado(clienteId, usuarioId);
        return ClienteRespuesta.de(cliente);
    }

    @Transactional(readOnly = true)
    public List<CuentaServicioRespuesta> listarCuentasCliente(Long clienteId, Long usuarioId) {
        obtenerClienteAutorizado(clienteId, usuarioId);

        return cuentaServicioRepositorio.findByCliente_Id(clienteId)
                .stream()
                .map(CuentaServicioRespuesta::de)
                .toList();
    }

    private Cliente obtenerClienteAutorizado(Long clienteId, Long usuarioId) {
        accesoConsultaServicio.validarUsuarioId(usuarioId);

        if (clienteId == null || clienteId <= 0) {
            throw new IllegalArgumentException("El identificador del cliente debe ser mayor que cero.");
        }

        Cliente cliente = clienteRepositorio.findById(clienteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("El cliente no existe."));

        accesoConsultaServicio.validarCliente(cliente, usuarioId);
        return cliente;
    }
}
