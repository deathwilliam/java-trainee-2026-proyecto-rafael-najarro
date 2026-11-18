package sv.sipas.api.entidad;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "usuario_rol", uniqueConstraints = @UniqueConstraint(name = "uk_usuario_rol", columnNames = {
        "usuario_id"}))
public class UsuarioRol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rol_id", nullable = false)
    private Rol rol;

    @Column(name = "fecha_hora", nullable = false, updatable = false)
    private LocalDateTime fechaHora = LocalDateTime.now();

    protected UsuarioRol() {
    }

    public UsuarioRol(Usuario usuario, Rol rol) {
        this.usuario = usuario;
        this.rol = rol;
    }

    public static UsuarioRol asignar(Usuario usuario, Rol rol) {

        return new UsuarioRol(usuario, rol);
    }

    public Boolean correspondeA(Rol rol) {
        return rol != null
                && this.rol != null
                && this.rol.getId() != null
                && this.rol.getId().equals(rol.getId());
    }

    public Long getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public Rol getRol() {
        return rol;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    @Override
    public String toString() {
        return String.format(
                "UsuarioRol{id=%s, usuarioId=%s, rolId=%s, fechaHora=%s}",
                id,
                usuario != null ? usuario.getId() : null,
                rol != null ? rol.getId() : null,
                fechaHora);
    }
}
