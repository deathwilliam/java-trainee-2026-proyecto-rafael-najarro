package sv.sipas.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SucursalSolicitud(
        @NotBlank(message = "El código es obligatorio.")
        @Size(max = 20, message = "El código no debe exceder los 20 caracteres.")
        String codigo,

        @NotBlank(message = "El nombre es obligatorio.")
        @Size(max = 80, message = "El nombre no debe exceder los 80 caracteres.")
        String nombre,

        @NotBlank(message = "La dirección es obligatoria.")
        @Size(max = 200, message = "La dirección no debe exceder los 200 caracteres.")
        String direccion) {
}
