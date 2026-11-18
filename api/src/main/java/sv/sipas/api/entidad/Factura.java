package sv.sipas.api.entidad;

import sv.sipas.api.entidad.enums.EstadoFactura;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "factura", uniqueConstraints = @UniqueConstraint(name = "uk_factura_cuenta_numero", columnNames = {
        "cuenta_servicio_id", "numero_factura"}))
public class Factura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cuenta_servicio_id", nullable = false)
    private CuentaServicio cuentaServicio;

    @Column(name = "numero_factura", length = 50, nullable = false)
    private String numeroFactura;

    @Column(nullable = false)
    private LocalDate periodo;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal monto;

    @Column(nullable = false)
    private LocalDate fechaEmision;

    @Column(nullable = false)
    private LocalDate fechaVencimiento;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private EstadoFactura estado;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn = LocalDateTime.now();

    protected Factura() {
    }

    public Factura(CuentaServicio cuentaServicio, String numeroFactura, LocalDate periodo, BigDecimal monto,
                   LocalDate fechaEmision, LocalDate fechaVencimiento) {
        if (cuentaServicio == null) {
            throw new IllegalArgumentException("La cuenta de servicio es obligatoria.");
        }

        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto de la factura debe ser mayor que cero.");
        }

        if (periodo == null || periodo.getDayOfMonth() != 1) {
            throw new IllegalArgumentException("El período debe corresponder al primer día del mes.");
        }

        if (fechaEmision == null || fechaVencimiento == null) {
            throw new IllegalArgumentException("Las fechas de emisión y vencimiento son obligatorias.");
        }

        if (fechaVencimiento.isBefore(fechaEmision)) {
            throw new IllegalArgumentException("El vencimiento no puede ser anterior a la emisión.");
        }

        this.cuentaServicio = cuentaServicio;
        this.numeroFactura = numeroFactura;
        this.periodo = periodo;
        this.monto = monto;
        this.fechaEmision = fechaEmision;
        this.fechaVencimiento = fechaVencimiento;
        this.estado = EstadoFactura.PENDIENTE;
    }

    public Boolean estaPendiente() {
        return this.estado == EstadoFactura.PENDIENTE;
    }

    public void marcarPagada() {
        if (!estaPendiente()) {
            throw new IllegalStateException("La factura no está pendiente de pago.");
        }

        this.estado = EstadoFactura.PAGADA;
    }

    public Long getId() {
        return id;
    }

    public CuentaServicio getCuentaServicio() {
        return cuentaServicio;
    }

    public String getNumeroFactura() {
        return numeroFactura;
    }

    public LocalDate getPeriodo() {
        return periodo;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public LocalDate getFechaEmision() {
        return fechaEmision;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public EstadoFactura getEstado() {
        return estado;
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }

    @Override
    public String toString() {
        return String.format(
                "Factura{id=%s, cuentaServicioId=%s, numeroFactura='%s', periodo=%s, monto=%s, fechaEmision=%s, fechaVencimiento=%s, estado=%s}",
                id, cuentaServicio != null ? cuentaServicio.getId() : null,
                numeroFactura, periodo, monto, fechaEmision, fechaVencimiento, estado);
    }
}
