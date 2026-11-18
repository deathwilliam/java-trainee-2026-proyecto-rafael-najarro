package sv.sipas.api.dto;

import jakarta.validation.constraints.Size;

public record SucursalModificacionSolicitud(
        @Size(max = 80, message = "El nombre no debe exceder los 80 caracteres.")
        String nombre,

        @Size(max = 200, message = "La dirección no debe exceder los 200 caracteres.")
        String direccion,

        Boolean activo) {
}
