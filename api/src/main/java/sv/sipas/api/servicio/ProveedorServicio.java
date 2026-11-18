package sv.sipas.api.servicio;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.sipas.api.dto.ProveedorModificacionSolicitud;
import sv.sipas.api.dto.ProveedorRespuesta;
import sv.sipas.api.dto.ProveedorSolicitud;
import sv.sipas.api.entidad.Proveedor;
import sv.sipas.api.excepcion.ConflictoException;
import sv.sipas.api.excepcion.RecursoNoEncontradoException;
import sv.sipas.api.repositorio.ProveedorRepositorio;

import java.util.List;

import static sv.sipas.api.servicio.DatosMantenimiento.*;

@Service
public class ProveedorServicio {

    private final ProveedorRepositorio proveedorRepositorio;

    public ProveedorServicio(ProveedorRepositorio proveedorRepositorio) {
        this.proveedorRepositorio = proveedorRepositorio;
    }

    @Transactional(readOnly = true)
    public List<ProveedorRespuesta> listarProveedoresActivos() {
        return proveedorRepositorio.findByActivoTrue().stream().map(ProveedorRespuesta::de).toList();
    }

    @Transactional(readOnly = true)
    public ProveedorRespuesta consultarProveedor(Long proveedorId) {
        return ProveedorRespuesta.de(obtenerProveedor(proveedorId));
    }

    @Transactional
    public ProveedorRespuesta crearProveedor(ProveedorSolicitud solicitud) {
        String codigo = validarTexto(solicitud.codigo(), "código", 20);
        String nombre = validarTexto(solicitud.nombre(), "nombre", 100);

        if (proveedorRepositorio.existsByCodigo(codigo)) {
            throw new ConflictoException("El código del proveedor ya existe.");
        }

        Proveedor proveedor = proveedorRepositorio.save(new Proveedor(codigo, nombre));
        return ProveedorRespuesta.de(proveedor);
    }

    @Transactional
    public ProveedorRespuesta modificarProveedor(Long proveedorId, ProveedorModificacionSolicitud solicitud) {
        Proveedor proveedor = obtenerProveedor(proveedorId);
        validarCambios(solicitud.nombre(), solicitud.activo());

        if (solicitud.nombre() != null) {
            proveedor.cambiarNombre(
                    validarTexto(solicitud.nombre(), "nombre", 100)
            );
        }

        if (solicitud.activo() != null) {
            if (solicitud.activo()) {
                proveedor.activar();
            } else {
                proveedor.desactivar();
            }
        }

        proveedorRepositorio.save(proveedor);
        return ProveedorRespuesta.de(proveedor);
    }

    @Transactional
    public void desactivarProveedor(Long proveedorId) {
        Proveedor proveedor = obtenerProveedor(proveedorId);
        proveedor.desactivar();
        proveedorRepositorio.save(proveedor);
    }

    private Proveedor obtenerProveedor(Long proveedorId) {
        validarId(proveedorId, "proveedor");

        return proveedorRepositorio.findById(proveedorId)
                .orElseThrow(() -> new RecursoNoEncontradoException("El proveedor no existe."));
    }
}
