package sv.sipas.api.dto;

import sv.sipas.api.entidad.MovimientoCaja;
import sv.sipas.api.entidad.enums.TipoMovimientoCaja;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MovimientoCajaRespuesta(Long id, Long sesionCajaId, Long pagoId,
                                      Long usuarioId, TipoMovimientoCaja tipo, BigDecimal monto,
                                      String motivo, LocalDateTime fechaHora) {

    public static MovimientoCajaRespuesta de(MovimientoCaja movimiento) {
        return new MovimientoCajaRespuesta(
                movimiento.getId(),
                movimiento.getSesionCaja().getId(),
                movimiento.getPago() != null ? movimiento.getPago().getId() : null,
                movimiento.getUsuario().getId(),
                movimiento.getTipo(),
                movimiento.getMonto(),
                movimiento.getMotivo(),
                movimiento.getFechaHora()
        );
    }
}