package sv.sipas.api.dto;

import sv.sipas.api.entidad.Sucursal;

public record SucursalRespuesta(Long id, String codigo, String nombre,
                                String direccion, Boolean activo) {

    public static SucursalRespuesta de(Sucursal sucursal) {
        return new SucursalRespuesta(
                sucursal.getId(),
                sucursal.getCodigo(),
                sucursal.getNombre(),
                sucursal.getDireccion(),
                sucursal.getActivo()
        );
    }
}
