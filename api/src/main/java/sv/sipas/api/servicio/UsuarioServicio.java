package sv.sipas.api.servicio;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.sipas.api.dto.UsuarioRespuesta;
import sv.sipas.api.entidad.Usuario;
import sv.sipas.api.excepcion.ConflictoException;
import sv.sipas.api.excepcion.CredencialesInvalidasException;
import sv.sipas.api.excepcion.OperacionRechazadaException;
import sv.sipas.api.excepcion.RecursoNoEncontradoException;
import sv.sipas.api.repositorio.UsuarioRepositorio;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioServicio {

    private final UsuarioRepositorio usuarioRepositorio;
    private final PasswordEncoder bcrypt;

    public UsuarioServicio(UsuarioRepositorio usuarioRepositorio, PasswordEncoder bcrypt) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.bcrypt = bcrypt;
    }

    @Transactional(readOnly = true)
    public List<UsuarioRespuesta> listarUsuarios() {
        return usuarioRepositorio.findAll().stream().map(UsuarioRespuesta::de).toList();
    }

    @Transactional(readOnly = true)
    public UsuarioRespuesta consultarUsuarioPorId(Long usuarioId) {
        if (usuarioId == null || usuarioId <= 0) {
            throw new IllegalArgumentException("El identificador del usuario debe ser mayor que cero.");
        }

        return usuarioRepositorio.findById(usuarioId).map(UsuarioRespuesta::de)
                .orElseThrow(() -> new RecursoNoEncontradoException("El usuario no existe"));
    }

    @Transactional
    public UsuarioRespuesta crearUsuario(String nombreUsuario, String clave) {
        if (nombreUsuario == null || nombreUsuario.isBlank() || nombreUsuario.length() > 50) {
            throw new IllegalArgumentException("El nombre de usuario es obligatorio y no debe exceder los 50 caracteres.");
        }
        if (clave == null || clave.isBlank()) {
            throw new IllegalArgumentException("El campo contraseña no debe ir vacío.");
        }
        nombreUsuario = nombreUsuario.trim();
        if (usuarioRepositorio.existsByNombreUsuario(nombreUsuario)) {
            throw new ConflictoException("Este nombre de usuario ya existe.");
        }

        Usuario usuario = usuarioRepositorio.save(new Usuario(nombreUsuario, bcrypt.encode(clave)));
        return UsuarioRespuesta.de(usuario);
    }

    @Transactional
    public UsuarioRespuesta modificarUsuario(Long usuarioId, String nombreUsuario,
                                             String clave, Boolean activo) {
        if (usuarioId == null || usuarioId <= 0) {
            throw new IllegalArgumentException("El identificador del usuario debe ser mayor que cero.");
        }

        Usuario usuario = usuarioRepositorio.findById(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el usuario con el id: " + usuarioId));

        if (nombreUsuario == null && clave == null && activo == null) {
            throw new IllegalArgumentException("Debe proporcionar al menos un campo para modificar.");
        }

        if (nombreUsuario != null) {
            if (nombreUsuario.isBlank() || nombreUsuario.length() > 50) {
                throw new IllegalArgumentException("El nombre de usuario no debe estar vacío ni exceder los 50 caracteres.");
            }

            nombreUsuario = nombreUsuario.trim();

            if (usuarioRepositorio.existsByNombreUsuarioAndIdNot(nombreUsuario, usuarioId)) {
                throw new ConflictoException("Este nombre de usuario ya existe.");
            }

            usuario.setNombreUsuario(nombreUsuario);
        }

        if (clave != null) {
            if (clave.isBlank()) {
                throw new IllegalArgumentException("El campo contraseña no debe ir vacío.");
            }

            usuario.cambiarClaveHash(bcrypt.encode(clave));
        }

        if (activo != null) {
            if (activo) {
                usuario.activar();
            } else {
                usuario.desactivar();
            }
        }

        usuarioRepositorio.save(usuario);
        return UsuarioRespuesta.de(usuario);
    }

    @Transactional
    public void desactivarUsuario(Long usuarioId) {
        if (usuarioId == null || usuarioId <= 0) {
            throw new IllegalArgumentException("El identificador del usuario debe ser mayor que cero.");
        }

        Usuario usuario = usuarioRepositorio.findById(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el usuario con el id: " + usuarioId));

        usuario.desactivar();
        usuarioRepositorio.save(usuario);
    }

    @Transactional(readOnly = true)
    public Long obtenerIdUsuarioActivo(String nombreUsuario) {
        Usuario usuario = usuarioRepositorio.findByNombreUsuario(nombreUsuario)
                .filter(Usuario::getActivo)
                .orElseThrow(CredencialesInvalidasException::new);

        return usuario.getId();
    }
}
