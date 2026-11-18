package sv.sipas.api;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.LazyInitializationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import sv.sipas.api.entidad.BitacoraOperacion;
import sv.sipas.api.entidad.Caja;
import sv.sipas.api.entidad.Cliente;
import sv.sipas.api.entidad.CuentaServicio;
import sv.sipas.api.entidad.Empleado;
import sv.sipas.api.entidad.Factura;
import sv.sipas.api.entidad.MovimientoCaja;
import sv.sipas.api.entidad.Pago;
import sv.sipas.api.entidad.Proveedor;
import sv.sipas.api.entidad.Rol;
import sv.sipas.api.entidad.Servicio;
import sv.sipas.api.entidad.Sucursal;
import sv.sipas.api.entidad.Usuario;
import sv.sipas.api.entidad.UsuarioRol;
import sv.sipas.api.entidad.enums.EstadoFactura;
import sv.sipas.api.entidad.enums.EstadoSesionCaja;
import sv.sipas.api.entidad.enums.ResultadoOperacion;
import sv.sipas.api.entidad.enums.TipoMovimientoCaja;
import sv.sipas.api.entidad.enums.TipoOperacion;
import sv.sipas.api.repositorio.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:postgresql://localhost:5433/sipas_test",
        "spring.jpa.hibernate.ddl-auto=create"
})
@ActiveProfiles("test")
class PrimerasPruebasTest {

    @Autowired
    private RolRepositorio roles;

    @Autowired
    private ProveedorRepositorio proveedores;

    @Autowired
    private ServicioRepositorio servicios;

    @Autowired
    private SucursalRepositorio sucursales;

    @Autowired
    private CajaRepositorio cajas;

    @Autowired
    private UsuarioRepositorio usuarios;

    @Autowired
    private UsuarioRolRepositorio usuariosRoles;

    @Autowired
    private EmpleadoRepositorio empleados;

    @Autowired
    private ClienteRepositorio clientes;

    @Autowired
    private CuentaServicioRepositorio cuentas;

    @Autowired
    private FacturaRepositorio facturas;

    @Autowired
    private SesionCajaRepositorio sesiones;

    @Autowired
    private PagoRepositorio pagos;

    @Autowired
    private MovimientoCajaRepositorio movimientos;

    @Autowired
    private BitacoraOperacionRepositorio bitacora;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    // Count por cada tabla
    @Test
    void importSqlEntroCompleto() {
        assertEquals(3L, roles.count());
        assertEquals(4L, proveedores.count());
        assertEquals(4L, servicios.count());
        assertEquals(2L, sucursales.count());
        assertEquals(6L, usuarios.count());
        assertEquals(6L, usuariosRoles.count());
        assertEquals(2L, empleados.count());
        assertEquals(4L, clientes.count());
        assertEquals(6L, cuentas.count());
        assertEquals(2L, cajas.count());
        assertEquals(7L, facturas.count());
        assertEquals(3L, sesiones.count());
        assertEquals(3L, pagos.count());
        assertEquals(4L, movimientos.count());
        assertEquals(9L, bitacora.count());
    }

