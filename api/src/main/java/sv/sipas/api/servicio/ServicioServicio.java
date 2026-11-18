package sv.sipas.api.servicio;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.sipas.api.dto.ServicioModificacionSolicitud;
import sv.sipas.api.dto.ServicioRespuesta;
import sv.sipas.api.dto.ServicioSolicitud;
import sv.sipas.api.entidad.Proveedor;
import sv.sipas.api.entidad.Servicio;
import sv.sipas.api.excepcion.ConflictoException;
import sv.sipas.api.excepcion.OperacionRechazadaException;
import sv.sipas.api.excepcion.RecursoNoEncontradoException;
import sv.sipas.api.repositorio.ProveedorRepositorio;
import sv.sipas.api.repositorio.ServicioRepositorio;

import java.util.List;

import static sv.sipas.api.servicio.DatosMantenimiento.*;

@Service
public class ServicioServicio {

    private final ServicioRepositorio servicioRepositorio;
    private final ProveedorRepositorio proveedorRepositorio;

    public ServicioServicio(ServicioRepositorio servicioRepositorio, ProveedorRepositorio proveedorRepositorio) {
        this.servicioRepositorio = servicioRepositorio;
        this.proveedorRepositorio = proveedorRepositorio;
    }

    @Transactional(readOnly = true)
    public List<ServicioRespuesta> listarServiciosActivosPorProveedor(Long proveedorId) {
        if (proveedorId == null || proveedorId <= 0) {
            throw new IllegalArgumentException("El identificador del proveedor debe ser mayor que cero.");
        }

        Proveedor proveedor = proveedorRepositorio.findById(proveedorId)
                .orElseThrow(() -> new RecursoNoEncontradoException("El proveedor no existe."));

        if (!proveedor.getActivo()) {
            throw new OperacionRechazadaException("El proveedor está inactivo.");
        }

        return servicioRepositorio.findByProveedor_IdAndActivoTrue(proveedorId)
                .stream()
                .map(ServicioRespuesta::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public ServicioRespuesta consultarServicio(Long servicioId) {
        return ServicioRespuesta.de(obtenerServicio(servicioId));
    }

    @Transactional
    public ServicioRespuesta crearServicio(ServicioSolicitud solicitud) {
        validarId(solicitud.proveedorId(), "proveedor");

        String codigo = validarTexto(solicitud.codigo(), "código", 20);
        String nombre = validarTexto(solicitud.nombre(), "nombre", 80);
        validarComision(solicitud.comision());

        Proveedor proveedor = proveedorRepositorio.findById(solicitud.proveedorId())
                .orElseThrow(() -> new RecursoNoEncontradoException("El proveedor no existe."));

        if (!proveedor.getActivo()) {
            throw new OperacionRechazadaException("El proveedor está inactivo.");
        }

        if (servicioRepositorio.existsByProveedor_IdAndCodigo(proveedor.getId(), codigo)) {
            throw new ConflictoException("El código del servicio ya existe para este proveedor.");
        }

        Servicio servicio = servicioRepositorio.save(new Servicio(proveedor, codigo, nombre, solicitud.comision()));
        return ServicioRespuesta.de(servicio);
    }

    @Transactional
    public ServicioRespuesta modificarServicio(Long servicioId,
                                               ServicioModificacionSolicitud solicitud) {

        Servicio servicio = obtenerServicio(servicioId);
        validarCambios(solicitud.nombre(), solicitud.comision(), solicitud.activo());

        if (solicitud.nombre() != null) {
            servicio.cambiarNombre(
                    validarTexto(solicitud.nombre(), "nombre", 80)
            );
        }

        if (solicitud.comision() != null) {
            validarComision(solicitud.comision());
            servicio.cambiarComision(solicitud.comision());
        }

        if (solicitud.activo() != null) {
            if (solicitud.activo()) {
                if (!servicio.getProveedor().getActivo()) {
                    throw new OperacionRechazadaException("No puede activar un servicio de un proveedor inactivo.");
                }

                servicio.activar();
            } else {
                servicio.desactivar();
            }
        }

        servicioRepositorio.save(servicio);
        return ServicioRespuesta.de(servicio);
    }

    @Transactional
    public void desactivarServicio(Long servicioId) {
        Servicio servicio = obtenerServicio(servicioId);
        servicio.desactivar();
        servicioRepositorio.save(servicio);
    }

    private Servicio obtenerServicio(Long servicioId) {
        validarId(servicioId, "servicio");

        return servicioRepositorio.findById(servicioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("El servicio no existe."));
    }
}
