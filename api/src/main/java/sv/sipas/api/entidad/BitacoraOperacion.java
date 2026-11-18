package sv.sipas.api.entidad;

import sv.sipas.api.entidad.enums.ResultadoOperacion;
import sv.sipas.api.entidad.enums.TipoOperacion;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "bitacora_operacion")
public class BitacoraOperacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(length = 30, nullable = false)
    private TipoOperacion tipoOperacion;

    @Column(length = 40, nullable = false)
    private String entidadAfectada;

    @Column(nullable = false)
    private Long registroAfectadoId;

    @Column(length = 250, nullable = false)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private ResultadoOperacion resultado;

    @Column(name = "fecha_hora", nullable = false, updatable = false)
    private LocalDateTime fechaHora = LocalDateTime.now();

    protected BitacoraOperacion() {
    }

    public BitacoraOperacion(Usuario usuario, TipoOperacion tipoOperacion, String entidadAfectada,
                             Long registroAfectadoId, String descripcion, ResultadoOperacion resultado) {
        if (usuario == null || tipoOperacion == null || resultado == null) {
            throw new IllegalArgumentException("El usuario, el tipo de operación y el resultado son obligatorios.");
        }

        if (registroAfectadoId == null || registroAfectadoId <= 0) {
            throw new IllegalArgumentException("El identificador del registro afectado debe ser mayor que cero.");
        }

        if (entidadAfectada == null || entidadAfectada.isBlank()
                || entidadAfectada.length() > 40) {
            throw new IllegalArgumentException("La entidad afectada es obligatoria y no debe exceder los 40 caracteres.");
        }

        if (descripcion == null || descripcion.isBlank()
                || descripcion.length() > 250) {
            throw new IllegalArgumentException("La descripción es obligatoria y no debe exceder los 250 caracteres.");
        }

        this.usuario = usuario;
        this.tipoOperacion = tipoOperacion;
        this.entidadAfectada = entidadAfectada;
        this.registroAfectadoId = registroAfectadoId;
        this.descripcion = descripcion;
        this.resultado = resultado;
    }

    public static BitacoraOperacion registrar(Usuario usuario, TipoOperacion tipoOperacion, String entidadAfectada,
                                              Long registroAfectadoId, String descripcion, ResultadoOperacion resultado) {

        return new BitacoraOperacion(usuario, tipoOperacion, entidadAfectada,
                registroAfectadoId, descripcion, resultado);
    }

    public Boolean esDeTipo(TipoOperacion tipoOperacion) {
        return this.tipoOperacion == tipoOperacion;
    }

    public Long getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public TipoOperacion getTipoOperacion() {
        return tipoOperacion;
    }

    public String getEntidadAfectada() {
        return entidadAfectada;
    }

    public Long getRegistroAfectadoId() {
        return registroAfectadoId;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public ResultadoOperacion getResultado() {
        return resultado;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    @Override
    public String toString() {
        return String.format(
                "BitacoraOperacion{id=%s, usuarioId=%s, tipoOperacion=%s, entidadAfectada='%s', registroAfectadoId=%s, descripcion='%s', resultado=%s, fechaHora=%s}",
                id, usuario != null ? usuario.getId() : null,
                tipoOperacion, entidadAfectada, registroAfectadoId,
                descripcion, resultado, fechaHora);
    }
}
