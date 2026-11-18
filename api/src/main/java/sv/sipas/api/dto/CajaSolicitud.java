package sv.sipas.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CajaSolicitud(
        @NotNull(message = "El identificador de la sucursal es obligatorio.")
        @Positive(message = "El identificador de la sucursal debe ser mayor que cero.")
        Long sucursalId,

        @NotBlank(message = "El código es obligatorio.")
        @Size(max = 20, message = "El código no debe exceder los 20 caracteres.")
        String codigo,

        @NotBlank(message = "El nombre es obligatorio.")
        @Size(max = 60, message = "El nombre no debe exceder los 60 caracteres.")
        String nombre) {
}
