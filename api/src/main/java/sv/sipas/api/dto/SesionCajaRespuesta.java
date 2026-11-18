package sv.sipas.api.dto;

import sv.sipas.api.entidad.SesionCaja;
import sv.sipas.api.entidad.enums.EstadoSesionCaja;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SesionCajaRespuesta(Long id, Long cajaId, Long empleadoId,
                                  EstadoSesionCaja estado, LocalDateTime fechaApertura,
                                  LocalDateTime fechaCierre, BigDecimal fondoInicial, BigDecimal efectivoContado) {

    public static SesionCajaRespuesta de(SesionCaja sesionCaja) {

        return new SesionCajaRespuesta(
                sesionCaja.getId(),
                sesionCaja.getCaja().getId(),
                sesionCaja.getEmpleado().getId(),
                sesionCaja.getEstado(),
                sesionCaja.getFechaApertura(),
                sesionCaja.getFechaCierre(),
                sesionCaja.getFondoInicial(),
                sesionCaja.getEfectivoContado());
    }
}
