package sv.sipas.api.controlador;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sv.sipas.api.dto.BitacoraOperacionRespuesta;
import sv.sipas.api.entidad.enums.ResultadoOperacion;
import sv.sipas.api.entidad.enums.TipoOperacion;
import sv.sipas.api.servicio.BitacoraOperacionServicio;

import java.util.List;

@Tag(name = "Bitácora de operaciones", description = "Consulta del historial de operaciones y sus resultados")
@RestController
@RequestMapping("/api/bitacora")
@PreAuthorize("hasRole('ADMIN')")
public class BitacoraOperacionControlador {

    private final BitacoraOperacionServicio bitacoraOperacionServicio;

    public BitacoraOperacionControlador(BitacoraOperacionServicio bitacoraOperacionServicio) {
        this.bitacoraOperacionServicio = bitacoraOperacionServicio;
    }

    @Operation(summary = "Listar bitácoras con filtros opcionales por usuario, tipo de operación y resultado",
            description = "200 con la lista; [] si no hay resultados; " +
                    "400 si los filtros son inválidos; " +
                    "404 si el usuario indicado no existe")
    @GetMapping
    public List<BitacoraOperacionRespuesta> listarBitacoras(@RequestParam(name = "usuarioId", required = false) Long usuarioId,
                                                            @RequestParam(name = "tipoOperacion", required = false) TipoOperacion tipoOperacion,
                                                            @RequestParam(name = "resultado", required = false) ResultadoOperacion resultado) {
        return bitacoraOperacionServicio.listarBitacoras(usuarioId, tipoOperacion, resultado);
    }

    @Operation(summary = "Consultar una bitácora por identificador",
            description = "200 con la bitácora; " +
                    "400 si el identificador es inválido; " +
                    "404 si no existe")
    @GetMapping("/{id}")
    public BitacoraOperacionRespuesta consultarBitacoraOperacion(@PathVariable Long id) {
        return bitacoraOperacionServicio.consultarBitacoraOperacion(id);
    }
}
