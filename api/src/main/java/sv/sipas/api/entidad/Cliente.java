package sv.sipas.api.entidad;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDateTime;

@Entity
@Table(name = "cliente")
public class Cliente {

    private static final String FORMATO_DUI = "^[0-9]{8}-[0-9]$";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "usuario_id", unique = true, nullable = true)
    private Usuario usuario;

    @Pattern(regexp = FORMATO_DUI, message = "El DUI debe tener el formato ########-#.")
    @Column(length = 10, nullable = false, unique = true)
    private String dui;

    @Column(length = 80, nullable = false)
    private String nombres;

    @Column(length = 80, nullable = false)
    private String apellidos;

    @Column(length = 20, nullable = true)
    private String telefono;

    @Column(length = 120, nullable = true)
    private String correo;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn = LocalDateTime.now();

    protected Cliente() {
    }

    public Cliente(Usuario usuario, String dui, String nombres, String apellidos, String telefono, String correo) {
        if (dui == null || !dui.matches(FORMATO_DUI)) {
            throw new IllegalArgumentException("El DUI debe tener el formato ########-#.");
        }

        this.usuario = usuario;
        this.dui = dui;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.telefono = telefono;
        this.correo = correo;
    }

    public void activar() {
        this.activo = true;
    }

    public void desactivar() {
        this.activo = false;
    }

    public Long getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public String getDui() {
        return dui;
    }

    public String getNombres() {
        return nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getCorreo() {
        return correo;
    }

    public Boolean getActivo() {
        return activo;
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }

    @Override
    public String toString() {
        return String.format(
                "Cliente{id=%s, usuarioId=%s, nombres='%s', apellidos='%s', activo=%s}",
                id, usuario != null ? usuario.getId() : null,
                nombres, apellidos, activo);
    }
}
