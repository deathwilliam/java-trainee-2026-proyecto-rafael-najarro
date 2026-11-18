package sv.sipas.api.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record CierreCajaSolicitud(
        @NotNull(message = "El efectivo contado es obligatorio.")
        @PositiveOrZero(message = "El efectivo contado debe ser mayor o igual que cero.")
        @Digits(integer = 10, fraction = 2, message = "El efectivo contado admite hasta 10 dígitos enteros y 2 decimales.")
        BigDecimal efectivoContado) {
}
