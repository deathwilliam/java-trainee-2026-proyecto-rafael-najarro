package sv.sipas.api.controlador;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sv.sipas.api.dto.RolRespuesta;
import sv.sipas.api.servicio.RolServicio;

import java.util.List;

@Tag(name = "Roles", description = "Consulta del catálogo de roles del sistema")
@RestController
@RequestMapping("/api/roles")
@PreAuthorize("hasRole('ADMIN')")
public class RolControlador {

    private final RolServicio rolServicio;

    public RolControlador(RolServicio rolServicio) {
        this.rolServicio = rolServicio;
    }

    @Operation(
            summary = "Listar roles del sistema",
            description = "200 con la lista de roles activos e inactivos; [] si no hay resultados")
    @GetMapping
    public List<RolRespuesta> listarUsuarioRoles() {
        return rolServicio.listarRoles();
    }
}
