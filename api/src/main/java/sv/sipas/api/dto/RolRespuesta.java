package sv.sipas.api.dto;

import sv.sipas.api.entidad.Rol;

public record RolRespuesta(Long id, String codigo, String descripcion, Boolean activo) {

    public static RolRespuesta de(Rol rol) {
        return new RolRespuesta(rol.getId(), rol.getCodigo(), rol.getDescripcion(), rol.getActivo());
    }
}
