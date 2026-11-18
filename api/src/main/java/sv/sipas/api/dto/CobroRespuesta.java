package sv.sipas.api.dto;

import sv.sipas.api.entidad.Pago;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CobroRespuesta(String numeroComprobante, String proveedor, String servicio, String referencia,
                             String numeroFactura, BigDecimal montoFactura, BigDecimal comisionAplicada,
                             BigDecimal totalCobrado, BigDecimal efectivoRecibido, BigDecimal cambio,
                             LocalDateTime fechaHora) {

    public static CobroRespuesta de(Pago pago) {
        return new CobroRespuesta(
                pago.getNumeroComprobante(),
                pago.getFactura().getCuentaServicio().getServicio().getProveedor().getNombre(),
                pago.getFactura().getCuentaServicio().getServicio().getNombre(),
                pago.getFactura().getCuentaServicio().getReferencia(),
                pago.getFactura().getNumeroFactura(),
                pago.getFactura().getMonto(),
                pago.getComisionAplicada(),
                pago.calcularTotal(),
                pago.getEfectivoRecibido(),
                pago.calcularCambio(),
                pago.getFechaHora()
        );
    }
}
