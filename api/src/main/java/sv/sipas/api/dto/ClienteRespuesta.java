package sv.sipas.api.dto;

import sv.sipas.api.entidad.Cliente;

public record ClienteRespuesta(Long id, String nombres, String apellidos, Boolean activo) {

    public static ClienteRespuesta de(Cliente cliente) {
        return new ClienteRespuesta(
                cliente.getId(),
                cliente.getNombres(),
                cliente.getApellidos(),
                cliente.getActivo()
        );
    }
}
