package sv.sipas.api.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record AperturaCajaSolicitud(
        @NotNull(message = "El fondo inicial es obligatorio.")
        @PositiveOrZero(message = "El fondo inicial debe ser mayor o igual que cero.")
        @Digits(integer = 10, fraction = 2, message = "El fondo inicial admite hasta 10 dígitos enteros y 2 decimales.")
        BigDecimal fondoInicial) {
}