    // Consultas a los repositorios
    @Test
    @Transactional
    void lasConsultasDeCatalogosEncuentran() {
        assertTrue(roles.findByCodigo("ASESOR").isPresent());
        assertTrue(roles.findByCodigo("JEFE").isEmpty());

        assertEquals(4, proveedores.findByActivoTrue().size());
        assertTrue(proveedores.existsByCodigo("AGUA-DEMO"));
        assertFalse(proveedores.existsByCodigo("INEXISTENTE"));

        List<Servicio> delProveedor = servicios.findByProveedor_IdAndActivoTrue(1L);
        assertEquals(1, delProveedor.size());
        assertEquals("AGUA", delProveedor.get(0).getCodigo());
        assertTrue(servicios.findByProveedor_IdAndActivoTrue(999L).isEmpty());
        assertTrue(servicios.existsByProveedor_IdAndCodigo(1L, "AGUA"));
        assertFalse(servicios.existsByProveedor_IdAndCodigo(2L, "AGUA"));

        assertEquals(2, sucursales.findByActivoTrue().size());
        assertTrue(sucursales.existsByCodigo("CENTRAL"));
        assertFalse(sucursales.existsByCodigo("INEXISTENTE"));

        List<Caja> deLaSucursal = cajas.findBySucursal_IdAndActivoTrue(1L);
        assertEquals(1, deLaSucursal.size());
        assertEquals("Caja Central 01", deLaSucursal.get(0).getNombre());
        assertTrue(cajas.findBySucursal_IdAndActivoTrue(999L).isEmpty());
        assertTrue(cajas.existsBySucursal_IdAndCodigo(1L, "CAJA-01"));
        assertFalse(cajas.existsBySucursal_IdAndCodigo(1L, "CAJA-99"));

        Proveedor proveedor = proveedores.findById(1L).orElseThrow();
        proveedor.desactivar();
        proveedores.saveAndFlush(proveedor);
        List<Proveedor> proveedoresActivos = proveedores.findByActivoTrue();
        assertEquals(3, proveedoresActivos.size());
        for (Proveedor activo : proveedoresActivos) {
            assertTrue(activo.getActivo());
        }

        Servicio servicio = servicios.findById(1L).orElseThrow();
        servicio.desactivar();
        servicios.saveAndFlush(servicio);
        assertTrue(servicios.findByProveedor_IdAndActivoTrue(1L).isEmpty());

        Sucursal sucursal = sucursales.findById(1L).orElseThrow();
        sucursal.desactivar();
        sucursales.saveAndFlush(sucursal);
        assertEquals(1, sucursales.findByActivoTrue().size());
        assertEquals("SANTA-ANA", sucursales.findByActivoTrue().get(0).getCodigo());

        Caja caja = cajas.findById(1L).orElseThrow();
        caja.desactivar();
        cajas.saveAndFlush(caja);
        assertTrue(cajas.findBySucursal_IdAndActivoTrue(1L).isEmpty());
    }

    @Test
    void lasConsultasDePersonasEncuentran() {
        assertEquals("Ana Lucia", clientes.findByDui("00000001-8").orElseThrow().getNombres());
        assertTrue(clientes.findByDui("99999999-9").isEmpty());
        assertTrue(clientes.existsByDui("00000001-8"));
        assertFalse(clientes.existsByDui("99999999-9"));
        assertEquals("Ana Lucia", clientes.findByUsuario_Id(4L).orElseThrow().getNombres());
        assertTrue(clientes.findByUsuario_Id(1L).isEmpty());
        assertTrue(clientes.existsByUsuario_Id(4L));
        assertFalse(clientes.existsByUsuario_Id(1L));
        assertNull(clientes.findById(4L).orElseThrow().getUsuario());

        assertEquals("asesor.central", usuarios.findByNombreUsuario("asesor.central").orElseThrow().getNombreUsuario());
        assertTrue(usuarios.findByNombreUsuario("inexistente").isEmpty());
        assertTrue(usuarios.existsByNombreUsuario("admin"));
        assertFalse(usuarios.existsByNombreUsuario("inexistente"));

        assertEquals("EMP-001", empleados.findByUsuario_Id(2L).orElseThrow().getCodigo());
        assertTrue(empleados.findByUsuario_Id(4L).isEmpty());
        assertEquals(1, empleados.findBySucursal_Id(1L).size());
        assertTrue(empleados.findBySucursal_Id(999L).isEmpty());
        assertTrue(empleados.existsByCodigo("EMP-001"));
        assertFalse(empleados.existsByCodigo("EMP-999"));
        assertTrue(empleados.existsByUsuario_Id(2L));
        assertFalse(empleados.existsByUsuario_Id(4L));

        assertEquals(1, usuariosRoles.findByUsuario_Id(2L).size());
        assertTrue(usuariosRoles.findByUsuario_Id(999L).isEmpty());
        assertTrue(usuariosRoles.existsByUsuario_IdAndRol_Id(2L, 2L));
        assertFalse(usuariosRoles.existsByUsuario_IdAndRol_Id(2L, 1L));
        assertTrue(usuariosRoles.existsByUsuario_IdAndRol_Codigo(2L, "ASESOR"));
        assertFalse(usuariosRoles.existsByUsuario_IdAndRol_Codigo(2L, "ADMIN"));
    }

