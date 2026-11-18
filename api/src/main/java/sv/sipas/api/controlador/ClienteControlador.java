package sv.sipas.api.controlador;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import sv.sipas.api.dto.ClienteRespuesta;
import sv.sipas.api.dto.CuentaServicioRespuesta;
import sv.sipas.api.servicio.ClienteServicio;
import sv.sipas.api.servicio.UsuarioServicio;

import java.util.List;

@Tag(name = "Clientes", description = "Consulta de clientes y sus cuentas de servicio")
@RestController
@RequestMapping("/api/clientes")
public class ClienteControlador {

    private final ClienteServicio clienteServicio;
    private final UsuarioServicio usuarioServicio;

    public ClienteControlador(ClienteServicio clienteServicio,
                              UsuarioServicio usuarioServicio) {
        this.clienteServicio = clienteServicio;
        this.usuarioServicio = usuarioServicio;
    }

    @Operation(
            summary = "Listar clientes para administración",
            description = "200 con la lista; " +
                    "[] si no hay resultados; " +
                    "403 si no tiene el rol ADMIN")
    @GetMapping
    public List<ClienteRespuesta> listarClientes(@AuthenticationPrincipal Jwt token) {
        return clienteServicio.listarClientes(usuarioId(token));
    }

    @Operation(
            summary = "Consultar un cliente",
            description = "200 con el cliente; " +
                    "400 si el identificador es inválido; " +
                    "403 si no tiene acceso; " +
                    "404 si el cliente no existe")
    @GetMapping("/{id}")
    public ClienteRespuesta consultarClientePorId(@PathVariable Long id,
                                                  @AuthenticationPrincipal Jwt token) {
        return clienteServicio.consultarClientePorId(id, usuarioId(token));
    }

    @Operation(
            summary = "Listar cuentas de un cliente",
            description = "200 con la lista; [] si el cliente no tiene cuentas; "
                    + "400 si el identificador es inválido; " +
                    "403 si no tiene acceso; " +
                    "404 si el cliente no existe")
    @GetMapping("/{clienteId}/cuentas-servicio")
    public List<CuentaServicioRespuesta> listarCuentasCliente(@PathVariable Long clienteId,
                                                              @AuthenticationPrincipal Jwt token) {
        return clienteServicio.listarCuentasCliente(clienteId, usuarioId(token));
    }

    private Long usuarioId(Jwt token) {
        return usuarioServicio.obtenerIdUsuarioActivo(token.getSubject());
    }
}
