package sv.sipas.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import sv.sipas.api.excepcion.OperacionRechazadaException;
import sv.sipas.api.repositorio.SesionCajaRepositorio;
import sv.sipas.api.servicio.SesionCajaServicio;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:postgresql://localhost:5433/sipas_test",
        "spring.jpa.hibernate.ddl-auto=create"
})
@ActiveProfiles("test")
class SesionCajaServicioTest {

    @Autowired
    private SesionCajaServicio servicio;

    @Autowired
    private SesionCajaRepositorio sesiones;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void noPermiteAbrirOtraSesionEnUnaCajaOcupada() {
        long sesionesAntes = sesiones.count();

        OperacionRechazadaException error = assertThrows(OperacionRechazadaException.class,
                () -> servicio.abrirCaja(1L, 2L, new BigDecimal("100.00")));

        assertEquals("La caja ya tiene una sesión abierta.", error.getMessage());
        assertEquals(sesionesAntes, sesiones.count());
    }

    @Test
    @Transactional
    void baseRechazaDosSesionesAbiertasEnLaMismaCaja() {
        DataIntegrityViolationException error = assertThrows(DataIntegrityViolationException.class,
                () -> jdbc.update("UPDATE sesion_caja SET caja_id = 1 WHERE id = 3"));

        assertTrue(error.getMostSpecificCause().getMessage().contains("uk_sesion_caja_abierta_caja"));
    }

    @Test
    @Transactional
    void baseRechazaDosSesionesAbiertasDelMismoEmpleado() {
        DataIntegrityViolationException error = assertThrows(DataIntegrityViolationException.class,
                () -> jdbc.update("UPDATE sesion_caja SET empleado_id = 1 WHERE id = 3"));

        assertTrue(error.getMostSpecificCause().getMessage().contains("uk_sesion_caja_abierta_empleado"));
    }
}
