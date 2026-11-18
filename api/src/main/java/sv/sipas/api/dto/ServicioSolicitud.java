package sv.sipas.api.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record ServicioSolicitud(
        @NotNull(message = "El identificador del proveedor es obligatorio.")
        @Positive(message = "El identificador del proveedor debe ser mayor que cero.")
        Long proveedorId,

        @NotBlank(message = "El código es obligatorio.")
        @Size(max = 20, message = "El código no debe exceder los 20 caracteres.")
        String codigo,

        @NotBlank(message = "El nombre es obligatorio.")
        @Size(max = 80, message = "El nombre no debe exceder los 80 caracteres.")
        String nombre,

        @NotNull(message = "La comisión es obligatoria.")
        @PositiveOrZero(message = "La comisión debe ser mayor o igual que cero.")
        @Digits(integer = 10, fraction = 2, message = "La comisión admite hasta 10 dígitos enteros y 2 decimales.")
        BigDecimal comision) {
}
