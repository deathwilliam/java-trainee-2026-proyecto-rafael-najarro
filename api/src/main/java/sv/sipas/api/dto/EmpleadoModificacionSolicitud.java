package sv.sipas.api.dto;

import jakarta.validation.constraints.Size;

public record EmpleadoModificacionSolicitud(
        @Size(max = 80, message = "Los nombres no deben exceder los 80 caracteres.")
        String nombres,

        @Size(max = 80, message = "Los apellidos no deben exceder los 80 caracteres.")
        String apellidos,

        Boolean activo) {
}
