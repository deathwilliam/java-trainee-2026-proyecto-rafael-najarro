package sv.sipas.api.entidad;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "servicio", uniqueConstraints = @UniqueConstraint(name = "uk_servicio_proveedor_codigo", columnNames = {
        "proveedor_id", "codigo"}))
public class Servicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "proveedor_id", nullable = false)
    private Proveedor proveedor;

    @Column(length = 20, nullable = false)
    private String codigo;

    @Column(length = 80, nullable = false)
    private String nombre;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal comision;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn = LocalDateTime.now();

    protected Servicio() {
    }

    public Servicio(Proveedor proveedor, String codigo, String nombre, BigDecimal comision) {
        if (proveedor == null) {
            throw new IllegalArgumentException("El proveedor es obligatorio.");
        }
        if (comision == null) {
            throw new IllegalArgumentException("La comisión es obligatoria.");
        }
        if (comision.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("La comisión debe ser mayor o igual que cero.");
        }

        this.proveedor = proveedor;
        this.codigo = codigo;
        this.nombre = nombre;
        this.comision = comision;
    }

    public void activar() {
        this.activo = true;
    }

    public void desactivar() {
        this.activo = false;
    }

    public void cambiarComision(BigDecimal nuevaComision) {
        if (nuevaComision == null) {
            throw new IllegalArgumentException("La comisión es obligatoria.");
        }
        if (nuevaComision.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("La comisión debe ser mayor o igual que cero.");
        }
        this.comision = nuevaComision;
    }

    public void cambiarNombre(String nombre) {
        this.nombre = nombre;
    }

    public Long getId() {
        return id;
    }

    public Proveedor getProveedor() {
        return proveedor;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public BigDecimal getComision() {
        return comision;
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
                "Servicio{id=%s, proveedorId=%s, codigo='%s', nombre='%s', comision=%s, activo=%s}",
                id, proveedor != null ? proveedor.getId() : null,
                codigo, nombre, comision, activo);
    }
}
