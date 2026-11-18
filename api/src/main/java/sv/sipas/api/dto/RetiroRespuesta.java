package sv.sipas.api.dto;

import sv.sipas.api.entidad.MovimientoCaja;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RetiroRespuesta(Long idMovimiento, Long sesionCajaId, Long usuarioId, BigDecimal monto,
                              String motivo, LocalDateTime fechaHora, BigDecimal saldoAnterior,
                              BigDecimal saldoRestante) {

    public static RetiroRespuesta de(MovimientoCaja movimiento,
                                     BigDecimal saldoAnterior,
                                     BigDecimal saldoRestante) {
        return new RetiroRespuesta(
                movimiento.getId(),
                movimiento.getSesionCaja().getId(),
                movimiento.getUsuario().getId(),
                movimiento.getMonto(),
                movimiento.getMotivo(),
                movimiento.getFechaHora(),
                saldoAnterior,
                saldoRestante
        );
    }
}
