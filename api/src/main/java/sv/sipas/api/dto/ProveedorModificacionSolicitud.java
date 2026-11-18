package sv.sipas.api.dto;

import jakarta.validation.constraints.Size;

public record ProveedorModificacionSolicitud(
        @Size(max = 100, message = "El nombre no debe exceder los 100 caracteres.")
        String nombre,

        Boolean activo) {
}
