package sv.sipas.api.servicio;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.sipas.api.dto.CajaModificacionSolicitud;
import sv.sipas.api.dto.CajaRespuesta;
import sv.sipas.api.dto.CajaSolicitud;
import sv.sipas.api.entidad.Caja;
import sv.sipas.api.entidad.Sucursal;
import sv.sipas.api.entidad.enums.EstadoSesionCaja;
import sv.sipas.api.excepcion.ConflictoException;
import sv.sipas.api.excepcion.OperacionRechazadaException;
import sv.sipas.api.excepcion.RecursoNoEncontradoException;
import sv.sipas.api.repositorio.CajaRepositorio;
import sv.sipas.api.repositorio.SesionCajaRepositorio;
import sv.sipas.api.repositorio.SucursalRepositorio;

import java.util.List;

import static sv.sipas.api.servicio.DatosMantenimiento.*;

@Service
public class CajaServicio {

    private final CajaRepositorio cajaRepositorio;
    private final SucursalRepositorio sucursalRepositorio;
    private final SesionCajaRepositorio sesionCajaRepositorio;

    public CajaServicio(CajaRepositorio cajaRepositorio,
                        SucursalRepositorio sucursalRepositorio,
                        SesionCajaRepositorio sesionCajaRepositorio) {
        this.cajaRepositorio = cajaRepositorio;
        this.sucursalRepositorio = sucursalRepositorio;
        this.sesionCajaRepositorio = sesionCajaRepositorio;
    }

    @Transactional(readOnly = true)
    public List<CajaRespuesta> listarCajas() {
        return cajaRepositorio.findAll().stream().map(CajaRespuesta::de).toList();
    }

    @Transactional(readOnly = true)
    public CajaRespuesta consultarCaja(Long cajaId) {
        return CajaRespuesta.de(obtenerCaja(cajaId));
    }

    @Transactional
    public CajaRespuesta crearCaja(CajaSolicitud solicitud) {
        validarId(solicitud.sucursalId(), "sucursal");
        String codigo = validarTexto(solicitud.codigo(), "código", 20);
        String nombre = validarTexto(solicitud.nombre(), "nombre", 60);

        Sucursal sucursal = sucursalRepositorio.findById(solicitud.sucursalId())
                .orElseThrow(() -> new RecursoNoEncontradoException("La sucursal no existe."));
        if (!sucursal.getActivo()) {
            throw new OperacionRechazadaException("La sucursal no está habilitada.");
        }
        if (cajaRepositorio.existsBySucursal_IdAndCodigo(sucursal.getId(), codigo)) {
            throw new ConflictoException("El código de caja ya existe en esta sucursal.");
        }

        Caja caja = cajaRepositorio.save(new Caja(sucursal, codigo, nombre));
        return CajaRespuesta.de(caja);
    }

    @Transactional
    public CajaRespuesta modificarCaja(Long cajaId, CajaModificacionSolicitud solicitud) {
        Caja caja = obtenerCaja(cajaId);
        validarCambios(solicitud.nombre(), solicitud.activo());

        if (solicitud.nombre() != null) {
            caja.cambiarNombre(validarTexto(solicitud.nombre(), "nombre", 60));
        }
        if (solicitud.activo() != null) {
            if (solicitud.activo()) {
                if (!caja.getSucursal().getActivo()) {
                    throw new OperacionRechazadaException("No puede activar una caja de una sucursal inactiva.");
                }
                caja.activar();
            } else {
                validarSinSesionAbierta(cajaId);
                caja.desactivar();
            }
        }

        cajaRepositorio.save(caja);
        return CajaRespuesta.de(caja);
    }

    @Transactional
    public void desactivarCaja(Long cajaId) {
        Caja caja = obtenerCaja(cajaId);
        validarSinSesionAbierta(cajaId);
        caja.desactivar();
        cajaRepositorio.save(caja);
    }

    private Caja obtenerCaja(Long cajaId) {
        validarId(cajaId, "caja");
        return cajaRepositorio.findById(cajaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("La caja no existe."));
    }

    private void validarSinSesionAbierta(Long cajaId) {
        if (sesionCajaRepositorio.existsByCaja_IdAndEstado(cajaId, EstadoSesionCaja.ABIERTA)) {
            throw new ConflictoException("La caja ya tiene una sesión abierta.");
        }
    }
}
