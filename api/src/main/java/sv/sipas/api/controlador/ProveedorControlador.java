package sv.sipas.api.controlador;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sv.sipas.api.dto.ProveedorModificacionSolicitud;
import sv.sipas.api.dto.ProveedorRespuesta;
import sv.sipas.api.dto.ProveedorSolicitud;
import sv.sipas.api.servicio.ProveedorServicio;

import java.util.List;

@Tag(name = "Proveedores", description = "Administración y consulta de proveedores")
@RestController
@RequestMapping("/api/proveedores")
@PreAuthorize("hasRole('ADMIN')")
public class ProveedorControlador {

    private final ProveedorServicio proveedorServicio;

    public ProveedorControlador(ProveedorServicio proveedorServicio) {
        this.proveedorServicio = proveedorServicio;
    }

    @Operation(summary = "Listar proveedores activos",
            description = "200 con la lista; [] si no hay proveedores activos")
    @GetMapping
    public List<ProveedorRespuesta> listarProveedoresActivos() {
        return proveedorServicio.listarProveedoresActivos();
    }

    @Operation(summary = "Consultar un proveedor",
            description = "200 con el proveedor; " +
                    "400 si el identificador es inválido; " +
                    "404 si no existe")
    @GetMapping("/{id}")
    public ProveedorRespuesta consultarProveedor(@PathVariable Long id) {
        return proveedorServicio.consultarProveedor(id);
    }

    @Operation(summary = "Crear proveedor",
            description = "201 con el proveedor; " +
                    "400 si los datos son inválidos; " +
                    "409 si el código ya existe")
    @PostMapping
    public ResponseEntity<ProveedorRespuesta> crearProveedor(@Valid @RequestBody ProveedorSolicitud solicitud) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(proveedorServicio.crearProveedor(solicitud));
    }

    @Operation(summary = "Modificar proveedor",
            description = "200 con el proveedor; " +
                    "400 si los datos son inválidos; " +
                    "404 si no existe")
    @PatchMapping("/{id}")
    public ProveedorRespuesta modificarProveedor(@PathVariable Long id,
                                                 @Valid @RequestBody ProveedorModificacionSolicitud solicitud) {
        return proveedorServicio.modificarProveedor(id, solicitud);
    }

    @Operation(summary = "Desactivar proveedor",
            description = "204 sin cuerpo; " +
                    "400 si el identificador es inválido; " +
                    "404 si no existe")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivarProveedor(@PathVariable Long id) {
        proveedorServicio.desactivarProveedor(id);
        return ResponseEntity.noContent().build();
    }
}
