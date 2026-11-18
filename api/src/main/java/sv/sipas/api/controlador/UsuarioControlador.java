package sv.sipas.api.controlador;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sv.sipas.api.dto.*;
import sv.sipas.api.servicio.UsuarioRolServicio;
import sv.sipas.api.servicio.UsuarioServicio;

import java.util.List;

@Tag(name = "Usuarios", description = "Administración de usuarios y asignación de roles")
@RestController
@RequestMapping("/api/usuarios")
@PreAuthorize("hasRole('ADMIN')")
public class UsuarioControlador {

    private final UsuarioServicio usuarioServicio;
    private final UsuarioRolServicio usuarioRolServicio;

    public UsuarioControlador(UsuarioServicio usuarioServicio, UsuarioRolServicio usuarioRolServicio) {
        this.usuarioServicio = usuarioServicio;
        this.usuarioRolServicio = usuarioRolServicio;
    }

    @Operation(
            summary = "Listar usuarios",
            description = "200 con la lista; [] si no hay resultados")
    @GetMapping
    public List<UsuarioRespuesta> listarUsuarios() {
        return usuarioServicio.listarUsuarios();
    }

    @Operation(
            summary = "Consultar un usuario",
            description = "200 con el usuario; " +
                    "400 si el identificador es inválido; " +
                    "404 si no existe")
    @GetMapping("/{id}")
    public UsuarioRespuesta consultarUsuarioPorId(@PathVariable Long id) {
        return usuarioServicio.consultarUsuarioPorId(id);
    }

    @Operation(
            summary = "Crear usuario",
            description = "201 con el usuario; " +
                    "400 si los datos son inválidos; " +
                    "409 si el nombre ya existe")
    @PostMapping
    public ResponseEntity<UsuarioRespuesta> crearUsuario(@Valid @RequestBody UsuarioSolicitud usuarioSolicitud) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioServicio.crearUsuario(
                usuarioSolicitud.nombreUsuario(), usuarioSolicitud.clave()
        ));
    }

    @Operation(
            summary = "Editar usuario",
            description = "200 con el usuario; permite modificar nombre, clave o estado; " +
                    "400 si no se proporciona ningún campo para modificar o los datos son inválidos; " +
                    "404 si no existe; " +
                    "409 si el nombre pertenece a otro usuario")
    @PatchMapping("/{id}")
    public ResponseEntity<UsuarioRespuesta> modificarUsuario(@PathVariable Long id,
                                                             @Valid @RequestBody UsuarioModificacionSolicitud solicitud) {
        return ResponseEntity.ok(usuarioServicio.modificarUsuario(
                id, solicitud.nombreUsuario(), solicitud.clave(), solicitud.activo()
        ));
    }

    @Operation(
            summary = "Desactivar usuario",
            description = "204 sin cuerpo; " +
                    "400 si el identificador es inválido; " +
                    "404 si no existe")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivarUsuario(@PathVariable Long id) {
        usuarioServicio.desactivarUsuario(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Consultar rol de un usuario",
            description = "200 con el rol del usuario; " +
                    "400 si el identificador es inválido; " +
                    "404 si no existe o no tiene rol")
    @GetMapping("/{id}/roles")
    public UsuarioRolRespuesta consultarUsuarioRolesPorId(@PathVariable Long id) {
        return usuarioRolServicio.consultarRolPorUsuarioId(id);
    }

    @Operation(
            summary = "Asignar rol a usuario",
            description = "201 con la asignación creada; "
                    + "400 si los identificadores son inválidos, el rol está inactivo o falta el cliente o empleado asociado requerido; "
                    + "404 si el usuario o el rol no existen; "
                    + "409 si el usuario ya tiene un rol asignado")
    @PostMapping("/{id}/roles")
    public ResponseEntity<UsuarioRolRespuesta> asignarRolUsuario(@PathVariable Long id,
                                                                 @Valid @RequestBody RolUsuarioSolicitud solicitud) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioRolServicio.asignarRolUsuario(id, solicitud.rolId()));
    }

    @Operation(
            summary = "Retirar rol a usuario",
            description = "204 sin cuerpo cuando se retira la asignación; " +
                    "400 si los identificadores son inválidos; " +
                    "404 si el usuario no existe, no tiene rol asignado o no tiene asignado el rol indicado")
    @DeleteMapping("/{id}/roles/{rolId}")
    public ResponseEntity<Void> retirarRolUsuario(@PathVariable Long id,
                                                  @PathVariable Long rolId) {
        usuarioRolServicio.retirarRolUsuario(id, rolId);
        return ResponseEntity.noContent().build();
    }
}
