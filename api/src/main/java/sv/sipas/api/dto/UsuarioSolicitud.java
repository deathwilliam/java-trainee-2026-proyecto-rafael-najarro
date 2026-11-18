package sv.sipas.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UsuarioSolicitud(
        @NotBlank(message = "El nombre de usuario es obligatorio.")
        @Size(max = 50, message = "El nombre de usuario no debe exceder los 50 caracteres.")
        String nombreUsuario,

        @NotBlank(message = "La contraseña es obligatoria.")
        String clave) {
}
