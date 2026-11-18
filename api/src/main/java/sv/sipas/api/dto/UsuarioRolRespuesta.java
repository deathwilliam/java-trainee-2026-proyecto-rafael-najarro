package sv.sipas.api.dto;

import sv.sipas.api.entidad.UsuarioRol;

public record UsuarioRolRespuesta(String nombreUsuario, String rol) {

    public static UsuarioRolRespuesta de(UsuarioRol usuarioRol) {
        return new UsuarioRolRespuesta(usuarioRol.getUsuario().getNombreUsuario(), usuarioRol.getRol().getCodigo());
    }
}