    @Test
    void lasConsultasDeCuentasYFacturasEncuentran() {
        assertEquals(1L, cuentas.findByServicio_IdAndReferencia(1L, "AGUA-0001")
                .orElseThrow().getId());
        assertTrue(cuentas.findByServicio_IdAndReferencia(2L, "AGUA-0001").isEmpty());
        assertTrue(cuentas.existsByServicio_IdAndReferencia(1L, "AGUA-0001"));
        assertFalse(cuentas.existsByServicio_IdAndReferencia(2L, "AGUA-0001"));
        assertEquals(2, cuentas.findByCliente_Id(1L).size());
        assertTrue(cuentas.findByCliente_Id(999L).isEmpty());

        assertEquals(2, facturas.findByCuentaServicio_Id(1L).size());
        assertTrue(facturas.findByCuentaServicio_Id(999L).isEmpty());
        List<Factura> pendientes = facturas.findByCuentaServicio_IdAndEstado(1L, EstadoFactura.PENDIENTE);
        assertEquals(1, pendientes.size());
        assertEquals("AGUA-202610-001", pendientes.get(0).getNumeroFactura());
        assertTrue(facturas.findByCuentaServicio_IdAndEstado(2L, EstadoFactura.PENDIENTE).isEmpty());

        List<Factura> deAna = facturas.findByCuentaServicio_Cliente_Id(1L);
        assertEquals(3, deAna.size());
        for (Factura factura : deAna) {
            CuentaServicio cuenta = cuentas.findById(factura.getCuentaServicio().getId()).orElseThrow();
            assertEquals(1L, cuenta.getCliente().getId());
        }
        assertTrue(facturas.findByCuentaServicio_Cliente_Id(999L).isEmpty());
    }

    @Test
    void lasConsultasDeSesionesYPagosEncuentran() {
        assertTrue(sesiones.existsByCaja_IdAndEstado(1L, EstadoSesionCaja.ABIERTA));
        assertFalse(sesiones.existsByCaja_IdAndEstado(999L, EstadoSesionCaja.ABIERTA));
        assertTrue(sesiones.existsByEmpleado_IdAndEstado(1L, EstadoSesionCaja.ABIERTA));
        assertFalse(sesiones.existsByEmpleado_IdAndEstado(999L, EstadoSesionCaja.ABIERTA));
        assertFalse(sesiones.existsByCaja_IdAndEstado(2L, EstadoSesionCaja.CERRADA));
        assertFalse(sesiones.existsByEmpleado_IdAndEstado(2L, EstadoSesionCaja.CERRADA));
        assertEquals(1, sesiones.findByEmpleado_IdAndEstado(1L, EstadoSesionCaja.ABIERTA).size());
        assertEquals(2L, sesiones.findByEmpleado_IdAndEstado(1L, EstadoSesionCaja.ABIERTA).get(0).getId());
        assertTrue(sesiones.findByEmpleado_IdAndEstado(2L, EstadoSesionCaja.CERRADA).isEmpty());

        assertTrue(pagos.existsByFactura_Id(1L));
        assertFalse(pagos.existsByFactura_Id(2L));
        assertEquals(1L, pagos.findByFactura_Id(1L).orElseThrow().getId());
        assertTrue(pagos.findByFactura_Id(2L).isEmpty());
        assertEquals(1L, pagos.findByNumeroComprobante("SIPAS-20261022-000001")
                .orElseThrow().getId());
        assertTrue(pagos.findByNumeroComprobante("INEXISTENTE").isEmpty());
        assertEquals(2, pagos.findBySesionCaja_Id(1L).size());
        assertTrue(pagos.findBySesionCaja_Id(2L).isEmpty());
        assertEquals(2, pagos.findBySesionCaja_Empleado_Id(1L).size());
        assertTrue(pagos.findBySesionCaja_Empleado_Id(999L).isEmpty());

        List<Pago> deAna = pagos.findByFactura_CuentaServicio_Cliente_Id(1L);
        assertEquals(2, deAna.size());
        for (Pago pago : deAna) {
            Factura factura = facturas.findById(pago.getFactura().getId()).orElseThrow();
            CuentaServicio cuenta = cuentas.findById(factura.getCuentaServicio().getId()).orElseThrow();
            assertEquals(1L, cuenta.getCliente().getId());
        }
        assertTrue(pagos.findByFactura_CuentaServicio_Cliente_Id(4L).isEmpty());
    }

