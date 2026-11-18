package sv.sipas.api.entidad;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "rol")
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 20, nullable = false, unique = true)
    private String codigo;

    @Column(length = 150, nullable = false)
    private String descripcion;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn = LocalDateTime.now();

    protected Rol() {
    }

    public Rol(String codigo, String descripcion) {
        if (!"ADMIN".equals(codigo)
                && !"ASESOR".equals(codigo)
                && !"CLIENTE".equals(codigo)) {
            throw new IllegalArgumentException("El código del rol debe ser ADMIN, ASESOR o CLIENTE.");
        }

        this.codigo = codigo;
        this.descripcion = descripcion;
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

    public String getCodigo() {
        return codigo;
    }

    public String getDescripcion() {
        return descripcion;
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
                "Rol{id=%s, codigo='%s', descripcion='%s', activo=%s}",
                id, codigo, descripcion, activo);
    }
}
