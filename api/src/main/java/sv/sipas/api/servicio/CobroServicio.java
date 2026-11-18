package sv.sipas.api.servicio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import sv.sipas.api.dto.CobroRespuesta;

import java.math.BigDecimal;

@Service
public class CobroServicio {

    private static final Logger log = LoggerFactory.getLogger(CobroServicio.class);

    private final FacturaServicio facturaServicio;
    private final BitacoraOperacionServicio bitacoraOperacionServicio;

    public CobroServicio(FacturaServicio facturaServicio, BitacoraOperacionServicio bitacoraOperacionServicio) {
        this.facturaServicio = facturaServicio;
        this.bitacoraOperacionServicio = bitacoraOperacionServicio;
    }

    public CobroRespuesta cobrarFactura(Long usuarioId, Long facturaId,
                                        Long sesionCajaId, BigDecimal efectivoRecibido) {
        try {
            CobroRespuesta respuesta = facturaServicio.cobrarFactura(
                    usuarioId, facturaId, sesionCajaId, efectivoRecibido);

            log.info("Cobro completado: usuarioId={}, facturaId={}, sesionCajaId={}, comprobante={}",
                    usuarioId, facturaId, sesionCajaId, respuesta.numeroComprobante());

            return respuesta;
        } catch (RuntimeException errorCobro) {
            log.warn("Cobro rechazado: usuarioId={}, facturaId={}, sesionCajaId={}, causa={}",
                    usuarioId, facturaId, sesionCajaId, errorCobro.getMessage());
            try {
                bitacoraOperacionServicio.registrarFallo(usuarioId, facturaId, errorCobro.getMessage());
            } catch (RuntimeException errorBitacora) {
                errorCobro.addSuppressed(errorBitacora);
            }
            throw errorCobro;
        }
    }
}