    @Test
    void lasConsultasDeBitacoraFiltranYOrdenan() {
        List<BitacoraOperacion> delAsesor =
                bitacora.findByUsuario_IdOrderByFechaHoraDesc(2L);
        assertEquals(6, delAsesor.size());
        assertEquals(9L, delAsesor.get(0).getId());
        for (int i = 1; i < delAsesor.size(); i++) {
            assertFalse(delAsesor.get(i).getFechaHora().isAfter(delAsesor.get(i - 1).getFechaHora()));
        }
        assertTrue(bitacora.findByUsuario_IdOrderByFechaHoraDesc(999L).isEmpty());

        assertEquals(1, bitacora.findByTipoOperacionAndResultadoOrderByFechaHoraDesc(TipoOperacion.COBRO_FACTURA, ResultadoOperacion.FALLIDO).size());
        assertEquals(9L, bitacora.findByTipoOperacionAndResultadoOrderByFechaHoraDesc(TipoOperacion.COBRO_FACTURA, ResultadoOperacion.FALLIDO).get(0).getId());
        assertTrue(bitacora.findByTipoOperacionAndResultadoOrderByFechaHoraDesc(TipoOperacion.APERTURA_CAJA, ResultadoOperacion.FALLIDO).isEmpty());

        assertEquals(2, bitacora.findByEntidadAfectadaAndRegistroAfectadoIdOrderByFechaHoraDesc("sesion_caja", 1L).size());
        assertTrue(bitacora.findByEntidadAfectadaAndRegistroAfectadoIdOrderByFechaHoraDesc("factura", 999L).isEmpty());
    }

    // @Query da el numero correcto
    @Test
    void laQuerySumaBienYDevuelveCeroSinMovimientos() {
        BigDecimal ingresos = movimientos.sumarPorSesionYTipo(1L, TipoMovimientoCaja.INGRESO_COBRO);
        BigDecimal retiros = movimientos.sumarPorSesionYTipo(1L, TipoMovimientoCaja.RETIRO);
        assertEquals(0, new BigDecimal("66.00").compareTo(ingresos));
        assertEquals(0, new BigDecimal("20.00").compareTo(retiros));
        assertEquals(0, new BigDecimal("12.50").compareTo(movimientos.sumarPorSesionYTipo(3L, TipoMovimientoCaja.INGRESO_COBRO)));
        assertEquals(0, BigDecimal.ZERO.compareTo(movimientos.sumarPorSesionYTipo(2L, TipoMovimientoCaja.INGRESO_COBRO)));
        assertEquals(0, BigDecimal.ZERO.compareTo(movimientos.sumarPorSesionYTipo(999L, TipoMovimientoCaja.RETIRO)));

        BigDecimal fondoInicial = sesiones.findById(1L).orElseThrow().getFondoInicial();
        BigDecimal saldoEsperado = fondoInicial.add(ingresos).subtract(retiros);
        assertEquals(0, new BigDecimal("146.00").compareTo(saldoEsperado));
        assertEquals(0, saldoEsperado.compareTo(sesiones.findById(1L).orElseThrow().getEfectivoContado()));

        List<MovimientoCaja> deLaSesion = movimientos.findBySesionCaja_IdOrderByFechaHoraAsc(1L);
        assertEquals(3, deLaSesion.size());
        assertEquals(1L, deLaSesion.get(0).getId());
        assertEquals(2L, deLaSesion.get(1).getId());
        assertEquals(3L, deLaSesion.get(2).getId());
        assertTrue(movimientos.findBySesionCaja_IdOrderByFechaHoraAsc(2L).isEmpty());
    }

