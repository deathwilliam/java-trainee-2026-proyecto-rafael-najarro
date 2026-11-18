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
import sv.sipas.api.dto.AperturaCajaRespuesta;
import sv.sipas.api.dto.AperturaCajaSolicitud;
import sv.sipas.api.dto.CajaModificacionSolicitud;
import sv.sipas.api.dto.CajaRespuesta;
import sv.sipas.api.dto.CajaSolicitud;
import sv.sipas.api.servicio.CajaServicio;
import sv.sipas.api.servicio.OperacionCajaServicio;
import sv.sipas.api.servicio.UsuarioServicio;

import java.util.List;

@Tag(name = "Cajas", description = "Administración de cajas y apertura de sesiones")
@RestController
@RequestMapping("/api/cajas")
public class CajaControlador {

    private final OperacionCajaServicio operacionCajaServicio;
    private final UsuarioServicio usuarioServicio;
    private final CajaServicio cajaServicio;

    public CajaControlador(OperacionCajaServicio operacionCajaServicio,
                           UsuarioServicio usuarioServicio,
                           CajaServicio cajaServicio) {
        this.operacionCajaServicio = operacionCajaServicio;
        this.usuarioServicio = usuarioServicio;
        this.cajaServicio = cajaServicio;
    }

    @Operation(summary = "Listar cajas",
            description = "200 con la lista; [] si no hay resultados")
    @GetMapping
    public List<CajaRespuesta> listarCajas() {
        return cajaServicio.listarCajas();
    }

    @Operation(summary = "Consultar una caja",
            description = "200 con la caja; " +
                    "400 si el identificador es inválido; " +
                    "404 si no existe")
    @GetMapping("/{id}")
    public CajaRespuesta consultarCaja(@PathVariable Long id) {
        return cajaServicio.consultarCaja(id);
    }

    @Operation(summary = "Crear caja",
            description = "201 con la caja; " +
                    "400 si los datos son inválidos o la sucursal está inactiva; " +
                    "404 si la sucursal no existe; " +
                    "409 si el código ya existe en la sucursal")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<CajaRespuesta> crearCaja(@Valid @RequestBody CajaSolicitud solicitud) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cajaServicio.crearCaja(solicitud));
    }

    @Operation(summary = "Modificar caja",
            description = "200 con la caja; " +
                    "400 si los datos son inválidos o no puede activarse; " +
                    "404 si no existe; " +
                    "409 si se intenta desactivar con una sesión abierta")
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}")
    public CajaRespuesta modificarCaja(@PathVariable Long id,
                                       @Valid @RequestBody CajaModificacionSolicitud solicitud) {
        return cajaServicio.modificarCaja(id, solicitud);
    }

    @Operation(summary = "Desactivar caja",
            description = "204 sin cuerpo; " +
                    "400 si el identificador es inválido; " +
                    "404 si no existe; " +
                    "409 si tiene una sesión abierta")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivarCaja(@PathVariable Long id) {
        cajaServicio.desactivarCaja(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Abrir una sesión de caja",
            description = "201 con el identificador de la sesión creada; "
                    + "400 si los datos son inválidos, la caja no existe, el usuario no tiene un empleado "
                    + "asociado, la caja o el empleado no están habilitados, pertenecen a distintas sucursales "
                    + "o ya tienen una sesión abierta; " +
                    "403 si no tiene el rol ASESOR")
    @PreAuthorize("hasRole('ASESOR')")
    @PostMapping("/{cajaId}/sesiones")
    public ResponseEntity<AperturaCajaRespuesta> abrirCaja(@PathVariable Long cajaId,
                                                           @Valid @RequestBody AperturaCajaSolicitud solicitud,
                                                           @AuthenticationPrincipal Jwt token) {
        Long usuarioId = usuarioServicio.obtenerIdUsuarioActivo(token.getSubject());
        Long sesionCajaId = operacionCajaServicio.abrirCaja(cajaId, usuarioId, solicitud.fondoInicial());
        return ResponseEntity.status(HttpStatus.CREATED).body(new AperturaCajaRespuesta(sesionCajaId));
    }
}
