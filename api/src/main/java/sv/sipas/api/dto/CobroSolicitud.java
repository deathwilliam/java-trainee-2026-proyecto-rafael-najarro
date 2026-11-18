package sv.sipas.api.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CobroSolicitud(
        @NotNull(message = "El identificador de la factura es obligatorio.")
        @Positive(message = "El identificador de la factura debe ser mayor que cero.")
        Long facturaId,

        @NotNull(message = "El identificador de la sesión de caja es obligatorio.")
        @Positive(message = "El identificador de la sesión de caja debe ser mayor que cero.")
        Long sesionCajaId,

        @NotNull(message = "El efectivo recibido es obligatorio.")
        @Positive(message = "El efectivo recibido debe ser mayor que cero.")
        @Digits(integer = 10, fraction = 2, message = "El efectivo recibido admite hasta 10 dígitos enteros y 2 decimales.")
        BigDecimal efectivoRecibido) {
}
