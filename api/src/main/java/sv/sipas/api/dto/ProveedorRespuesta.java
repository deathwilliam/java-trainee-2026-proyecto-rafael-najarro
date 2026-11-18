package sv.sipas.api.dto;

import sv.sipas.api.entidad.Proveedor;

public record ProveedorRespuesta(Long id, String codigo, String nombre, Boolean activo) {

    public static ProveedorRespuesta de(Proveedor proveedor) {
        return new ProveedorRespuesta(
                proveedor.getId(),
                proveedor.getCodigo(),
                proveedor.getNombre(),
                proveedor.getActivo()
        );
    }
}