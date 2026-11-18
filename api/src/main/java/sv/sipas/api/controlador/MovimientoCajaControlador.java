package sv.sipas.api.controlador;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sv.sipas.api.dto.MovimientoCajaRespuesta;
import sv.sipas.api.servicio.MovimientoCajaServicio;
import sv.sipas.api.servicio.UsuarioServicio;

@Tag(name = "Movimientos de caja", description = "Consulta de movimientos de ingreso por cobro y retiro de efectivo")
@RestController
@RequestMapping("/api/movimientos-caja")
public class MovimientoCajaControlador {

    private final MovimientoCajaServicio movimientoCajaServicio;
    private final UsuarioServicio usuarioServicio;

    public MovimientoCajaControlador(MovimientoCajaServicio movimientoCajaServicio,
                                     UsuarioServicio usuarioServicio) {
        this.movimientoCajaServicio = movimientoCajaServicio;
        this.usuarioServicio = usuarioServicio;
    }

    @Operation(
            summary = "Consultar un movimiento",
            description = "200 con el movimiento; " +
                    "400 si el identificador es inválido; " +
                    "403 si no tiene acceso a la sesión; " +
                    "404 si el movimiento no existe")
    @GetMapping("/{id}")
    public MovimientoCajaRespuesta buscarPorId(@PathVariable Long id,
                                               @AuthenticationPrincipal Jwt token) {
        Long usuarioId = usuarioServicio.obtenerIdUsuarioActivo(token.getSubject());
        return movimientoCajaServicio.buscarPorId(id, usuarioId);
    }

}
