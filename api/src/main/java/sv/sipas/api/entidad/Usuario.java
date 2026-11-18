package sv.sipas.api.entidad;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_usuario", length = 50, nullable = false, unique = true)
    private String nombreUsuario;

    @Column(name = "clave_hash", length = 255, nullable = false)
    private String claveHash;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn = LocalDateTime.now();

    protected Usuario() {
    }

    public Usuario(String nombreUsuario, String claveHash) {
        this.nombreUsuario = nombreUsuario;
        this.claveHash = claveHash;
    }

    public void activar() {
        this.activo = true;
    }

    public void desactivar() {
        this.activo = false;
    }

    public void cambiarClaveHash(String nuevaClaveHash) {
        this.claveHash = nuevaClaveHash;
    }

    public Long getId() {
        return id;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public String getClaveHash() {
        return claveHash;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public void setClaveHash(String claveHash) {
        this.claveHash = claveHash;
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }

    @Override
    public String toString() {
        return String.format(
                "Usuario{id=%s, nombreUsuario='%s', activo=%s}",
                id, nombreUsuario, activo);
    }
}
