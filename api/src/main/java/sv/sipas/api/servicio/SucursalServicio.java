package sv.sipas.api.servicio;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.sipas.api.dto.SucursalModificacionSolicitud;
import sv.sipas.api.dto.SucursalRespuesta;
import sv.sipas.api.dto.SucursalSolicitud;
import sv.sipas.api.entidad.Sucursal;
import sv.sipas.api.entidad.enums.EstadoSesionCaja;
import sv.sipas.api.excepcion.ConflictoException;
import sv.sipas.api.excepcion.RecursoNoEncontradoException;
import sv.sipas.api.repositorio.SesionCajaRepositorio;
import sv.sipas.api.repositorio.SucursalRepositorio;

import java.util.List;

import static sv.sipas.api.servicio.DatosMantenimiento.*;

@Service
public class SucursalServicio {

    private final SucursalRepositorio sucursalRepositorio;
    private final SesionCajaRepositorio sesionCajaRepositorio;

    public SucursalServicio(SucursalRepositorio sucursalRepositorio,
                            SesionCajaRepositorio sesionCajaRepositorio) {
        this.sucursalRepositorio = sucursalRepositorio;
        this.sesionCajaRepositorio = sesionCajaRepositorio;
    }

    @Transactional(readOnly = true)
    public List<SucursalRespuesta> listarSucursales() {
        return sucursalRepositorio.findAll().stream().map(SucursalRespuesta::de).toList();
    }

    @Transactional(readOnly = true)
    public SucursalRespuesta consultarSucursal(Long sucursalId) {
        return SucursalRespuesta.de(obtenerSucursal(sucursalId));
    }

    @Transactional
    public SucursalRespuesta crearSucursal(SucursalSolicitud solicitud) {
        String codigo = validarTexto(solicitud.codigo(), "código", 20);
        String nombre = validarTexto(solicitud.nombre(), "nombre", 80);
        String direccion = validarTexto(solicitud.direccion(), "dirección", 200);

        if (sucursalRepositorio.existsByCodigo(codigo)) {
            throw new ConflictoException("El código de la sucursal ya existe.");
        }

        Sucursal sucursal = sucursalRepositorio.save(new Sucursal(codigo, nombre, direccion));
        return SucursalRespuesta.de(sucursal);
    }

    @Transactional
    public SucursalRespuesta modificarSucursal(Long sucursalId,
                                               SucursalModificacionSolicitud solicitud) {
        Sucursal sucursal = obtenerSucursal(sucursalId);
        validarCambios(solicitud.nombre(), solicitud.direccion(), solicitud.activo());

        if (solicitud.nombre() != null) {
            sucursal.cambiarNombre(validarTexto(solicitud.nombre(), "nombre", 80));
        }
        if (solicitud.direccion() != null) {
            sucursal.cambiarDireccion(validarTexto(solicitud.direccion(), "dirección", 200));
        }
        if (solicitud.activo() != null) {
            if (solicitud.activo()) {
                sucursal.activar();
            } else {
                validarSinSesionesAbiertas(sucursalId);
                sucursal.desactivar();
            }
        }

        sucursalRepositorio.save(sucursal);
        return SucursalRespuesta.de(sucursal);
    }

    @Transactional
    public void desactivarSucursal(Long sucursalId) {
        Sucursal sucursal = obtenerSucursal(sucursalId);
        validarSinSesionesAbiertas(sucursalId);
        sucursal.desactivar();
        sucursalRepositorio.save(sucursal);
    }

    private Sucursal obtenerSucursal(Long sucursalId) {
        validarId(sucursalId, "sucursal");
        return sucursalRepositorio.findById(sucursalId)
                .orElseThrow(() -> new RecursoNoEncontradoException("La sucursal no existe."));
    }

    private void validarSinSesionesAbiertas(Long sucursalId) {
        if (sesionCajaRepositorio.existsByCaja_Sucursal_IdAndEstado(sucursalId, EstadoSesionCaja.ABIERTA)) {
            throw new ConflictoException("La sucursal tiene sesiones de caja abiertas.");
        }
    }
}
