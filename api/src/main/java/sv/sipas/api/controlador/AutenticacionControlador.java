package sv.sipas.api.controlador;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import sv.sipas.api.dto.LoginSolicitud;
import sv.sipas.api.dto.TokenRespuesta;
import sv.sipas.api.servicio.AutenticacionServicio;

import java.util.Map;

@Tag(name = "Autenticación", description = "Inicio de sesión y consulta de los datos del token autenticado")
@RestController
@RequestMapping("/api/auth")
public class AutenticacionControlador {

    private final AutenticacionServicio autenticacion;

    public AutenticacionControlador(AutenticacionServicio autenticacion) {
        this.autenticacion = autenticacion;
    }

    @Operation(
            summary = "Iniciar sesión y obtener token JWT",
            description = "200 con el token; " +
                    "400 si los datos son inválidos; " +
                    "401 si las credenciales son incorrectas o el usuario no está habilitado para iniciar sesión")
    @SecurityRequirements
    @PostMapping("/login")
    public TokenRespuesta login(@Valid @RequestBody LoginSolicitud solicitud) {
        return autenticacion.entrar(solicitud.usuario(), solicitud.clave());
    }

    @Operation(
            summary = "Consultar los datos del usuario autenticado",
            description = "200 con el usuario, sus roles y el vencimiento del token; " +
                    "401 si el token está ausente, es inválido o está vencido")
    @GetMapping("/yo")
    public Map<String, Object> yo(@AuthenticationPrincipal Jwt token) {
        return Map.of(
                "usuario", token.getSubject(),
                "roles", token.getClaimAsStringList("roles"),
                "venceEn", token.getExpiresAt().toString());
    }
}
