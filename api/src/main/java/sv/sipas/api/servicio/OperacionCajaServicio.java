package sv.sipas.api.servicio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import sv.sipas.api.dto.ResumenSesionCajaRespuesta;
import sv.sipas.api.dto.RetiroRespuesta;
import sv.sipas.api.entidad.enums.TipoOperacion;

import java.math.BigDecimal;

@Service
public class OperacionCajaServicio {

    private static final Logger log = LoggerFactory.getLogger(OperacionCajaServicio.class);

    private final SesionCajaServicio sesionCajaServicio;
    private final MovimientoCajaServicio movimientoCajaServicio;
    private final BitacoraOperacionServicio bitacoraOperacionServicio;

    public OperacionCajaServicio(SesionCajaServicio sesionCajaServicio,
                                 MovimientoCajaServicio movimientoCajaServicio,
                                 BitacoraOperacionServicio bitacoraOperacionServicio) {
        this.sesionCajaServicio = sesionCajaServicio;
        this.movimientoCajaServicio = movimientoCajaServicio;
        this.bitacoraOperacionServicio = bitacoraOperacionServicio;
    }

    public Long abrirCaja(Long cajaId, Long usuarioId, BigDecimal fondoInicial) {
        try {
            Long respuesta = sesionCajaServicio.abrirCaja(cajaId, usuarioId, fondoInicial);

            log.info("Caja abierta: usuarioId={}, cajaId={}, sesionCajaId={}",
                    usuarioId, cajaId, respuesta);

            return respuesta;
        } catch (RuntimeException error) {
            registrarFallo(usuarioId, TipoOperacion.APERTURA_CAJA, cajaId, error);
            log.warn("Caja abierta rechazada: usuarioId={}, cajaId={}, fondoInicial={}, causa={}",
                    usuarioId, cajaId, fondoInicial, error.getMessage());
            throw error;
        }
    }

    public ResumenSesionCajaRespuesta cerrarCaja(Long sesionCajaId,
                                                 BigDecimal efectivoContado, Long usuarioId) {
        try {
            ResumenSesionCajaRespuesta respuesta = sesionCajaServicio.cerrarCaja(sesionCajaId, efectivoContado, usuarioId);

            log.info("Caja cerrada: usuarioId={} sesionCajaId={} diferencia={}",
                    usuarioId, sesionCajaId, respuesta.diferencia());

            return respuesta;
        } catch (RuntimeException error) {
            registrarFallo(usuarioId, TipoOperacion.CIERRE_CAJA, sesionCajaId, error);
            log.warn("Caja cerrada rechazada: sesionCajaId={}, efectivoContado={}, usuarioId={}, causa={}",
                    sesionCajaId, efectivoContado, usuarioId, error.getMessage());
            throw error;
        }
    }

    public RetiroRespuesta retirar(Long usuarioId, Long sesionCajaId,
                                   BigDecimal monto, String motivo) {
        try {
            RetiroRespuesta respuesta = movimientoCajaServicio.retirar(usuarioId, sesionCajaId, monto, motivo);

            log.info("Retiro completado: usuarioId={} sesionCajaId={} movimientoId={}",
                    usuarioId, sesionCajaId, respuesta.idMovimiento());

            return respuesta;
        } catch (RuntimeException error) {
            registrarFallo(usuarioId, TipoOperacion.RETIRO_EFECTIVO, sesionCajaId, error);
            log.warn("Retiro rechazado: usuarioId={}, sesionCajaId={}, monto={}, causa={}",
                    usuarioId, sesionCajaId, monto, error.getMessage());
            throw error;
        }
    }

    private void registrarFallo(Long usuarioId, TipoOperacion tipoOperacion,
                                Long registroId, RuntimeException errorOriginal) {
        try {
            bitacoraOperacionServicio.registrarFalloCaja(
                    usuarioId, tipoOperacion, registroId, errorOriginal.getMessage());
        } catch (RuntimeException errorBitacora) {
            errorOriginal.addSuppressed(errorBitacora);
        }
    }
}
