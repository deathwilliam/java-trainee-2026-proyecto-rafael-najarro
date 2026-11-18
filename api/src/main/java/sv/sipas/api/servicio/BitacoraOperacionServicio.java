package sv.sipas.api.servicio;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import sv.sipas.api.dto.BitacoraOperacionRespuesta;
import sv.sipas.api.entidad.BitacoraOperacion;
import sv.sipas.api.entidad.Usuario;
import sv.sipas.api.entidad.enums.ResultadoOperacion;
import sv.sipas.api.entidad.enums.TipoOperacion;
import sv.sipas.api.excepcion.OperacionRechazadaException;
import sv.sipas.api.excepcion.RecursoNoEncontradoException;
import sv.sipas.api.repositorio.BitacoraOperacionRepositorio;
import sv.sipas.api.repositorio.CajaRepositorio;
import sv.sipas.api.repositorio.FacturaRepositorio;
import sv.sipas.api.repositorio.SesionCajaRepositorio;
import sv.sipas.api.repositorio.UsuarioRepositorio;

import java.util.List;

import static sv.sipas.api.servicio.DatosMantenimiento.validarId;

@Service
public class BitacoraOperacionServicio {

    private final FacturaRepositorio facturaRepositorio;
    private final UsuarioRepositorio usuarioRepositorio;
    private final BitacoraOperacionRepositorio bitacoraOperacionRepositorio;
    private final CajaRepositorio cajaRepositorio;
    private final SesionCajaRepositorio sesionCajaRepositorio;

    public BitacoraOperacionServicio(FacturaRepositorio facturaRepositorio,
                                     UsuarioRepositorio usuarioRepositorio,
                                     BitacoraOperacionRepositorio bitacoraOperacionRepositorio,
                                     CajaRepositorio cajaRepositorio,
                                     SesionCajaRepositorio sesionCajaRepositorio) {
        this.facturaRepositorio = facturaRepositorio;
        this.usuarioRepositorio = usuarioRepositorio;
        this.bitacoraOperacionRepositorio = bitacoraOperacionRepositorio;
        this.cajaRepositorio = cajaRepositorio;
        this.sesionCajaRepositorio = sesionCajaRepositorio;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarFallo(Long usuarioId, Long facturaId, String descripcion) {
        validarId(usuarioId, "usuario");
        validarId(facturaId, "factura");

        if (!facturaRepositorio.existsById(facturaId)) {
            return;
        }

        Usuario usuario = usuarioRepositorio.findById(usuarioId)
                .orElseThrow(() -> new OperacionRechazadaException("El usuario no existe."));

        String detalle = descripcion == null || descripcion.isBlank()
                ? "No se pudo completar el cobro."
                : descripcion;

        if (detalle.length() > 250) {
            detalle = detalle.substring(0, 250);
        }

        bitacoraOperacionRepositorio.save(
                BitacoraOperacion.registrar(
                        usuario,
                        TipoOperacion.COBRO_FACTURA,
                        "factura",
                        facturaId,
                        detalle,
                        ResultadoOperacion.FALLIDO));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarFalloCaja(Long usuarioId, TipoOperacion tipoOperacion,
                                   Long registroId, String descripcion) {
        validarId(usuarioId, "usuario");
        validarId(registroId, "registro afectado");

        String entidadAfectada;
        if (tipoOperacion == TipoOperacion.APERTURA_CAJA) {
            if (!cajaRepositorio.existsById(registroId)) {
                return;
            }
            entidadAfectada = "caja";
        } else if (tipoOperacion == TipoOperacion.CIERRE_CAJA
                || tipoOperacion == TipoOperacion.RETIRO_EFECTIVO) {
            if (!sesionCajaRepositorio.existsById(registroId)) {
                return;
            }
            entidadAfectada = "sesion_caja";
        } else {
            throw new IllegalArgumentException("El tipo de operación no corresponde a una operación de caja.");
        }

        Usuario usuario = usuarioRepositorio.findById(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("El usuario no existe."));

        String detalle = descripcion == null || descripcion.isBlank()
                ? "No se pudo completar la operación de caja." : descripcion;
        if (detalle.length() > 250) {
            detalle = detalle.substring(0, 250);
        }

        bitacoraOperacionRepositorio.save(
                BitacoraOperacion.registrar(
                        usuario,
                        tipoOperacion,
                        entidadAfectada,
                        registroId,
                        detalle,
                        ResultadoOperacion.FALLIDO));
    }

    @Transactional(readOnly = true)
    public List<BitacoraOperacionRespuesta> listarBitacoras(Long usuarioId,
                                                            TipoOperacion tipoOperacion,
                                                            ResultadoOperacion resultado) {

        if (usuarioId != null) {
            validarId(usuarioId, "usuario");

            if (!usuarioRepositorio.existsById(usuarioId)) {
                throw new RecursoNoEncontradoException("El usuario no existe.");
            }
        }

        return bitacoraOperacionRepositorio
                .buscarConFiltros(usuarioId, tipoOperacion, resultado)
                .stream()
                .map(BitacoraOperacionRespuesta::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public BitacoraOperacionRespuesta consultarBitacoraOperacion(Long bitacoraId) {
        validarId(bitacoraId, "bitácora");

        return bitacoraOperacionRepositorio.findById(bitacoraId)
                .map(BitacoraOperacionRespuesta::de)
                .orElseThrow(() -> new RecursoNoEncontradoException("La bitácora de operación no existe."));
    }
}
