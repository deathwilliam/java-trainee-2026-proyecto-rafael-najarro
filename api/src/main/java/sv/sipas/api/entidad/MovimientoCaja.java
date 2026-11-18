package sv.sipas.api.entidad;

import sv.sipas.api.entidad.enums.TipoMovimientoCaja;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "movimiento_caja")
public class MovimientoCaja {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sesion_caja_id", nullable = false)
    private SesionCaja sesionCaja;

    @OneToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "pago_id", unique = true, nullable = true)
    private Pago pago;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private TipoMovimientoCaja tipo;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal monto;

    @Column(length = 200, nullable = true)
    private String motivo;

    @Column(name = "fecha_hora", nullable = false, updatable = false)
    private LocalDateTime fechaHora = LocalDateTime.now();

    protected MovimientoCaja() {
    }

    private MovimientoCaja(SesionCaja sesionCaja, Pago pago, Usuario usuario, TipoMovimientoCaja tipo, BigDecimal monto,
                           String motivo) {
        if (sesionCaja == null || usuario == null || tipo == null) {
            throw new IllegalArgumentException("La sesión de caja, el usuario y el tipo de movimiento son obligatorios.");
        }

        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor que cero.");
        }

        if (tipo == TipoMovimientoCaja.INGRESO_COBRO && pago == null) {
            throw new IllegalArgumentException("El ingreso debe estar asociado a un pago.");
        }

        if (tipo == TipoMovimientoCaja.RETIRO) {
            if (pago != null) {
                throw new IllegalArgumentException("Un retiro no puede estar asociado a un pago.");
            }

            if (motivo == null || motivo.isBlank()) {
                throw new IllegalArgumentException("El motivo del retiro es obligatorio.");
            }
        }

        if (motivo != null && motivo.length() > 200) {
            throw new IllegalArgumentException("El motivo del retiro no debe exceder los 200 caracteres.");
        }

        this.sesionCaja = sesionCaja;
        this.pago = pago;
        this.usuario = usuario;
        this.tipo = tipo;
        this.monto = monto;
        this.motivo = motivo;
    }

    public static MovimientoCaja crearIngreso(Pago pago, Usuario usuario) {
        if (pago == null) {
            throw new IllegalArgumentException("El pago es obligatorio.");
        }

        return new MovimientoCaja(
                pago.getSesionCaja(),
                pago,
                usuario,
                TipoMovimientoCaja.INGRESO_COBRO,
                pago.calcularTotal(),
                null);
    }

    public static MovimientoCaja crearRetiro(SesionCaja sesionCaja, Usuario usuario,
                                             BigDecimal monto, String motivo) {

        return new MovimientoCaja(
                sesionCaja,
                null,
                usuario,
                TipoMovimientoCaja.RETIRO,
                monto,
                motivo);
    }

    public Boolean esIngreso() {
        return this.tipo == TipoMovimientoCaja.INGRESO_COBRO;
    }

    public Boolean esRetiro() {
        return this.tipo == TipoMovimientoCaja.RETIRO;
    }

    public Long getId() {
        return id;
    }

    public SesionCaja getSesionCaja() {
        return sesionCaja;
    }

    public Pago getPago() {
        return pago;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public TipoMovimientoCaja getTipo() {
        return tipo;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public String getMotivo() {
        return motivo;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    @Override
    public String toString() {
        return String.format(
                "MovimientoCaja{id=%s, sesionCajaId=%s, pagoId=%s, usuarioId=%s, tipo=%s, monto=%s, motivo='%s', fechaHora=%s}",
                id,
                sesionCaja != null ? sesionCaja.getId() : null,
                pago != null ? pago.getId() : null,
                usuario != null ? usuario.getId() : null,
                tipo, monto, motivo, fechaHora);
    }
}
