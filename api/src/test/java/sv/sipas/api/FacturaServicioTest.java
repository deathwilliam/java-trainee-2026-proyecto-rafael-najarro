package sv.sipas.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import sv.sipas.api.dto.CobroRespuesta;
import sv.sipas.api.entidad.BitacoraOperacion;
import sv.sipas.api.entidad.MovimientoCaja;
import sv.sipas.api.entidad.enums.EstadoFactura;
import sv.sipas.api.entidad.enums.ResultadoOperacion;
import sv.sipas.api.entidad.enums.TipoOperacion;
import sv.sipas.api.excepcion.SaldoInsuficienteException;
import sv.sipas.api.repositorio.BitacoraOperacionRepositorio;
import sv.sipas.api.repositorio.FacturaRepositorio;
import sv.sipas.api.repositorio.MovimientoCajaRepositorio;
import sv.sipas.api.repositorio.PagoRepositorio;
import sv.sipas.api.servicio.CobroServicio;
import sv.sipas.api.servicio.FacturaServicio;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:postgresql://localhost:5433/sipas_test",
        "spring.jpa.hibernate.ddl-auto=create"
})
@ActiveProfiles("test")
class FacturaServicioTest {

    @Autowired
    private FacturaServicio facturaServicio;

    @Autowired
    private FacturaRepositorio facturaRepositorio;

    @Autowired
    private PagoRepositorio pagoRepositorio;

    @Autowired
    private BitacoraOperacionRepositorio bitacoraOperacionRepositorio;

    @MockitoSpyBean
    private MovimientoCajaRepositorio movimientoCajaRepositorio;

    @Autowired
    private CobroServicio cobroServicio;

    @Test
    void siFallaElMovimientoSeDeshaceTodoElCobro() {
        EstadoFactura estadoAntes = facturaRepositorio.findById(4L).orElseThrow().getEstado();
        long pagosAntes = pagoRepositorio.count();
        long movimientosAntes = movimientoCajaRepositorio.count();
        long bitacorasAntes = bitacoraOperacionRepositorio.count();

        doAnswer(invocacion -> {
            assertTrue(pagoRepositorio.existsByFactura_Id(4L));
            assertEquals(EstadoFactura.PAGADA, facturaRepositorio.findById(4L).orElseThrow().getEstado());
            throw new IllegalStateException("Falla simulada al guardar el movimiento.");
        }).when(movimientoCajaRepositorio).save(any(MovimientoCaja.class));

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> facturaServicio.cobrarFactura(2L, 4L, 2L, new BigDecimal("40.00")));

        assertEquals("Falla simulada al guardar el movimiento.", error.getMessage());
        assertEquals(estadoAntes, facturaRepositorio.findById(4L).orElseThrow().getEstado());
        assertFalse(pagoRepositorio.existsByFactura_Id(4L));
        assertEquals(pagosAntes, pagoRepositorio.count());
        assertEquals(movimientosAntes, movimientoCajaRepositorio.count());
        assertEquals(bitacorasAntes, bitacoraOperacionRepositorio.count());
    }

    @Test
    void siElEfectivoEsInsuficienteNoSeRegistraElCobro() {
        EstadoFactura estadoAntes = facturaRepositorio.findById(6L).orElseThrow().getEstado();
        long pagosAntes = pagoRepositorio.count();
        long movimientosAntes = movimientoCajaRepositorio.count();
        long bitacorasAntes = bitacoraOperacionRepositorio.count();

        // La factura cuesta 15.25 y la comisión es 1.00: necesita 16.25.
        assertThrows(SaldoInsuficienteException.class,
                () -> facturaServicio.cobrarFactura(2L, 6L, 2L, new BigDecimal("10.00")));

        assertEquals(estadoAntes, facturaRepositorio.findById(6L).orElseThrow().getEstado());
        assertFalse(pagoRepositorio.existsByFactura_Id(6L));
        assertEquals(pagosAntes, pagoRepositorio.count());
        assertEquals(movimientosAntes, movimientoCajaRepositorio.count());
        assertEquals(bitacorasAntes, bitacoraOperacionRepositorio.count());
    }

    @Test
    void siTodoSaleBienSeRegistraElCobroCompleto() {
        long pagosAntes = pagoRepositorio.count();
        long movimientosAntes = movimientoCajaRepositorio.count();
        long bitacorasAntes = bitacoraOperacionRepositorio.count();

        CobroRespuesta respuesta =
                facturaServicio.cobrarFactura(2L, 2L, 2L, new BigDecimal("30.00"));

        assertEquals(EstadoFactura.PAGADA, facturaRepositorio.findById(2L).orElseThrow().getEstado());
        assertEquals(pagosAntes + 1, pagoRepositorio.count());
        assertEquals(movimientosAntes + 1, movimientoCajaRepositorio.count());
        assertEquals(bitacorasAntes + 1, bitacoraOperacionRepositorio.count());
        assertEquals(pagoRepositorio.findByFactura_Id(2L).orElseThrow().getNumeroComprobante(),
                respuesta.numeroComprobante());

        // Factura de 22.00 + comisión de 1.00; entrega 30.00 y recibe 7.00.
        assertEquals(0, new BigDecimal("23.00").compareTo(respuesta.totalCobrado()));
        assertEquals(0, new BigDecimal("7.00").compareTo(respuesta.cambio()));

        MovimientoCaja ingreso =
                movimientoCajaRepositorio.findBySesionCaja_IdOrderByFechaHoraAsc(2L).getFirst();
        assertEquals(0, new BigDecimal("23.00").compareTo(ingreso.getMonto()));
        assertEquals(pagoRepositorio.findByFactura_Id(2L).orElseThrow().getId(),
                ingreso.getPago().getId());
    }

    @Test
    void siFallaElCobroSeConservaLaBitacoraDelIntentoFallido() {
        long pagosAntes = pagoRepositorio.count();
        long movimientosAntes = movimientoCajaRepositorio.count();
        long bitacorasAntes = bitacoraOperacionRepositorio.count();

        doAnswer(invocacion -> {
            assertTrue(pagoRepositorio.existsByFactura_Id(4L));
            assertEquals(EstadoFactura.PAGADA,
                    facturaRepositorio.findById(4L).orElseThrow().getEstado());

            throw new IllegalStateException("Falla simulada al guardar el movimiento.");
        }).when(movimientoCajaRepositorio).save(any(MovimientoCaja.class));

        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> cobroServicio.cobrarFactura(2L, 4L, 2L, new BigDecimal("40.00")));

        assertEquals("Falla simulada al guardar el movimiento.", error.getMessage());
        assertEquals(EstadoFactura.PENDIENTE, facturaRepositorio.findById(4L).orElseThrow().getEstado());
        assertFalse(pagoRepositorio.existsByFactura_Id(4L));
        assertEquals(pagosAntes, pagoRepositorio.count());
        assertEquals(movimientosAntes, movimientoCajaRepositorio.count());

        assertEquals(bitacorasAntes + 1, bitacoraOperacionRepositorio.count());

        BitacoraOperacion registro = bitacoraOperacionRepositorio
                .findByEntidadAfectadaAndRegistroAfectadoIdOrderByFechaHoraDesc(
                        "factura", 4L)
                .getFirst();

        assertEquals(ResultadoOperacion.FALLIDO, registro.getResultado());
        assertEquals(TipoOperacion.COBRO_FACTURA, registro.getTipoOperacion());
        assertEquals(error.getMessage(), registro.getDescripcion());
        assertEquals(2L, registro.getUsuario().getId());
    }
}
