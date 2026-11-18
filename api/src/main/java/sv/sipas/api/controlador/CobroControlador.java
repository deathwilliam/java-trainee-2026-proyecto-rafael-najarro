package sv.sipas.api.controlador;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sv.sipas.api.dto.CobroRespuesta;
import sv.sipas.api.dto.CobroSolicitud;
import sv.sipas.api.servicio.CobroServicio;
import sv.sipas.api.servicio.UsuarioServicio;

@Tag(name = "Cobros", description = "Cobro de facturas de servicios y generación de comprobantes")
@RestController
@RequestMapping("/api/cobros")
public class CobroControlador {

    private final CobroServicio cobroServicio;
    private final UsuarioServicio usuarioServicio;


    public CobroControlador(CobroServicio cobroServicio, UsuarioServicio usuarioServicio) {
        this.cobroServicio = cobroServicio;
        this.usuarioServicio = usuarioServicio;
    }

    @Operation(summary = "Cobrar una factura pendiente",
            description = "201 con el comprobante del cobro; " +
                    "400 si los datos son inválidos o se rechaza una regla de cobro; " +
                    "401 sin autenticación; " +
                    "403 si no tiene el rol ASESOR; 409 si existe un conflicto con los datos")
    @PreAuthorize("hasRole('ASESOR')")
    @PostMapping
    public ResponseEntity<CobroRespuesta> cobrarFactura(@Valid @RequestBody CobroSolicitud cobroSolicitud,
                                                        @AuthenticationPrincipal Jwt token) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                cobroServicio.cobrarFactura(
                        usuarioServicio.obtenerIdUsuarioActivo(token.getSubject()),
                        cobroSolicitud.facturaId(),
                        cobroSolicitud.sesionCajaId(),
                        cobroSolicitud.efectivoRecibido())
        );
    }
}
