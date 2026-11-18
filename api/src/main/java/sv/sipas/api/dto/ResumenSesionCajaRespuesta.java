package sv.sipas.api.dto;

import sv.sipas.api.entidad.SesionCaja;
import sv.sipas.api.entidad.enums.EstadoSesionCaja;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ResumenSesionCajaRespuesta(Long id, Long cajaId, Long empleadoId,
                                         EstadoSesionCaja estado, LocalDateTime fechaApertura,
                                         LocalDateTime fechaCierre, BigDecimal fondoInicial,
                                         BigDecimal totalIngresos, BigDecimal totalRetiros,
                                         BigDecimal saldoEsperado, BigDecimal efectivoContado, BigDecimal diferencia) {

    public static ResumenSesionCajaRespuesta de(SesionCaja sesionCaja, BigDecimal totalIngresos,
                                                BigDecimal totalRetiros, BigDecimal saldoEsperado,
                                                BigDecimal diferencia) {

        return new ResumenSesionCajaRespuesta(
                sesionCaja.getId(),
                sesionCaja.getCaja().getId(),
                sesionCaja.getEmpleado().getId(),
                sesionCaja.getEstado(),
                sesionCaja.getFechaApertura(),
                sesionCaja.getFechaCierre(),
                sesionCaja.getFondoInicial(),
                totalIngresos,
                totalRetiros,
                saldoEsperado,
                sesionCaja.getEfectivoContado(),
                diferencia);
    }
}
