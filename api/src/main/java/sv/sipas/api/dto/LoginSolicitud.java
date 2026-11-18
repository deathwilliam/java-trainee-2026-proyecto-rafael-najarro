package sv.sipas.api.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginSolicitud(
        @NotBlank(message = "El nombre de usuario es obligatorio.")
        String usuario,

        @NotBlank(message = "La contraseña es obligatoria.")
        String clave) {
}
