package sv.sipas.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RestriccionesBaseDatosTest {

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void facturaRechazaMontoCero() {
        DataIntegrityViolationException error = assertThrows(DataIntegrityViolationException.class,
                () -> jdbc.update("UPDATE factura SET monto = 0 WHERE id = 2"));

        assertTrue(error.getMostSpecificCause().getMessage().contains("ck_factura_monto_positivo"));
    }

    @Test
    void movimientoRechazaMontoCero() {
        DataIntegrityViolationException error = assertThrows(DataIntegrityViolationException.class,
                () -> jdbc.update("UPDATE movimiento_caja SET monto = 0 WHERE id = 1"));

        assertTrue(error.getMostSpecificCause().getMessage().contains("ck_movimiento_monto_positivo"));
    }

    @Test
    void pagoRechazaEfectivoCero() {
        DataIntegrityViolationException error = assertThrows(DataIntegrityViolationException.class,
                () -> jdbc.update("UPDATE pago SET efectivo_recibido = 0 WHERE id = 1"));

        assertTrue(error.getMostSpecificCause().getMessage().contains("ck_pago_efectivo_positivo"));
    }

    @Test
    void servicioRechazaComisionNegativa() {
        DataIntegrityViolationException error = assertThrows(DataIntegrityViolationException.class,
                () -> jdbc.update("UPDATE servicio SET comision = -1 WHERE id = 1"));

        assertTrue(error.getMostSpecificCause().getMessage().contains("ck_servicio_comision_no_negativa"));
    }

    @Test
    void pagoRechazaComisionNegativa() {
        DataIntegrityViolationException error = assertThrows(DataIntegrityViolationException.class,
                () -> jdbc.update("UPDATE pago SET comision_aplicada = -1 WHERE id = 1"));

        assertTrue(error.getMostSpecificCause().getMessage().contains("ck_pago_comision_no_negativa"));
    }

    @Test
    void sesionRechazaFondoNegativo() {
        DataIntegrityViolationException error = assertThrows(DataIntegrityViolationException.class,
                () -> jdbc.update("UPDATE sesion_caja SET fondo_inicial = -1 WHERE id = 2"));

        assertTrue(error.getMostSpecificCause().getMessage().contains("ck_sesion_fondo_no_negativo"));
    }

    @Test
    void servicioPermiteComisionCero() {
        assertEquals(1, jdbc.update("UPDATE servicio SET comision = 0 WHERE id = 1"));
    }

    @Test
    void pagoPermiteComisionCero() {
        assertEquals(1, jdbc.update("UPDATE pago SET comision_aplicada = 0 WHERE id = 1"));
    }

    @Test
    void sesionPermiteFondoCero() {
        assertEquals(1, jdbc.update("UPDATE sesion_caja SET fondo_inicial = 0 WHERE id = 2"));
    }

    @Test
    void clienteRechazaDuiConFormatoInvalido() {
        DataIntegrityViolationException error = assertThrows(DataIntegrityViolationException.class,
                () -> jdbc.update("UPDATE cliente SET dui = '12345678' WHERE id = 4"));

        assertTrue(error.getMostSpecificCause().getMessage().contains("ck_cliente_dui_formato"));
    }

    @Test
    void clientePermiteDuiConFormatoValido() {
        assertEquals(1, jdbc.update("UPDATE cliente SET dui = '12345678-9' WHERE id = 4"));
    }

    @Test
    void ingresoRechazaPagoNulo() {
        DataIntegrityViolationException error = assertThrows(DataIntegrityViolationException.class,
                () -> jdbc.update("UPDATE movimiento_caja SET pago_id = NULL WHERE id = 1"));

        assertTrue(error.getMostSpecificCause().getMessage().contains("ck_movimiento_tipo_datos"));
    }

    @Test
    void retiroRechazaPagoAsociado() {
        DataIntegrityViolationException error = assertThrows(DataIntegrityViolationException.class,
                () -> jdbc.update("""
                        UPDATE movimiento_caja
                        SET tipo = 'RETIRO', motivo = 'Retiro de prueba'
                        WHERE id = 1
                        """));

        assertTrue(error.getMostSpecificCause().getMessage().contains("ck_movimiento_tipo_datos"));
    }

    @Test
    void retiroRechazaMotivoNulo() {
        DataIntegrityViolationException error = assertThrows(DataIntegrityViolationException.class,
                () -> jdbc.update("UPDATE movimiento_caja SET motivo = NULL WHERE id = 3"));

        assertTrue(error.getMostSpecificCause().getMessage().contains("ck_movimiento_tipo_datos"));
    }

    @Test
    void retiroRechazaMotivoVacio() {
        DataIntegrityViolationException error = assertThrows(DataIntegrityViolationException.class,
                () -> jdbc.update("UPDATE movimiento_caja SET motivo = '' WHERE id = 3"));

        assertTrue(error.getMostSpecificCause().getMessage().contains("ck_movimiento_tipo_datos"));
    }

    @Test
    void retiroRechazaMotivoSoloConEspacios() {
        DataIntegrityViolationException error = assertThrows(DataIntegrityViolationException.class,
                () -> jdbc.update("UPDATE movimiento_caja SET motivo = '   ' WHERE id = 3"));

        assertTrue(error.getMostSpecificCause().getMessage().contains("ck_movimiento_tipo_datos"));
    }

    @Test
    void retiroRechazaMotivoSoloConTabulacion() {
        DataIntegrityViolationException error = assertThrows(DataIntegrityViolationException.class,
                () -> jdbc.update("UPDATE movimiento_caja SET motivo = chr(9) WHERE id = 3"));

        assertTrue(error.getMostSpecificCause().getMessage().contains("ck_movimiento_tipo_datos"));
    }

    @Test
    void sesionCerradaRechazaEfectivoNegativo() {
        DataIntegrityViolationException error = assertThrows(DataIntegrityViolationException.class,
                () -> jdbc.update("UPDATE sesion_caja SET efectivo_contado = -1 WHERE id = 1"));

        assertTrue(error.getMostSpecificCause().getMessage().contains("ck_sesion_datos_cierre"));
    }

    @Test
    void sesionCerradaRechazaEfectivoNulo() {
        DataIntegrityViolationException error = assertThrows(DataIntegrityViolationException.class,
                () -> jdbc.update("UPDATE sesion_caja SET efectivo_contado = NULL WHERE id = 1"));

        assertTrue(error.getMostSpecificCause().getMessage().contains("ck_sesion_datos_cierre"));
    }

    @Test
    void sesionCerradaRechazaFechaCierreNula() {
        DataIntegrityViolationException error = assertThrows(DataIntegrityViolationException.class,
                () -> jdbc.update("UPDATE sesion_caja SET fecha_cierre = NULL WHERE id = 1"));

        assertTrue(error.getMostSpecificCause().getMessage().contains("ck_sesion_datos_cierre"));
    }

    @Test
    void sesionCerradaRechazaCierreAnteriorAApertura() {
        DataIntegrityViolationException error = assertThrows(DataIntegrityViolationException.class,
                () -> jdbc.update("""
                        UPDATE sesion_caja
                        SET fecha_cierre = fecha_apertura - interval '1 second'
                        WHERE id = 1
                        """));

        assertTrue(error.getMostSpecificCause().getMessage().contains("ck_sesion_datos_cierre"));
    }

    @Test
    void sesionAbiertaRechazaEfectivoContado() {
        DataIntegrityViolationException error = assertThrows(DataIntegrityViolationException.class,
                () -> jdbc.update("UPDATE sesion_caja SET efectivo_contado = 0 WHERE id = 2"));

        assertTrue(error.getMostSpecificCause().getMessage().contains("ck_sesion_datos_cierre"));
    }

    @Test
    void sesionAbiertaRechazaFechaCierre() {
        DataIntegrityViolationException error = assertThrows(DataIntegrityViolationException.class,
                () -> jdbc.update("""
                        UPDATE sesion_caja
                        SET fecha_cierre = fecha_apertura
                        WHERE id = 2
                        """));

        assertTrue(error.getMostSpecificCause().getMessage().contains("ck_sesion_datos_cierre"));
    }

    @Test
    void facturaRechazaVencimientoAnteriorAEmision() {
        DataIntegrityViolationException error = assertThrows(DataIntegrityViolationException.class,
                () -> jdbc.update("""
                        UPDATE factura
                        SET fecha_vencimiento = fecha_emision - 1
                        WHERE id = 2
                        """));

        assertTrue(error.getMostSpecificCause().getMessage().contains("ck_factura_fechas"));
    }

    @Test
    void ingresoPermiteMotivoNulo() {
        assertEquals(1, jdbc.update("UPDATE movimiento_caja SET motivo = NULL WHERE id = 1"));
    }

    @Test
    void cierrePermiteCeroYFechaIgualAApertura() {
        assertEquals(1, jdbc.update("""
                UPDATE sesion_caja
                SET estado = 'CERRADA',
                    efectivo_contado = 0,
                    fecha_cierre = fecha_apertura
                WHERE id = 2
                """));
    }

    @Test
    void facturaPermiteVencimientoIgualAEmision() {
        assertEquals(1, jdbc.update("""
                UPDATE factura
                SET fecha_vencimiento = fecha_emision
                WHERE id = 2
                """));
    }
}
