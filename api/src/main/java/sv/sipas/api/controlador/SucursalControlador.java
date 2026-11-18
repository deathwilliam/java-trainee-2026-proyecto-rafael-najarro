package sv.sipas.api.controlador;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sv.sipas.api.dto.SucursalModificacionSolicitud;
import sv.sipas.api.dto.SucursalRespuesta;
import sv.sipas.api.dto.SucursalSolicitud;
import sv.sipas.api.servicio.SucursalServicio;

import java.util.List;

@Tag(name = "Sucursales", description = "Administración de sucursales")
@RestController
@RequestMapping("/api/sucursales")
@PreAuthorize("hasRole('ADMIN')")
public class SucursalControlador {

    private final SucursalServicio sucursalServicio;

    public SucursalControlador(SucursalServicio sucursalServicio) {
        this.sucursalServicio = sucursalServicio;
    }

    @Operation(summary = "Listar sucursales",
            description = "200 con la lista; [] si no hay resultados")
    @GetMapping
    public List<SucursalRespuesta> listarSucursales() {
        return sucursalServicio.listarSucursales();
    }

    @Operation(summary = "Consultar una sucursal",
            description = "200 con la sucursal; " +
                    "400 si el identificador es inválido; " +
                    "404 si no existe")
    @GetMapping("/{id}")
    public SucursalRespuesta consultarSucursal(@PathVariable Long id) {
        return sucursalServicio.consultarSucursal(id);
    }

    @Operation(summary = "Crear sucursal",
            description = "201 con la sucursal; " +
                    "400 si los datos son inválidos; " +
                    "409 si el código ya existe")
    @PostMapping
    public ResponseEntity<SucursalRespuesta> crearSucursal(@Valid @RequestBody SucursalSolicitud solicitud) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(sucursalServicio.crearSucursal(solicitud));
    }

    @Operation(summary = "Modificar sucursal",
            description = "200 con la sucursal; " +
                    "400 si los datos son inválidos; " +
                    "404 si no existe; " +
                    "409 si se intenta desactivar con sesiones abiertas")
    @PatchMapping("/{id}")
    public SucursalRespuesta modificarSucursal(@PathVariable Long id,
                                               @Valid @RequestBody SucursalModificacionSolicitud solicitud) {
        return sucursalServicio.modificarSucursal(id, solicitud);
    }

    @Operation(summary = "Desactivar sucursal",
            description = "204 sin cuerpo; " +
                    "400 si el identificador es inválido; " +
                    "404 si no existe; " +
                    "409 si tiene sesiones abiertas")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivarSucursal(@PathVariable Long id) {
        sucursalServicio.desactivarSucursal(id);
        return ResponseEntity.noContent().build();
    }
}
