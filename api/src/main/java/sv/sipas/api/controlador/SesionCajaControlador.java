package sv.sipas.api.controlador;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import sv.sipas.api.dto.*;
import sv.sipas.api.entidad.enums.EstadoSesionCaja;
import sv.sipas.api.servicio.MovimientoCajaServicio;
import sv.sipas.api.servicio.OperacionCajaServicio;
import sv.sipas.api.servicio.SesionCajaServicio;
import sv.sipas.api.servicio.UsuarioServicio;

import java.util.List;

@Tag(name = "Sesiones de caja", description = "Consulta, cierre y retiros de sesiones de caja")
@RestController
@RequestMapping("/api/sesiones-caja")
public class SesionCajaControlador {

    private final SesionCajaServicio sesionCajaServicio;
    private final MovimientoCajaServicio movimientoCajaServicio;
    private final UsuarioServicio usuarioServicio;
    private final OperacionCajaServicio operacionCajaServicio;

    public SesionCajaControlador(SesionCajaServicio sesionCajaServicio,
                                 MovimientoCajaServicio movimientoCajaServicio,
                                 UsuarioServicio usuarioServicio,
                                 OperacionCajaServicio operacionCajaServicio) {
        this.sesionCajaServicio = sesionCajaServicio;
        this.movimientoCajaServicio = movimientoCajaServicio;
        this.usuarioServicio = usuarioServicio;
        this.operacionCajaServicio = operacionCajaServicio;
    }

    @Operation(summary = "Consultar el resumen de una sesión",
            description = "200 con el resumen; " +
                    "400 si el identificador es inválido; " +
                    "403 si no tiene acceso; " +
                    "404 si la sesión no existe")
    @GetMapping("/{sesionCajaId}/resumen")
    public ResumenSesionCajaRespuesta consultarResumen(@PathVariable Long sesionCajaId,
                                                       @AuthenticationPrincipal Jwt token) {
        Long usuarioId = usuarioServicio.obtenerIdUsuarioActivo(token.getSubject());
        return sesionCajaServicio.consultarResumen(sesionCajaId, usuarioId);
    }

    @Operation(
            summary = "Retirar efectivo de una sesión de caja",
            description = "201 con el retiro registrado; " +
                    "400 si los datos son inválidos, " +
                    "la sesión no existe o está cerrada, el efectivo disponible es insuficiente " +
                    "o el usuario no tiene una asignación ADMIN activa; " +
                    "403 si el token no tiene el rol ADMIN")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{sesionCajaId}/retiros")
    public ResponseEntity<RetiroRespuesta> retirar(@PathVariable Long sesionCajaId,
                                                   @Valid @RequestBody RetiroSolicitud retiroSolicitud,
                                                   @AuthenticationPrincipal Jwt token) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                operacionCajaServicio.retirar(
                        usuarioServicio.obtenerIdUsuarioActivo(token.getSubject()),
                        sesionCajaId,
                        retiroSolicitud.monto(),
                        retiroSolicitud.motivo())
        );
    }

    @Operation(
            summary = "Cerrar una sesión de caja",
            description = "200 con el cuadre de la sesión; " +
                    "400 si los datos son inválidos o ya está cerrada; " +
                    "403 si pertenece a otro usuario; " +
                    "404 si no existe")
    @PreAuthorize("hasRole('ASESOR')")
    @PostMapping("/{sesionCajaId}/cierre")
    public ResponseEntity<ResumenSesionCajaRespuesta> cerrarCaja(@PathVariable Long sesionCajaId,
                                                                 @Valid @RequestBody CierreCajaSolicitud solicitud,
                                                                 @AuthenticationPrincipal Jwt token) {
        Long usuarioId = usuarioServicio.obtenerIdUsuarioActivo(token.getSubject());
        return ResponseEntity.ok(operacionCajaServicio.cerrarCaja(
                sesionCajaId, solicitud.efectivoContado(), usuarioId));
    }

    @Operation(summary = "Consultar movimientos de una sesión de caja",
            description = "200 con la lista; " +
                    "400 si el identificador es inválido; " +
                    "403 si no tiene acceso; " +
                    "404 si la sesión no existe")
    @GetMapping("/{sesionCajaId}/movimientos")
    public List<MovimientoCajaRespuesta> listarPorSesion(@PathVariable Long sesionCajaId,
                                                         @AuthenticationPrincipal Jwt token) {
        Long usuarioId = usuarioServicio.obtenerIdUsuarioActivo(token.getSubject());
        return movimientoCajaServicio.listarPorSesion(sesionCajaId, usuarioId);
    }

    @Operation(summary = "Listar sesiones con filtros opcionales por empleado y estado",
            description = "200 con la lista; el ASESOR consulta sus propias sesiones; " +
                    "400 si los filtros son inválidos; " +
                    "403 si no tiene acceso; " +
                    "404 si el ADMIN indica un empleado inexistente")
    @GetMapping
    public List<SesionCajaRespuesta> consultarSesionPorEmpleado(@RequestParam(name = "empleadoId", required = false) Long empleadoId,
                                                                @RequestParam(name = "estado", required = false) EstadoSesionCaja estado,
                                                                @AuthenticationPrincipal Jwt token) {
        Long usuarioId = usuarioServicio.obtenerIdUsuarioActivo(token.getSubject());
        return sesionCajaServicio.consultarSesionPorEmpleado(empleadoId, estado, usuarioId);
    }

    @Operation(summary = "Consultar los datos de una sesión",
            description = "200 con la sesión; " +
                    "400 si el identificador es inválido; " +
                    "403 si no tiene acceso; " +
                    "404 si la sesión no existe")
    @GetMapping("/{id}")
    public SesionCajaRespuesta consultarSesionPorId(@PathVariable Long id,
                                                    @AuthenticationPrincipal Jwt token) {
        return sesionCajaServicio.consultarSesionPorId(id, usuarioServicio.obtenerIdUsuarioActivo(token.getSubject()));
    }
}
