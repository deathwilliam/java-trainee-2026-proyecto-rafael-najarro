package sv.sipas.api.dto;

public record TokenRespuesta(String token, String tipo, long expiraEnSegundos) {
}
