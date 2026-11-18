package sv.sipas.api.entidad;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pago")
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "factura_id", unique = true, nullable = false)
    private Factura factura;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sesion_caja_id", nullable = false)
    private SesionCaja sesionCaja;

    @Column(name = "numero_comprobante", length = 30, nullable = false, unique = true)
    private String numeroComprobante;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal comisionAplicada;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal efectivoRecibido;

    @Column(name = "fecha_hora", nullable = false, updatable = false)
    private LocalDateTime fechaHora = LocalDateTime.now();

    protected Pago() {
    }

    public Pago(Factura factura, SesionCaja sesionCaja, String numeroComprobante, BigDecimal comisionAplicada,
                BigDecimal efectivoRecibido) {
        if (factura == null) {
            throw new IllegalArgumentException("La factura es obligatoria.");
        }

        if (sesionCaja == null) {
            throw new IllegalArgumentException("La sesión de caja es obligatoria.");
        }

        if (factura.getMonto() == null
                || factura.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto de la factura debe ser mayor que cero.");
        }

        if (comisionAplicada == null
                || comisionAplicada.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("La comisión aplicada debe ser mayor o igual que cero.");
        }

        if (efectivoRecibido == null
                || efectivoRecibido.compareTo(
                factura.getMonto().add(comisionAplicada)) < 0) {
            throw new IllegalArgumentException("El efectivo recibido es insuficiente para pagar la factura.");
        }

        if (numeroComprobante == null || numeroComprobante.isBlank()
                || numeroComprobante.length() > 30) {
            throw new IllegalArgumentException("El número de comprobante es obligatorio y no debe exceder los 30 caracteres.");
        }

        this.factura = factura;
        this.sesionCaja = sesionCaja;
        this.numeroComprobante = numeroComprobante;
        this.comisionAplicada = comisionAplicada;
        this.efectivoRecibido = efectivoRecibido;
    }

    public BigDecimal calcularTotal() {
        return factura.getMonto().add(comisionAplicada);
    }

    public BigDecimal calcularCambio() {
        return efectivoRecibido.subtract(calcularTotal());
    }

    public Long getId() {
        return id;
    }

    public Factura getFactura() {
        return factura;
    }

    public SesionCaja getSesionCaja() {
        return sesionCaja;
    }

    public String getNumeroComprobante() {
        return numeroComprobante;
    }

    public BigDecimal getComisionAplicada() {
        return comisionAplicada;
    }

    public BigDecimal getEfectivoRecibido() {
        return efectivoRecibido;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    @Override
    public String toString() {
        return String.format(
                "Pago{id=%s, facturaId=%s, sesionCajaId=%s, numeroComprobante='%s', comisionAplicada=%s, efectivoRecibido=%s, fechaHora=%s}",
                id,
                factura != null ? factura.getId() : null,
                sesionCaja != null ? sesionCaja.getId() : null,
                numeroComprobante, comisionAplicada, efectivoRecibido, fechaHora);
    }
}
