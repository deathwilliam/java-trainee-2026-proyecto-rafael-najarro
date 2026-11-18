package sv.sipas.api.controlador;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sv.sipas.api.dto.ServicioModificacionSolicitud;
import sv.sipas.api.dto.ServicioRespuesta;
import sv.sipas.api.dto.ServicioSolicitud;
import sv.sipas.api.servicio.ServicioServicio;

import java.util.List;

@Tag(name = "Servicios", description = "Administración de servicios y consulta por proveedor")
@RestController
@RequestMapping("/api/servicios")
@PreAuthorize("hasRole('ADMIN')")
public class ServicioControlador {

    private final ServicioServicio servicioServicio;

    public ServicioControlador(ServicioServicio servicioServicio) {
        this.servicioServicio = servicioServicio;
    }

    @Operation(summary = "Listar servicios activos por proveedor",
            description = "200 con la lista; " +
                    "400 si el identificador es inválido o el proveedor está inactivo; " +
                    "404 si el proveedor no existe")
    @GetMapping
    public List<ServicioRespuesta> listarServiciosActivosPorProveedor(@RequestParam(name = "proveedorId") Long proveedorId) {
        return servicioServicio.listarServiciosActivosPorProveedor(proveedorId);
    }

    @Operation(summary = "Consultar un servicio",
            description = "200 con el servicio; " +
                    "400 si el identificador es inválido; " +
                    "404 si no existe")
    @GetMapping("/{id}")
    public ServicioRespuesta consultarServicio(@PathVariable Long id) {
        return servicioServicio.consultarServicio(id);
    }

    @Operation(summary = "Crear servicio",
            description = "201 con el servicio; " +
                    "400 si los datos son inválidos o el proveedor está inactivo; " +
                    "404 si el proveedor no existe; " +
                    "409 si el código ya existe para el proveedor")
    @PostMapping
    public ResponseEntity<ServicioRespuesta> crearServicio(@Valid @RequestBody ServicioSolicitud solicitud) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(servicioServicio.crearServicio(solicitud));
    }

    @Operation(summary = "Modificar servicio",
            description = "200 con el servicio; " +
                    "400 si los datos son inválidos o no puede activarse; " +
                    "404 si el servicio no existe")
    @PatchMapping("/{id}")
    public ServicioRespuesta modificarServicio(@PathVariable Long id,
                                               @Valid @RequestBody ServicioModificacionSolicitud solicitud) {
        return servicioServicio.modificarServicio(id, solicitud);
    }

    @Operation(summary = "Desactivar servicio",
            description = "204 sin cuerpo; " +
                    "400 si el identificador es inválido; " +
                    "404 si no existe")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivarServicio(@PathVariable Long id) {
        servicioServicio.desactivarServicio(id);
        return ResponseEntity.noContent().build();
    }
}