    // Cada UNIQUE de el diagrama
    @Test
    void noPermiteDuiDuplicado() {
        Cliente repetido = new Cliente(null, "00000001-8", "Otra", "Persona", null, null);
        assertThrows(DataIntegrityViolationException.class, () -> clientes.saveAndFlush(repetido));
        assertEquals(4L, clientes.count());
    }

    @Test
    void noPermiteUsuarioDuplicadoEnCliente() {
        Usuario usuario = usuarios.findById(4L).orElseThrow();
        Cliente repetido = new Cliente(usuario, "99999991-5", "Otro", "Cliente", null, null);
        assertThrows(DataIntegrityViolationException.class, () -> clientes.saveAndFlush(repetido));
        assertEquals(4L, clientes.count());
    }

    @Test
    void noPermiteCodigoProveedorDuplicado() {
        Proveedor repetido = new Proveedor("AGUA-DEMO", "Otro proveedor");
        assertThrows(DataIntegrityViolationException.class, () -> proveedores.saveAndFlush(repetido));
        assertEquals(4L, proveedores.count());
    }

    @Test
    void noPermiteCodigoServicioDuplicadoEnProveedor() {
        Proveedor proveedor = proveedores.findById(1L).orElseThrow();
        Servicio repetido = new Servicio(proveedor, "AGUA", "Otro servicio", new BigDecimal("1.00"));
        assertThrows(DataIntegrityViolationException.class, () -> servicios.saveAndFlush(repetido));
        assertEquals(4L, servicios.count());
    }

    @Test
    void noPermiteReferenciaDuplicadaEnServicio() {
        Cliente cliente = clientes.findById(2L).orElseThrow();
        Servicio servicio = servicios.findById(1L).orElseThrow();
        CuentaServicio repetida = new CuentaServicio(cliente, servicio, "AGUA-0001", "Otra cuenta");
        assertThrows(DataIntegrityViolationException.class, () -> cuentas.saveAndFlush(repetida));
        assertEquals(6L, cuentas.count());
    }

