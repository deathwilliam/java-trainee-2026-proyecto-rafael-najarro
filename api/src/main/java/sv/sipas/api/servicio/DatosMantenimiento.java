package sv.sipas.api.servicio;

import java.math.BigDecimal;
import java.util.Arrays;

public final class DatosMantenimiento {

    private DatosMantenimiento() {
    }

    public static void validarId(Long id, String campo) {
        if (id == null || id <= 0) {
            String articulo = switch (campo) {
                case "caja",
                     "sucursal",
                     "factura",
                     "sesión de caja",
                     "bitácora" -> "de la ";
                default -> "del ";
            };

            throw new IllegalArgumentException("El identificador " + articulo + campo + " debe ser mayor que cero.");
        }
    }

    public static String validarTexto(String valor, String campo, int maximo) {
        if (valor == null || valor.isBlank() || valor.length() > maximo) {
            throw new IllegalArgumentException("El campo " + campo + " es obligatorio y no debe exceder los "
                    + maximo + " caracteres.");
        }
        return valor.trim();
    }

    public static void validarCambios(Object... campos) {
        if (Arrays.stream(campos).allMatch(campo -> campo == null)) {
            throw new IllegalArgumentException("Debe proporcionar al menos un campo para modificar.");
        }
    }

    public static void validarComision(BigDecimal comision) {
        if (comision == null || comision.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("La comisión debe ser mayor o igual que cero.");
        }

        if (comision.stripTrailingZeros().scale() > 2
                || comision.compareTo(new BigDecimal("9999999999.99")) > 0) {
            throw new IllegalArgumentException("La comisión admite hasta 10 dígitos enteros y 2 decimales.");
        }
    }

    public static void validarMonto(BigDecimal monto) {
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor que cero.");
        }

        if (monto.stripTrailingZeros().scale() > 2
                || monto.compareTo(new BigDecimal("9999999999.99")) > 0) {
            throw new IllegalArgumentException("El monto admite hasta 10 dígitos enteros y 2 decimales.");
        }
    }
}
