package sv.sipas.api.controlador;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import sv.sipas.api.dto.FacturaRespuesta;
import sv.sipas.api.entidad.enums.EstadoFactura;
import sv.sipas.api.servicio.FacturaServicio;
import sv.sipas.api.servicio.UsuarioServicio;

import java.util.List;

@Tag(name = "Facturas", description = "Consulta de facturas por identificador, cuenta de servicio y estado")
@RestController
@RequestMapping("/api/facturas")
public class FacturaControlador {

    private final FacturaServicio facturaServicio;
    private final UsuarioServicio usuarioServicio;

    public FacturaControlador(FacturaServicio facturaServicio,
                              UsuarioServicio usuarioServicio) {
        this.facturaServicio = facturaServicio;
        this.usuarioServicio = usuarioServicio;
    }

    @Operation(
            summary = "Listar facturas por cuenta de servicio y estado opcional",
            description = "200 con la lista; " +
                    "400 si los parámetros son inválidos; " +
                    "403 si no tiene acceso a la cuenta; " +
                    "404 si la cuenta no existe")
    @GetMapping
    public List<FacturaRespuesta> listarPorCuentaYEstado(@RequestParam(name = "cuentaServicioId") Long cuentaServicioId,
                                                         @RequestParam(name = "estado", required = false) EstadoFactura estado,
                                                         @AuthenticationPrincipal Jwt token) {
        return facturaServicio.listarPorCuentaYEstado(cuentaServicioId, estado, usuarioId(token));
    }

    @Operation(
            summary = "Consultar una factura",
            description = "200 con la factura; " +
                    "400 si el identificador es inválido; " +
                    "403 si no tiene acceso; " +
                    "404 si la factura no existe")
    @GetMapping("/{id}")
    public FacturaRespuesta consultarPorId(@PathVariable Long id,
                                           @AuthenticationPrincipal Jwt token) {
        return facturaServicio.consultarPorId(id, usuarioId(token));
    }

    private Long usuarioId(Jwt token) {
        return usuarioServicio.obtenerIdUsuarioActivo(token.getSubject());
    }
}
