package sv.sipas.api.dto;

import sv.sipas.api.entidad.Factura;
import sv.sipas.api.entidad.enums.EstadoFactura;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FacturaRespuesta(Long id, String numeroFactura, LocalDate periodo, BigDecimal monto,
                               LocalDate fechaEmision, LocalDate fechaVencimiento, EstadoFactura estadoFactura) {

    public static FacturaRespuesta de(Factura factura) {
        return new FacturaRespuesta(
                factura.getId(),
                factura.getNumeroFactura(),
                factura.getPeriodo(),
                factura.getMonto(),
                factura.getFechaEmision(),
                factura.getFechaVencimiento(),
                factura.getEstado()
        );
    }
}