    @Test
    void noPermiteNumeroFacturaDuplicadoEnCuenta() {
        CuentaServicio cuenta = cuentas.findById(1L).orElseThrow();
        Factura repetida = new Factura(cuenta, "AGUA-202609-001", LocalDate.of(2026, 9, 1),
                new BigDecimal("10.00"), LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 15));
        assertThrows(DataIntegrityViolationException.class, () -> facturas.saveAndFlush(repetida));
        assertEquals(7L, facturas.count());
    }

    @Test
    void noPermiteDosPagosParaUnaFactura() {
        Pago repetido = new Pago(facturas.findById(1L).orElseThrow(),
                sesiones.findById(2L).orElseThrow(), "PRUEBA-FACTURA-DUPLICADA",
                new BigDecimal("1.00"), new BigDecimal("20.00"));
        assertThrows(DataIntegrityViolationException.class, () -> pagos.saveAndFlush(repetido));
        assertEquals(3L, pagos.count());
    }

    @Test
    void noPermiteComprobanteDuplicado() {
        Pago repetido = new Pago(facturas.findById(2L).orElseThrow(),
                sesiones.findById(2L).orElseThrow(), "SIPAS-20261022-000001",
                new BigDecimal("1.00"), new BigDecimal("30.00"));
        assertThrows(DataIntegrityViolationException.class, () -> pagos.saveAndFlush(repetido));
        assertEquals(3L, pagos.count());
    }

    @Test
    void noPermiteCodigoSucursalDuplicado() {
        Sucursal repetida = new Sucursal("CENTRAL", "Otra sucursal", "Direccion de prueba");
        assertThrows(DataIntegrityViolationException.class, () -> sucursales.saveAndFlush(repetida));
        assertEquals(2L, sucursales.count());
    }

    @Test
    void noPermiteCodigoCajaDuplicadoEnSucursal() {
        Sucursal sucursal = sucursales.findById(1L).orElseThrow();
        Caja repetida = new Caja(sucursal, "CAJA-01", "Otra caja");
        assertThrows(DataIntegrityViolationException.class, () -> cajas.saveAndFlush(repetida));
        assertEquals(2L, cajas.count());
    }

    @Test
    void noPermiteDosMovimientosParaUnPago() {
        assertThrows(DataIntegrityViolationException.class, () -> jdbc.update(
                "insert into movimiento_caja (sesion_caja_id, pago_id, usuario_id, tipo, monto, fecha_hora) "
                        + "values (1, 1, 2, 'INGRESO_COBRO', 19.50, now())"));
        assertEquals(4L, movimientos.count());
    }

    @Test
    void noPermiteUsuarioDuplicadoEnEmpleado() {
        Usuario usuario = usuarios.findById(2L).orElseThrow();
        Sucursal sucursal = sucursales.findById(1L).orElseThrow();
        Empleado repetido = new Empleado(usuario, sucursal, "EMP-003", "Otro", "Empleado");
        assertThrows(DataIntegrityViolationException.class, () -> empleados.saveAndFlush(repetido));
        assertEquals(2L, empleados.count());
    }

    @Test
    void noPermiteCodigoEmpleadoDuplicado() {
        Usuario usuario = usuarios.findById(1L).orElseThrow();
        Sucursal sucursal = sucursales.findById(1L).orElseThrow();
        Empleado repetido = new Empleado(usuario, sucursal, "EMP-001", "Otro", "Empleado");
        assertThrows(DataIntegrityViolationException.class, () -> empleados.saveAndFlush(repetido));
        assertEquals(2L, empleados.count());
    }

    @Test
    void noPermiteNombreUsuarioDuplicado() {
        String claveHash = usuarios.findById(1L).orElseThrow().getClaveHash();
        Usuario repetido = new Usuario("admin", claveHash);
        assertThrows(DataIntegrityViolationException.class, () -> usuarios.saveAndFlush(repetido));
        assertEquals(6L, usuarios.count());
    }

    @Test
    void noPermiteCodigoRolDuplicado() {
        Rol repetido = new Rol("ADMIN", "Otro administrador");
        assertThrows(DataIntegrityViolationException.class, () -> roles.saveAndFlush(repetido));
        assertEquals(3L, roles.count());
    }

    @Test
    void noPermiteAsignacionUsuarioRolDuplicada() {
        Usuario usuario = usuarios.findById(2L).orElseThrow();
        Rol rol = roles.findById(2L).orElseThrow();
        UsuarioRol repetido = new UsuarioRol(usuario, rol);
        assertThrows(DataIntegrityViolationException.class, () -> usuariosRoles.saveAndFlush(repetido));
        assertEquals(6L, usuariosRoles.count());
    }

    @Test
    @Transactional
    void lasCombinacionesDiferentesSiSePermiten() {
        Servicio servicio = new Servicio(proveedores.findById(2L).orElseThrow(),
                "AGUA", "Otro servicio de agua", new BigDecimal("1.00"));
        assertNotNull(servicios.saveAndFlush(servicio).getId());

        CuentaServicio cuenta = new CuentaServicio(clientes.findById(1L).orElseThrow(),
                servicios.findById(2L).orElseThrow(), "AGUA-0001", "Otra cuenta de servicio");
        assertNotNull(cuentas.saveAndFlush(cuenta).getId());

        Factura factura = new Factura(cuentas.findById(3L).orElseThrow(),
                "AGUA-202609-001", LocalDate.of(2026, 9, 1), new BigDecimal("10.00"),
                LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 15));
        assertNotNull(facturas.saveAndFlush(factura).getId());

        assertTrue(cajas.existsBySucursal_IdAndCodigo(1L, "CAJA-01"));
        assertTrue(cajas.existsBySucursal_IdAndCodigo(2L, "CAJA-01"));

    }

    @Test
    void noPermiteDosRolesParaElMismoUsuario() {
        Usuario usuario = usuarios.findById(2L).orElseThrow();
        Rol otroRol = roles.findById(1L).orElseThrow();
        UsuarioRol asignacion = new UsuarioRol(usuario, otroRol);

        assertThrows(DataIntegrityViolationException.class,
                () -> usuariosRoles.saveAndFlush(asignacion));
        assertEquals(6L, usuariosRoles.count());
    }

    @Test
    void laTablaNoPermiteNombresNulos() {
        assertThrows(DataIntegrityViolationException.class, () -> jdbc.update(
                "insert into cliente (dui, nombres, apellidos, activo, creado_en) values (?, null, ?, true, now())",
                "99999998-1", "Persona"));
        assertEquals(4L, clientes.count());
    }

    @Test
    @Transactional
    void laSecuenciaPermiteInsertarUnClienteNuevo() {
        Cliente nuevo = new Cliente(null, "99999997-3", "Cliente", "De prueba", null, null);
        Cliente guardado = clientes.saveAndFlush(nuevo);
        assertTrue(guardado.getId() > 4L);
        assertEquals(5L, clientes.count());
    }

    // Relacion LAZY
    @Test
    void lazyFueraDeUnaTransaccionYConJoinFetch() {
        Factura sinCuenta = facturas.findById(1L).orElseThrow();
        assertThrows(LazyInitializationException.class,
                () -> sinCuenta.getCuentaServicio().getReferencia());

        Factura conCuenta;
        try (EntityManager em = entityManagerFactory.createEntityManager()) {
            conCuenta = em.createQuery(
                            "select f from Factura f join fetch f.cuentaServicio where f.id = :id", Factura.class)
                    .setParameter("id", 1L)
                    .getSingleResult();
        }
        assertEquals("AGUA-0001", conCuenta.getCuentaServicio().getReferencia());
    }

    // El estado se lea como enum
    @Test
    void losEnumsQuedanComoTextoEnLasTablas() {
        Factura factura = facturas.findById(2L).orElseThrow();
        assertEquals(EstadoFactura.PENDIENTE, factura.getEstado());
        assertNotNull(factura.getCreadoEn());
        assertEquals("PENDIENTE", jdbc.queryForObject("select estado from factura where id = 2", String.class));
        assertEquals("INACTIVA", jdbc.queryForObject("select estado from cuenta_servicio where id = 6", String.class));
        assertEquals("CERRADA", jdbc.queryForObject("select estado from sesion_caja where id = 1", String.class));
        assertEquals("RETIRO", jdbc.queryForObject("select tipo from movimiento_caja where id = 3", String.class));
        assertEquals("COBRO_FACTURA", jdbc.queryForObject("select tipo_operacion from bitacora_operacion where id = 9", String.class));
        assertEquals("FALLIDO", jdbc.queryForObject("select resultado from bitacora_operacion where id = 9", String.class));
    }
}
