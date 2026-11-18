package sv.sipas.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProveedorSolicitud(
        @NotBlank(message = "El código es obligatorio.")
        @Size(max = 20, message = "El código no debe exceder los 20 caracteres.")
        String codigo,

        @NotBlank(message = "El nombre es obligatorio.")
        @Size(max = 100, message = "El nombre no debe exceder los 100 caracteres.")
        String nombre) {
}
