package sv.sipas.api.dto;

import sv.sipas.api.entidad.BitacoraOperacion;
import sv.sipas.api.entidad.enums.ResultadoOperacion;
import sv.sipas.api.entidad.enums.TipoOperacion;

import java.time.LocalDateTime;

public record BitacoraOperacionRespuesta(Long id, Long usuarioId, TipoOperacion tipoOperacion,
                                         String entidadAfectada, Long registroAfectadoId, String descripcion,
                                         ResultadoOperacion resultado, LocalDateTime fechaHora) {

    public static BitacoraOperacionRespuesta de(BitacoraOperacion bitacora) {
        return new BitacoraOperacionRespuesta(
                bitacora.getId(),
                bitacora.getUsuario().getId(),
                bitacora.getTipoOperacion(),
                bitacora.getEntidadAfectada(),
                bitacora.getRegistroAfectadoId(),
                bitacora.getDescripcion(),
                bitacora.getResultado(),
                bitacora.getFechaHora()
        );
    }
}
