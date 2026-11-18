package sv.sipas.api.entidad;

import sv.sipas.api.entidad.enums.EstadoSesionCaja;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "sesion_caja")
public class SesionCaja {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "caja_id", nullable = false)
    private Caja caja;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empleado_id", nullable = false)
    private Empleado empleado;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal fondoInicial;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaApertura = LocalDateTime.now();

    @Column(nullable = true)
    private LocalDateTime fechaCierre;

    @Column(precision = 12, scale = 2, nullable = true)
    private BigDecimal efectivoContado;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private EstadoSesionCaja estado;

    protected SesionCaja() {
    }

    public SesionCaja(Caja caja, Empleado empleado, BigDecimal fondoInicial) {
        if (caja == null || empleado == null) {
            throw new IllegalArgumentException("La caja y el empleado son obligatorios.");
        }

        if (fondoInicial == null || fondoInicial.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El fondo inicial debe ser mayor o igual que cero.");
        }

        this.caja = caja;
        this.empleado = empleado;
        this.fondoInicial = fondoInicial;
        this.fechaCierre = null;
        this.efectivoContado = null;
        this.estado = EstadoSesionCaja.ABIERTA;
    }

    public static SesionCaja abrir(Caja caja, Empleado empleado, BigDecimal fondoInicial) {

        return new SesionCaja(caja, empleado, fondoInicial);
    }

    public Boolean estaAbierta() {
        return this.estado == EstadoSesionCaja.ABIERTA;
    }

    public void cerrar(BigDecimal efectivoContado, LocalDateTime fechaCierre) {
        if (!estaAbierta()) {
            throw new IllegalStateException("La sesión de caja ya está cerrada.");
        }

        if (efectivoContado == null || efectivoContado.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El efectivo contado debe ser mayor o igual que cero.");
        }

        if (fechaCierre == null || fechaCierre.isBefore(fechaApertura)) {
            throw new IllegalArgumentException("La fecha de cierre no puede ser nula ni anterior a la apertura.");
        }

        this.efectivoContado = efectivoContado;
        this.fechaCierre = fechaCierre;
        this.estado = EstadoSesionCaja.CERRADA;
    }

    public Long getId() {
        return id;
    }

    public Caja getCaja() {
        return caja;
    }

    public Empleado getEmpleado() {
        return empleado;
    }

    public BigDecimal getFondoInicial() {
        return fondoInicial;
    }

    public LocalDateTime getFechaApertura() {
        return fechaApertura;
    }

    public LocalDateTime getFechaCierre() {
        return fechaCierre;
    }

    public BigDecimal getEfectivoContado() {
        return efectivoContado;
    }

    public EstadoSesionCaja getEstado() {
        return estado;
    }

    @Override
    public String toString() {
        return String.format(
                "SesionCaja{id=%s, cajaId=%s, empleadoId=%s, fondoInicial=%s, fechaApertura=%s, fechaCierre=%s, efectivoContado=%s, estado=%s}",
                id,
                caja != null ? caja.getId() : null,
                empleado != null ? empleado.getId() : null,
                fondoInicial, fechaApertura, fechaCierre, efectivoContado, estado);
    }
}
