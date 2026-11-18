package sv.sipas.api.controlador;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sv.sipas.api.dto.EmpleadoModificacionSolicitud;
import sv.sipas.api.dto.EmpleadoRespuesta;
import sv.sipas.api.dto.EmpleadoSolicitud;
import sv.sipas.api.servicio.EmpleadoServicio;

import java.util.List;

@Tag(name = "Empleados", description = "Administración de empleados")
@RestController
@RequestMapping("/api/empleados")
@PreAuthorize("hasRole('ADMIN')")
public class EmpleadoControlador {

    private final EmpleadoServicio empleadoServicio;

    public EmpleadoControlador(EmpleadoServicio empleadoServicio) {
        this.empleadoServicio = empleadoServicio;
    }

    @Operation(summary = "Listar empleados",
            description = "200 con la lista; [] si no hay resultados")
    @GetMapping
    public List<EmpleadoRespuesta> listarEmpleados() {
        return empleadoServicio.listarEmpleados();
    }

    @Operation(summary = "Consultar un empleado",
            description = "200 con el empleado; " +
                    "400 si el identificador es inválido; " +
                    "404 si no existe")
    @GetMapping("/{id}")
    public EmpleadoRespuesta consultarEmpleado(@PathVariable Long id) {
        return empleadoServicio.consultarEmpleado(id);
    }

    @Operation(summary = "Crear empleado",
            description = "201 con el empleado; " +
                    "400 si los datos son inválidos o el usuario o la sucursal están inactivos; " +
                    "404 si no existen; " +
                    "409 si el código o la asociación con el usuario ya existen")
    @PostMapping
    public ResponseEntity<EmpleadoRespuesta> crearEmpleado(@Valid @RequestBody EmpleadoSolicitud solicitud) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(empleadoServicio.crearEmpleado(solicitud));
    }

    @Operation(summary = "Modificar empleado",
            description = "200 con el empleado; " +
                    "400 si los datos son inválidos o no puede activarse; " +
                    "404 si no existe; " +
                    "409 si se intenta desactivar con una sesión abierta")
    @PatchMapping("/{id}")
    public EmpleadoRespuesta modificarEmpleado(@PathVariable Long id,
                                               @Valid @RequestBody EmpleadoModificacionSolicitud solicitud) {
        return empleadoServicio.modificarEmpleado(id, solicitud);
    }

    @Operation(summary = "Desactivar empleado",
            description = "204 sin cuerpo; " +
                    "400 si el identificador es inválido; " +
                    "404 si no existe; 409 si tiene una sesión abierta")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivarEmpleado(@PathVariable Long id) {
        empleadoServicio.desactivarEmpleado(id);
        return ResponseEntity.noContent().build();
    }
}
