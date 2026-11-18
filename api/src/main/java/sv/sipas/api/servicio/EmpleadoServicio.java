package sv.sipas.api.servicio;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.sipas.api.dto.EmpleadoModificacionSolicitud;
import sv.sipas.api.dto.EmpleadoRespuesta;
import sv.sipas.api.dto.EmpleadoSolicitud;
import sv.sipas.api.entidad.Empleado;
import sv.sipas.api.entidad.Sucursal;
import sv.sipas.api.entidad.Usuario;
import sv.sipas.api.entidad.enums.EstadoSesionCaja;
import sv.sipas.api.excepcion.ConflictoException;
import sv.sipas.api.excepcion.OperacionRechazadaException;
import sv.sipas.api.excepcion.RecursoNoEncontradoException;
import sv.sipas.api.repositorio.EmpleadoRepositorio;
import sv.sipas.api.repositorio.SesionCajaRepositorio;
import sv.sipas.api.repositorio.SucursalRepositorio;
import sv.sipas.api.repositorio.UsuarioRepositorio;

import java.util.List;

import static sv.sipas.api.servicio.DatosMantenimiento.*;

@Service
public class EmpleadoServicio {

    private final EmpleadoRepositorio empleadoRepositorio;
    private final UsuarioRepositorio usuarioRepositorio;
    private final SucursalRepositorio sucursalRepositorio;
    private final SesionCajaRepositorio sesionCajaRepositorio;

    public EmpleadoServicio(EmpleadoRepositorio empleadoRepositorio,
                            UsuarioRepositorio usuarioRepositorio,
                            SucursalRepositorio sucursalRepositorio,
                            SesionCajaRepositorio sesionCajaRepositorio) {
        this.empleadoRepositorio = empleadoRepositorio;
        this.usuarioRepositorio = usuarioRepositorio;
        this.sucursalRepositorio = sucursalRepositorio;
        this.sesionCajaRepositorio = sesionCajaRepositorio;
    }

    @Transactional(readOnly = true)
    public List<EmpleadoRespuesta> listarEmpleados() {
        return empleadoRepositorio.findAll().stream().map(EmpleadoRespuesta::de).toList();
    }

    @Transactional(readOnly = true)
    public EmpleadoRespuesta consultarEmpleado(Long empleadoId) {
        return EmpleadoRespuesta.de(obtenerEmpleado(empleadoId));
    }

    @Transactional
    public EmpleadoRespuesta crearEmpleado(EmpleadoSolicitud solicitud) {
        validarId(solicitud.usuarioId(), "usuario");
        validarId(solicitud.sucursalId(), "sucursal");
        String codigo = validarTexto(solicitud.codigo(), "código", 20);
        String nombres = validarTexto(solicitud.nombres(), "nombres", 80);
        String apellidos = validarTexto(solicitud.apellidos(), "apellidos", 80);

        Usuario usuario = usuarioRepositorio.findById(solicitud.usuarioId())
                .orElseThrow(() -> new RecursoNoEncontradoException("El usuario no existe."));
        Sucursal sucursal = sucursalRepositorio.findById(solicitud.sucursalId())
                .orElseThrow(() -> new RecursoNoEncontradoException("La sucursal no existe."));

        if (!usuario.getActivo() || !sucursal.getActivo()) {
            throw new OperacionRechazadaException("El usuario y la sucursal deben estar activos.");
        }
        if (empleadoRepositorio.existsByCodigo(codigo)) {
            throw new ConflictoException("El código del empleado ya existe.");
        }
        if (empleadoRepositorio.existsByUsuario_Id(usuario.getId())) {
            throw new ConflictoException("El usuario ya tiene un empleado asociado.");
        }

        Empleado empleado = empleadoRepositorio.save(new Empleado(usuario, sucursal, codigo, nombres, apellidos));
        return EmpleadoRespuesta.de(empleado);
    }

    @Transactional
    public EmpleadoRespuesta modificarEmpleado(Long empleadoId,
                                               EmpleadoModificacionSolicitud solicitud) {
        Empleado empleado = obtenerEmpleado(empleadoId);
        validarCambios(solicitud.nombres(), solicitud.apellidos(), solicitud.activo());

        if (solicitud.nombres() != null) {
            empleado.cambiarNombres(validarTexto(solicitud.nombres(), "nombres", 80));
        }
        if (solicitud.apellidos() != null) {
            empleado.cambiarApellidos(validarTexto(solicitud.apellidos(), "apellidos", 80));
        }
        if (solicitud.activo() != null) {
            if (solicitud.activo()) {
                if (!empleado.getUsuario().getActivo() || !empleado.getSucursal().getActivo()) {
                    throw new OperacionRechazadaException("El usuario y la sucursal deben estar activos.");
                }
                empleado.activar();
            } else {
                validarSinSesionAbierta(empleadoId);
                empleado.desactivar();
            }
        }

        empleadoRepositorio.save(empleado);
        return EmpleadoRespuesta.de(empleado);
    }

    @Transactional
    public void desactivarEmpleado(Long empleadoId) {
        Empleado empleado = obtenerEmpleado(empleadoId);
        validarSinSesionAbierta(empleadoId);
        empleado.desactivar();
        empleadoRepositorio.save(empleado);
    }

    private Empleado obtenerEmpleado(Long empleadoId) {
        validarId(empleadoId, "empleado");
        return empleadoRepositorio.findById(empleadoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("El empleado no existe."));
    }

    private void validarSinSesionAbierta(Long empleadoId) {
        if (sesionCajaRepositorio.existsByEmpleado_IdAndEstado(empleadoId, EstadoSesionCaja.ABIERTA)) {
            throw new ConflictoException("El empleado ya tiene una sesión abierta.");
        }
    }
}
