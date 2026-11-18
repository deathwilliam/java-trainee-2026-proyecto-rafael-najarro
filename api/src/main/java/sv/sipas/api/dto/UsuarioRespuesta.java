package sv.sipas.api.dto;

import sv.sipas.api.entidad.Usuario;

public record UsuarioRespuesta(Long usuarioId, String nombreUsuario, Boolean activo) {

    public static UsuarioRespuesta de(Usuario usuario) {
        return new UsuarioRespuesta(usuario.getId(), usuario.getNombreUsuario(), usuario.getActivo());
    }
}
