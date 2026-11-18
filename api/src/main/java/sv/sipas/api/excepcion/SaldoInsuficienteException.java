package sv.sipas.api.excepcion;

import java.math.BigDecimal;

public class SaldoInsuficienteException extends OperacionRechazadaException {

    private final BigDecimal efectivoRecibido;

    public SaldoInsuficienteException(String numeroFactura, BigDecimal efectivoRecibido, BigDecimal total) {
        super("Efectivo insuficiente para pagar la factura " + numeroFactura + ": recibió $" + efectivoRecibido.toPlainString()
                + " y necesita $" + total.toPlainString() + ".");
        this.efectivoRecibido = efectivoRecibido;
    }

    public BigDecimal getEfectivoRecibido() {
        return efectivoRecibido;
    }
}
