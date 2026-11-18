package sv.sipas.api.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record ServicioModificacionSolicitud(
        @Size(max = 80, message = "El nombre no debe exceder los 80 caracteres.")
        String nombre,

        @PositiveOrZero(message = "La comisión debe ser mayor o igual que cero.")
        @Digits(integer = 10, fraction = 2, message = "La comisión admite hasta 10 dígitos enteros y 2 decimales.")
        BigDecimal comision,

        Boolean activo) {
}
