package sv.sipas.api.servicio;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.sipas.api.dto.UsuarioRolRespuesta;
import sv.sipas.api.entidad.Usuario;
import sv.sipas.api.entidad.UsuarioRol;
import sv.sipas.api.entidad.Rol;
import sv.sipas.api.excepcion.ConflictoException;
import sv.sipas.api.excepcion.OperacionRechazadaException;
import sv.sipas.api.excepcion.RecursoNoEncontradoException;
import sv.sipas.api.repositorio.*;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioRolServicio {

    private final UsuarioRolRepositorio usuarioRolRepositorio;
    private final UsuarioRepositorio usuarioRepositorio;
    private final RolRepositorio rolRepositorio;
    private final ClienteRepositorio clienteRepositorio;
    private final EmpleadoRepositorio empleadoRepositorio;

    public UsuarioRolServicio(UsuarioRolRepositorio usuarioRolRepositorio,
                              UsuarioRepositorio usuarioRepositorio,
                              RolRepositorio rolRepositorio,
                              ClienteRepositorio clienteRepositorio,
                              EmpleadoRepositorio empleadoRepositorio) {
        this.usuarioRolRepositorio = usuarioRolRepositorio;
        this.usuarioRepositorio = usuarioRepositorio;
        this.rolRepositorio = rolRepositorio;
        this.clienteRepositorio = clienteRepositorio;
        this.empleadoRepositorio = empleadoRepositorio;
    }

    @Transactional(readOnly = true)
    public UsuarioRolRespuesta consultarRolPorUsuarioId(Long usuarioId) {
        if (usuarioId == null || usuarioId <= 0) {
            throw new IllegalArgumentException("El identificador del usuario debe ser mayor que cero.");
        }

        if (!usuarioRepositorio.existsById(usuarioId)) {
            throw new RecursoNoEncontradoException("El usuario no existe.");
        }

        Optional<UsuarioRol> usuarioRol = usuarioRolRepositorio.findOneByUsuario_Id(usuarioId);
        if (usuarioRol.isEmpty()) {
            throw new RecursoNoEncontradoException("El usuario no tiene un rol asignado.");
        }

        return UsuarioRolRespuesta.de(usuarioRol.get());
    }

    @Transactional
    public UsuarioRolRespuesta asignarRolUsuario(Long usuarioId, Long rolId) {
        if (usuarioId == null || usuarioId <= 0) {
            throw new IllegalArgumentException("El identificador del usuario debe ser mayor que cero.");
        }
        if (rolId == null || rolId <= 0) {
            throw new IllegalArgumentException("El identificador del rol debe ser mayor que cero.");
        }

        Usuario usuario = usuarioRepositorio.findById(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("El usuario no existe."));

        Rol rol = rolRepositorio.findById(rolId)
                .orElseThrow(() -> new RecursoNoEncontradoException("El rol no existe."));

        if (usuarioRolRepositorio.existsByUsuario_Id(usuarioId)) {
            throw new ConflictoException("El usuario ya tiene un rol asignado.");
        }

        if (!rol.getActivo()) {
            throw new OperacionRechazadaException("El rol está inactivo.");
        }

        if ("CLIENTE".equals(rol.getCodigo()) && !clienteRepositorio.existsByUsuario_Id(usuarioId)) {
            throw new OperacionRechazadaException("El usuario debe tener un cliente asociado para asignar el rol CLIENTE.");
        }

        if ("ASESOR".equals(rol.getCodigo()) && !empleadoRepositorio.existsByUsuario_Id(usuarioId)) {
            throw new OperacionRechazadaException("El usuario debe tener un empleado asociado para asignar el rol ASESOR.");
        }

        UsuarioRol asignacion = usuarioRolRepositorio.save(new UsuarioRol(usuario, rol));
        return UsuarioRolRespuesta.de(asignacion);
    }

    @Transactional
    public void retirarRolUsuario(Long usuarioId, Long rolId) {
        if (usuarioId == null || usuarioId <= 0) {
            throw new IllegalArgumentException("El identificador del usuario debe ser mayor que cero.");
        }
        if (rolId == null || rolId <= 0) {
            throw new IllegalArgumentException("El identificador del rol debe ser mayor que cero.");
        }

        if (!usuarioRepositorio.existsById(usuarioId)) {
            throw new RecursoNoEncontradoException("El usuario no existe.");
        }

        Optional<UsuarioRol> usuarioRol = usuarioRolRepositorio.findOneByUsuario_Id(usuarioId);
        if (usuarioRol.isEmpty()) {
            throw new RecursoNoEncontradoException("El usuario no tiene un rol asignado.");
        }
        if (!rolId.equals(usuarioRol.get().getRol().getId())) {
            throw new RecursoNoEncontradoException("El usuario no tiene asignado el rol indicado.");
        }
        usuarioRolRepositorio.delete(usuarioRol.get());
    }
}
