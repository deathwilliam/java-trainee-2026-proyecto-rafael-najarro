package sv.sipas.api.dto;

import sv.sipas.api.entidad.CuentaServicio;
import sv.sipas.api.entidad.enums.EstadoCuentaServicio;

public record CuentaServicioRespuesta(Long id, String referencia, String titular, String nombreProveedor,
                                      String nombreServicio, EstadoCuentaServicio estado) {

    public static CuentaServicioRespuesta de(CuentaServicio cuentaServicio) {
        return new CuentaServicioRespuesta(
                cuentaServicio.getId(),
                cuentaServicio.getReferencia(),
                cuentaServicio.getCliente().getNombres().concat(" " + cuentaServicio.getCliente().getApellidos()),
                cuentaServicio.getServicio().getProveedor().getNombre(),
                cuentaServicio.getServicio().getNombre(),
                cuentaServicio.getEstado()
        );
    }
}
