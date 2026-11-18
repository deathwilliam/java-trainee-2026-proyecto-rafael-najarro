package sv.sipas.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record EmpleadoSolicitud(
        @NotNull(message = "El identificador del usuario es obligatorio.")
        @Positive(message = "El identificador del usuario debe ser mayor que cero.")
        Long usuarioId,

        @NotNull(message = "El identificador de la sucursal es obligatorio.")
        @Positive(message = "El identificador de la sucursal debe ser mayor que cero.")
        Long sucursalId,

        @NotBlank(message = "El código es obligatorio.")
        @Size(max = 20, message = "El código no debe exceder los 20 caracteres.")
        String codigo,

        @NotBlank(message = "Los nombres son obligatorios.")
        @Size(max = 80, message = "Los nombres no deben exceder los 80 caracteres.")
        String nombres,

        @NotBlank(message = "Los apellidos son obligatorios.")
        @Size(max = 80, message = "Los apellidos no deben exceder los 80 caracteres.")
        String apellidos) {
}
