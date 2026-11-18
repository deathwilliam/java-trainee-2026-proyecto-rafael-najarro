package sv.sipas.api.controlador;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import sv.sipas.api.dto.CuentaServicioRespuesta;
import sv.sipas.api.servicio.CuentaServicioServicio;
import sv.sipas.api.servicio.UsuarioServicio;

@Tag(name = "Cuentas de servicio", description = "Consulta de cuentas de servicio por identificador o referencia")
@RestController
@RequestMapping("/api/cuentas-servicio")
public class CuentaServicioControlador {

    private final CuentaServicioServicio cuentaServicioServicio;
    private final UsuarioServicio usuarioServicio;

    public CuentaServicioControlador(CuentaServicioServicio cuentaServicioServicio,
                                     UsuarioServicio usuarioServicio) {
        this.cuentaServicioServicio = cuentaServicioServicio;
        this.usuarioServicio = usuarioServicio;
    }

    @Operation(summary = "Buscar una cuenta de servicio por servicio y referencia",
            description = "200 con la cuenta; " +
                    "400 si los parámetros son inválidos; " +
                    "403 si no tiene acceso; " +
                    "404 si la cuenta no existe")
    @GetMapping
    public CuentaServicioRespuesta buscarPorServicioYReferencia(@RequestParam(name = "servicioId") Long servicioId,
                                                                @RequestParam(name = "referencia") String referencia,
                                                                @AuthenticationPrincipal Jwt token) {
        return cuentaServicioServicio.buscarPorServicioYReferencia(servicioId, referencia, usuarioId(token));
    }

    @Operation(summary = "Consultar una cuenta de servicio por identificador",
            description = "200 con la cuenta; " +
                    "400 si el identificador es inválido; " +
                    "403 si no tiene acceso; " +
                    "404 si la cuenta no existe")
    @GetMapping("/{id}")
    public CuentaServicioRespuesta consultarCuenta(@PathVariable Long id,
                                                   @AuthenticationPrincipal Jwt token) {
        return cuentaServicioServicio.consultarCuenta(id, usuarioId(token));
    }

    private Long usuarioId(Jwt token) {
        return usuarioServicio.obtenerIdUsuarioActivo(token.getSubject());
    }
}
