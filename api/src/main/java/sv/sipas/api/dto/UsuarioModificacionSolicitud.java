package sv.sipas.api.dto;

import jakarta.validation.constraints.Size;

public record UsuarioModificacionSolicitud(
        @Size(max = 50, message = "El nombre de usuario no debe exceder los 50 caracteres.")
        String nombreUsuario,

        String clave,

        Boolean activo) {
}
