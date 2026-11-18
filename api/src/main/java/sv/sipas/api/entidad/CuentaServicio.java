package sv.sipas.api.entidad;

import sv.sipas.api.entidad.enums.EstadoCuentaServicio;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "cuenta_servicio", uniqueConstraints = @UniqueConstraint(name = "uk_cuenta_servicio_referencia", columnNames = {
        "servicio_id", "referencia"}))
public class CuentaServicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "servicio_id", nullable = false)
    private Servicio servicio;

    @Column(length = 50, nullable = false)
    private String referencia;

    @Column(length = 80, nullable = true)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private EstadoCuentaServicio estado;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn = LocalDateTime.now();

    protected CuentaServicio() {
    }

    public CuentaServicio(Cliente cliente, Servicio servicio, String referencia, String descripcion) {
        this.cliente = cliente;
        this.servicio = servicio;
        this.referencia = referencia;
        this.descripcion = descripcion;
        this.estado = EstadoCuentaServicio.ACTIVA;
    }

    public void activar() {
        this.estado = EstadoCuentaServicio.ACTIVA;
    }

    public void desactivar() {
        this.estado = EstadoCuentaServicio.INACTIVA;
    }

    public Boolean estaActiva() {
        return this.estado == EstadoCuentaServicio.ACTIVA;
    }

    public Long getId() {
        return id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public Servicio getServicio() {
        return servicio;
    }

    public String getReferencia() {
        return referencia;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public EstadoCuentaServicio getEstado() {
        return estado;
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }

    @Override
    public String toString() {
        return String.format(
                "CuentaServicio{id=%s, clienteId=%s, servicioId=%s, referencia='%s', estado=%s}",
                id,
                cliente != null ? cliente.getId() : null,
                servicio != null ? servicio.getId() : null,
                referencia, estado);
    }
}
