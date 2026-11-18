package sv.sipas.api.entidad;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "caja", uniqueConstraints = @UniqueConstraint(name = "uk_caja_sucursal_codigo", columnNames = {
        "sucursal_id", "codigo"}))
public class Caja {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sucursal_id", nullable = false)
    private Sucursal sucursal;

    @Column(length = 20, nullable = false)
    private String codigo;

    @Column(length = 60, nullable = false)
    private String nombre;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn = LocalDateTime.now();

    protected Caja() {
    }

    public Caja(Sucursal sucursal, String codigo, String nombre) {
        this.sucursal = sucursal;
        this.codigo = codigo;
        this.nombre = nombre;
    }

    public void activar() {
        this.activo = true;
    }

    public void desactivar() {
        this.activo = false;
    }

    public void cambiarNombre(String nombre) {
        this.nombre = nombre;
    }

    public Long getId() {
        return id;
    }

    public Sucursal getSucursal() {
        return sucursal;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
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
                "Caja{id=%s, sucursalId=%s, codigo='%s', nombre='%s', activo=%s}",
                id, sucursal != null ? sucursal.getId() : null,
                codigo, nombre, activo);
    }
}
