package sv.sipas.api.dto;

import sv.sipas.api.entidad.Servicio;

import java.math.BigDecimal;

public record ServicioRespuesta(Long id, String nombreProveedor, String codigoServicio,
                                String nombreServicio, BigDecimal comision, Boolean activo) {

    public static ServicioRespuesta de(Servicio servicio) {
        return new ServicioRespuesta(
                servicio.getId(),
                servicio.getProveedor().getNombre(),
                servicio.getCodigo(),
                servicio.getNombre(),
                servicio.getComision(),
                servicio.getActivo()
        );
    }
}