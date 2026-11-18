package sv.sipas.api.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record RetiroSolicitud(
        @NotNull(message = "El monto es obligatorio.")
        @Positive(message = "El monto debe ser mayor que cero.")
        @Digits(integer = 10, fraction = 2, message = "El monto admite hasta 10 dígitos enteros y 2 decimales.")
        BigDecimal monto,

        @NotBlank(message = "El motivo del retiro es obligatorio.")
        @Size(max = 200, message = "El motivo del retiro no debe exceder los 200 caracteres.")
        String motivo) {
}
