package sv.sipas.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record RolUsuarioSolicitud(
        @NotNull(message = "El identificador del rol es obligatorio.")
        @Positive(message = "El identificador del rol debe ser mayor que cero.")
        Long rolId) {
}
