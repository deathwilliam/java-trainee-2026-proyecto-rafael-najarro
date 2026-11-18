package sv.sipas.api.dto;

import sv.sipas.api.entidad.Caja;

public record CajaRespuesta(Long id, Long sucursalId, String codigo,
                            String nombre, Boolean activo) {

    public static CajaRespuesta de(Caja caja) {
        return new CajaRespuesta(
                caja.getId(),
                caja.getSucursal().getId(),
                caja.getCodigo(),
                caja.getNombre(),
                caja.getActivo()
        );
    }
}
