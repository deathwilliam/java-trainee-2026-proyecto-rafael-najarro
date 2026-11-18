package sv.sipas.api.servicio;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.sipas.api.dto.CuentaServicioRespuesta;
import sv.sipas.api.entidad.CuentaServicio;
import sv.sipas.api.excepcion.RecursoNoEncontradoException;
import sv.sipas.api.repositorio.CuentaServicioRepositorio;

@Service
public class CuentaServicioServicio {

    private final CuentaServicioRepositorio cuentaServicioRepositorio;
    private final AccesoConsultaServicio accesoConsultaServicio;

    public CuentaServicioServicio(CuentaServicioRepositorio cuentaServicioRepositorio,
                                  AccesoConsultaServicio accesoConsultaServicio) {
        this.cuentaServicioRepositorio = cuentaServicioRepositorio;
        this.accesoConsultaServicio = accesoConsultaServicio;
    }

    @Transactional(readOnly = true)
    public CuentaServicioRespuesta buscarPorServicioYReferencia(Long servicioId, String referencia, Long usuarioId) {
        accesoConsultaServicio.validarUsuarioId(usuarioId);

        if (servicioId == null || servicioId <= 0) {
            throw new IllegalArgumentException("El identificador del servicio debe ser mayor que cero.");
        }

        if (referencia == null || referencia.isBlank()) {
            throw new IllegalArgumentException("La referencia es obligatoria.");
        }

        CuentaServicio cuenta = cuentaServicioRepositorio
                .findByServicio_IdAndReferencia(servicioId, referencia.trim())
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe una cuenta de servicio para el servicio y la referencia indicados."));

        accesoConsultaServicio.validarCuenta(cuenta, usuarioId);
        return CuentaServicioRespuesta.de(cuenta);
    }

    @Transactional(readOnly = true)
    public CuentaServicioRespuesta consultarCuenta(Long cuentaId, Long usuarioId) {
        accesoConsultaServicio.validarUsuarioId(usuarioId);

        if (cuentaId == null || cuentaId <= 0) {
            throw new IllegalArgumentException("El identificador de la cuenta de servicio debe ser mayor que cero.");
        }

        CuentaServicio cuenta = cuentaServicioRepositorio.findById(cuentaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("La cuenta de servicio no existe."));

        accesoConsultaServicio.validarCuenta(cuenta, usuarioId);
        return CuentaServicioRespuesta.de(cuenta);
    }
}
