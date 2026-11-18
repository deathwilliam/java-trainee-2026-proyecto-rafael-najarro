package sv.sipas.api.dto;

import sv.sipas.api.entidad.Empleado;

public record EmpleadoRespuesta(Long id, Long usuarioId, Long sucursalId,
                                String codigo, String nombres, String apellidos,
                                Boolean activo) {

    public static EmpleadoRespuesta de(Empleado empleado) {
        return new EmpleadoRespuesta(
                empleado.getId(),
                empleado.getUsuario().getId(),
                empleado.getSucursal().getId(),
                empleado.getCodigo(),
                empleado.getNombres(),
                empleado.getApellidos(),
                empleado.getActivo()
        );
    }
}
