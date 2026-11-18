package sv.sipas.api.dto;

import jakarta.validation.constraints.Size;

public record CajaModificacionSolicitud(
        @Size(max = 60, message = "El nombre no debe exceder los 60 caracteres.")
        String nombre,
        Boolean activo) {
}
