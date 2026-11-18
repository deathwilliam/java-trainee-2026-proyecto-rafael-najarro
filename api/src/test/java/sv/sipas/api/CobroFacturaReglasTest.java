package sv.sipas.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import sv.sipas.api.dto.CobroRespuesta;
import sv.sipas.api.entidad.*;
import sv.sipas.api.entidad.enums.ResultadoOperacion;
import sv.sipas.api.entidad.enums.TipoMovimientoCaja;
import sv.sipas.api.excepcion.OperacionRechazadaException;
import sv.sipas.api.excepcion.SaldoInsuficienteException;
import sv.sipas.api.repositorio.*;
import sv.sipas.api.servicio.BitacoraOperacionServicio;
import sv.sipas.api.servicio.CobroServicio;
import sv.sipas.api.servicio.FacturaServicio;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CobroFacturaReglasTest {

    @Mock
    private FacturaRepositorio facturaRepositorio;
    @Mock
    private SesionCajaRepositorio sesionCajaRepositorio;
    @Mock
    private PagoRepositorio pagoRepositorio;
    @Mock
    private MovimientoCajaRepositorio movimientoCajaRepositorio;
    @Mock
    private UsuarioRepositorio usuarioRepositorio;
    @Mock
    private UsuarioRolRepositorio usuarioRolRepositorio;
    @Mock
    private BitacoraOperacionRepositorio bitacoraOperacionRepositorio;
    @Mock
    private BitacoraOperacionServicio bitacoraOperacionServicio;
    @Mock
    private SesionCaja sesionCaja;
    @Mock
    private Factura factura;
    @Mock
    private Usuario usuario;
    @Mock
    private CuentaServicio cuentaServicio;
    @Mock
    private Servicio servicio;
    @Mock
    private Proveedor proveedor;
    @Mock
    private Empleado empleado;
    @Mock
    private Caja caja;
    @Mock
    private Sucursal sucursal;

    @InjectMocks
    private FacturaServicio facturaServicio;

    private CobroServicio cobroServicio;

    @BeforeEach
    void prepararCobro() {
        cobroServicio = new CobroServicio(facturaServicio, bitacoraOperacionServicio);

        when(sesionCajaRepositorio.buscarParaOperacion(1L)).thenReturn(Optional.of(sesionCaja));
        when(facturaRepositorio.buscarParaCobro(1L)).thenReturn(Optional.of(factura));
        when(usuarioRepositorio.findById(2L)).thenReturn(Optional.of(usuario));
    }

    @Test
    void cobroConEfectivoExactoGuardaPagoMovimientoYBitacora() {
        when(factura.estaPendiente()).thenReturn(true);

        when(factura.getCuentaServicio()).thenReturn(cuentaServicio);
        when(cuentaServicio.estaActiva()).thenReturn(true);

        when(cuentaServicio.getServicio()).thenReturn(servicio);
        when(servicio.getActivo()).thenReturn(true);
        when(servicio.getProveedor()).thenReturn(proveedor);
        when(proveedor.getActivo()).thenReturn(true);

        when(sesionCaja.estaAbierta()).thenReturn(true);
        when(sesionCaja.getEmpleado()).thenReturn(empleado);
        when(empleado.getUsuario()).thenReturn(usuario);
        when(usuario.getId()).thenReturn(2L);
        when(usuarioRolRepositorio.existsByUsuario_IdAndRol_CodigoAndRol_ActivoTrue(2L, "ASESOR")).thenReturn(true);
        when(usuario.getActivo()).thenReturn(true);
        when(empleado.getActivo()).thenReturn(true);
        when(sesionCaja.getCaja()).thenReturn(caja);
        when(caja.getActivo()).thenReturn(true);
        when(caja.getSucursal()).thenReturn(sucursal);
        when(sucursal.getActivo()).thenReturn(true);

        when(factura.getMonto()).thenReturn(new BigDecimal("38.00"));
        when(servicio.getComision()).thenReturn(new BigDecimal("2.00"));

        when(pagoRepositorio.existsByFactura_Id(1L)).thenReturn(false);

        when(pagoRepositorio.obtenerSiguienteNumeroComprobante()).thenReturn(1L);
        when(pagoRepositorio.save(any(Pago.class))).thenAnswer(invocacion -> {
            Pago pago = invocacion.getArgument(0);
            ReflectionTestUtils.setField(pago, "id", 10L);
            return pago;
        });
        CobroRespuesta respuesta = cobroServicio.cobrarFactura(2L, 1L, 1L, new BigDecimal("40.00"));

        assertEquals(new BigDecimal("40.00"), respuesta.totalCobrado());
        assertEquals(new BigDecimal("0.00"), respuesta.cambio());

        verify(pagoRepositorio).save(any(Pago.class));
        verify(factura).marcarPagada();
        verify(facturaRepositorio).save(factura);

        ArgumentCaptor<MovimientoCaja> movimientos = ArgumentCaptor.forClass(MovimientoCaja.class);
        verify(movimientoCajaRepositorio, times(1)).save(movimientos.capture());
        assertEquals(TipoMovimientoCaja.INGRESO_COBRO, movimientos.getValue().getTipo());
        assertEquals(new BigDecimal("40.00"), movimientos.getValue().getMonto());

        ArgumentCaptor<BitacoraOperacion> bitacoras = ArgumentCaptor.forClass(BitacoraOperacion.class);
        verify(bitacoraOperacionRepositorio, times(1)).save(bitacoras.capture());
        assertEquals(ResultadoOperacion.EXITOSO, bitacoras.getValue().getResultado());
        verify(bitacoraOperacionServicio, never()).registrarFallo(anyLong(), anyLong(), anyString());
    }

    // ----------------------------------------------------------------

    @Test
    void r1RechazaFacturaQueNoEstaPendiente() {
        when(factura.estaPendiente()).thenReturn(false);

        OperacionRechazadaException e = assertThrows(OperacionRechazadaException.class,
                () -> cobroServicio.cobrarFactura(2L, 1L, 1L, new BigDecimal("40.0")));

        assertEquals("La factura no está pendiente de pago.", e.getMessage());
        verify(facturaRepositorio, never()).save(any(Factura.class));
        verifyNoInteractions(pagoRepositorio, movimientoCajaRepositorio);
        verify(bitacoraOperacionServicio).registrarFallo(2L, 1L, e.getMessage());
    }

    // ----------------------------------------------------------------

    @Test
    void r2RechazaCuentaDeServicioInactiva() {
        when(factura.estaPendiente()).thenReturn(true);
        when(factura.getCuentaServicio()).thenReturn(cuentaServicio);
        when(cuentaServicio.estaActiva()).thenReturn(false);

        OperacionRechazadaException e = assertThrows(OperacionRechazadaException.class,
                () -> cobroServicio.cobrarFactura(2L, 1L, 1L, new BigDecimal("40.0")));

        assertEquals("La cuenta de servicio está inactiva.", e.getMessage());
        verify(facturaRepositorio, never()).save(any(Factura.class));
        verifyNoInteractions(pagoRepositorio, movimientoCajaRepositorio);
        verify(bitacoraOperacionServicio).registrarFallo(2L, 1L, e.getMessage());
    }

    // ----------------------------------------------------------------

    @Test
    void r3RechazaServicioInactivo() {
        when(factura.estaPendiente()).thenReturn(true);

        when(factura.getCuentaServicio()).thenReturn(cuentaServicio);
        when(cuentaServicio.estaActiva()).thenReturn(true);

        when(cuentaServicio.getServicio()).thenReturn(servicio);
        when(servicio.getActivo()).thenReturn(false);

        OperacionRechazadaException e = assertThrows(OperacionRechazadaException.class,
                () -> cobroServicio.cobrarFactura(2L, 1L, 1L, new BigDecimal("40.0")));

        assertEquals("El servicio no está habilitado para cobros.", e.getMessage());
        verify(facturaRepositorio, never()).save(any(Factura.class));
        verifyNoInteractions(pagoRepositorio, movimientoCajaRepositorio);
        verify(bitacoraOperacionServicio).registrarFallo(2L, 1L, e.getMessage());
    }

    @Test
    void r3RechazaProveedorInactivo() {
        when(factura.estaPendiente()).thenReturn(true);

        when(factura.getCuentaServicio()).thenReturn(cuentaServicio);
        when(cuentaServicio.estaActiva()).thenReturn(true);

        when(cuentaServicio.getServicio()).thenReturn(servicio);
        when(servicio.getActivo()).thenReturn(true);
        when(servicio.getProveedor()).thenReturn(proveedor);
        when(proveedor.getActivo()).thenReturn(false);

        OperacionRechazadaException e = assertThrows(OperacionRechazadaException.class,
                () -> cobroServicio.cobrarFactura(2L, 1L, 1L, new BigDecimal("40.0")));

        assertEquals("El servicio no está habilitado para cobros.", e.getMessage());
        verify(facturaRepositorio, never()).save(any(Factura.class));
        verifyNoInteractions(pagoRepositorio, movimientoCajaRepositorio);
        verify(bitacoraOperacionServicio).registrarFallo(2L, 1L, e.getMessage());
    }

    // ----------------------------------------------------------------

    @Test
    void r4RechazaSesionDeCajaCerrada() {
        when(factura.estaPendiente()).thenReturn(true);

        when(factura.getCuentaServicio()).thenReturn(cuentaServicio);
        when(cuentaServicio.estaActiva()).thenReturn(true);

        when(cuentaServicio.getServicio()).thenReturn(servicio);
        when(servicio.getActivo()).thenReturn(true);
        when(servicio.getProveedor()).thenReturn(proveedor);
        when(proveedor.getActivo()).thenReturn(true);

        when(sesionCaja.estaAbierta()).thenReturn(false);

        OperacionRechazadaException e = assertThrows(OperacionRechazadaException.class,
                () -> cobroServicio.cobrarFactura(2L, 1L, 1L, new BigDecimal("40.0")));

        assertEquals("No tiene una sesión de caja habilitada para cobrar.", e.getMessage());
        verify(facturaRepositorio, never()).save(any(Factura.class));
        verifyNoInteractions(pagoRepositorio, movimientoCajaRepositorio);
        verify(bitacoraOperacionServicio).registrarFallo(2L, 1L, e.getMessage());
    }

    @Test
    void r4RechazaSesionDeOtroUsuario() {
        Usuario titularDeLaSesion = mock(Usuario.class);

        when(factura.estaPendiente()).thenReturn(true);

        when(factura.getCuentaServicio()).thenReturn(cuentaServicio);
        when(cuentaServicio.estaActiva()).thenReturn(true);

        when(cuentaServicio.getServicio()).thenReturn(servicio);
        when(servicio.getActivo()).thenReturn(true);
        when(servicio.getProveedor()).thenReturn(proveedor);
        when(proveedor.getActivo()).thenReturn(true);

        when(sesionCaja.estaAbierta()).thenReturn(true);
        when(sesionCaja.getEmpleado()).thenReturn(empleado);
        when(empleado.getUsuario()).thenReturn(titularDeLaSesion);
        when(titularDeLaSesion.getId()).thenReturn(1L);
        when(usuario.getId()).thenReturn(2L);

        OperacionRechazadaException e = assertThrows(OperacionRechazadaException.class,
                () -> cobroServicio.cobrarFactura(2L, 1L, 1L, new BigDecimal("40.0")));

        assertEquals("No tiene una sesión de caja habilitada para cobrar.", e.getMessage());
        verify(facturaRepositorio, never()).save(any(Factura.class));
        verifyNoInteractions(pagoRepositorio, movimientoCajaRepositorio);
        verify(bitacoraOperacionServicio).registrarFallo(2L, 1L, e.getMessage());
        verifyNoInteractions(usuarioRolRepositorio);
    }

    @Test
    void r4RechazaUsuarioSinRolAsesorActivo() {
        when(factura.estaPendiente()).thenReturn(true);

        when(factura.getCuentaServicio()).thenReturn(cuentaServicio);
        when(cuentaServicio.estaActiva()).thenReturn(true);

        when(cuentaServicio.getServicio()).thenReturn(servicio);
        when(servicio.getActivo()).thenReturn(true);
        when(servicio.getProveedor()).thenReturn(proveedor);
        when(proveedor.getActivo()).thenReturn(true);

        when(sesionCaja.estaAbierta()).thenReturn(true);
        when(sesionCaja.getEmpleado()).thenReturn(empleado);
        when(empleado.getUsuario()).thenReturn(usuario);
        when(usuario.getId()).thenReturn(2L);
        when(usuarioRolRepositorio.existsByUsuario_IdAndRol_CodigoAndRol_ActivoTrue(2L, "ASESOR")).thenReturn(false);

        OperacionRechazadaException e = assertThrows(OperacionRechazadaException.class,
                () -> cobroServicio.cobrarFactura(2L, 1L, 1L, new BigDecimal("40.0")));

        assertEquals("No tiene una sesión de caja habilitada para cobrar.", e.getMessage());
        verify(facturaRepositorio, never()).save(any(Factura.class));
        verifyNoInteractions(pagoRepositorio, movimientoCajaRepositorio);
        verify(bitacoraOperacionServicio).registrarFallo(2L, 1L, e.getMessage());
    }

    @Test
    void r4RechazaUsuarioInactivo() {
        when(factura.estaPendiente()).thenReturn(true);

        when(factura.getCuentaServicio()).thenReturn(cuentaServicio);
        when(cuentaServicio.estaActiva()).thenReturn(true);

        when(cuentaServicio.getServicio()).thenReturn(servicio);
        when(servicio.getActivo()).thenReturn(true);
        when(servicio.getProveedor()).thenReturn(proveedor);
        when(proveedor.getActivo()).thenReturn(true);

        when(sesionCaja.estaAbierta()).thenReturn(true);
        when(sesionCaja.getEmpleado()).thenReturn(empleado);
        when(empleado.getUsuario()).thenReturn(usuario);
        when(usuario.getId()).thenReturn(2L);
        when(usuarioRolRepositorio.existsByUsuario_IdAndRol_CodigoAndRol_ActivoTrue(2L, "ASESOR")).thenReturn(true);
        when(usuario.getActivo()).thenReturn(false);

        OperacionRechazadaException e = assertThrows(OperacionRechazadaException.class,
                () -> cobroServicio.cobrarFactura(2L, 1L, 1L, new BigDecimal("40.0")));

        assertEquals("No tiene una sesión de caja habilitada para cobrar.", e.getMessage());
        verify(facturaRepositorio, never()).save(any(Factura.class));
        verifyNoInteractions(pagoRepositorio, movimientoCajaRepositorio);
        verify(bitacoraOperacionServicio).registrarFallo(2L, 1L, e.getMessage());
    }

    @Test
    void r4RechazaEmpleadoInactivo() {
        when(factura.estaPendiente()).thenReturn(true);

        when(factura.getCuentaServicio()).thenReturn(cuentaServicio);
        when(cuentaServicio.estaActiva()).thenReturn(true);

        when(cuentaServicio.getServicio()).thenReturn(servicio);
        when(servicio.getActivo()).thenReturn(true);
        when(servicio.getProveedor()).thenReturn(proveedor);
        when(proveedor.getActivo()).thenReturn(true);

        when(sesionCaja.estaAbierta()).thenReturn(true);
        when(sesionCaja.getEmpleado()).thenReturn(empleado);
        when(empleado.getUsuario()).thenReturn(usuario);
        when(usuario.getId()).thenReturn(2L);
        when(usuarioRolRepositorio.existsByUsuario_IdAndRol_CodigoAndRol_ActivoTrue(2L, "ASESOR")).thenReturn(true);
        when(usuario.getActivo()).thenReturn(true);
        when(empleado.getActivo()).thenReturn(false);

        OperacionRechazadaException e = assertThrows(OperacionRechazadaException.class,
                () -> cobroServicio.cobrarFactura(2L, 1L, 1L, new BigDecimal("40.0")));

        assertEquals("No tiene una sesión de caja habilitada para cobrar.", e.getMessage());
        verify(facturaRepositorio, never()).save(any(Factura.class));
        verifyNoInteractions(pagoRepositorio, movimientoCajaRepositorio);
        verify(bitacoraOperacionServicio).registrarFallo(2L, 1L, e.getMessage());
    }

    @Test
    void r4RechazaCajaInactiva() {
        when(factura.estaPendiente()).thenReturn(true);

        when(factura.getCuentaServicio()).thenReturn(cuentaServicio);
        when(cuentaServicio.estaActiva()).thenReturn(true);

        when(cuentaServicio.getServicio()).thenReturn(servicio);
        when(servicio.getActivo()).thenReturn(true);
        when(servicio.getProveedor()).thenReturn(proveedor);
        when(proveedor.getActivo()).thenReturn(true);

        when(sesionCaja.estaAbierta()).thenReturn(true);
        when(sesionCaja.getEmpleado()).thenReturn(empleado);
        when(empleado.getUsuario()).thenReturn(usuario);
        when(usuario.getId()).thenReturn(2L);
        when(usuarioRolRepositorio.existsByUsuario_IdAndRol_CodigoAndRol_ActivoTrue(2L, "ASESOR")).thenReturn(true);
        when(usuario.getActivo()).thenReturn(true);
        when(empleado.getActivo()).thenReturn(true);
        when(sesionCaja.getCaja()).thenReturn(caja);
        when(caja.getActivo()).thenReturn(false);

        OperacionRechazadaException e = assertThrows(OperacionRechazadaException.class,
                () -> cobroServicio.cobrarFactura(2L, 1L, 1L, new BigDecimal("40.0")));

        assertEquals("No tiene una sesión de caja habilitada para cobrar.", e.getMessage());
        verify(facturaRepositorio, never()).save(any(Factura.class));
        verifyNoInteractions(pagoRepositorio, movimientoCajaRepositorio);
        verify(bitacoraOperacionServicio).registrarFallo(2L, 1L, e.getMessage());
    }

    @Test
    void r4RechazaSucursalInactiva() {
        when(factura.estaPendiente()).thenReturn(true);

        when(factura.getCuentaServicio()).thenReturn(cuentaServicio);
        when(cuentaServicio.estaActiva()).thenReturn(true);

        when(cuentaServicio.getServicio()).thenReturn(servicio);
        when(servicio.getActivo()).thenReturn(true);
        when(servicio.getProveedor()).thenReturn(proveedor);
        when(proveedor.getActivo()).thenReturn(true);

        when(sesionCaja.estaAbierta()).thenReturn(true);
        when(sesionCaja.getEmpleado()).thenReturn(empleado);
        when(empleado.getUsuario()).thenReturn(usuario);
        when(usuario.getId()).thenReturn(2L);
        when(usuarioRolRepositorio.existsByUsuario_IdAndRol_CodigoAndRol_ActivoTrue(2L, "ASESOR")).thenReturn(true);
        when(usuario.getActivo()).thenReturn(true);
        when(empleado.getActivo()).thenReturn(true);
        when(sesionCaja.getCaja()).thenReturn(caja);
        when(caja.getActivo()).thenReturn(true);
        when(caja.getSucursal()).thenReturn(sucursal);
        when(sucursal.getActivo()).thenReturn(false);

        OperacionRechazadaException e = assertThrows(OperacionRechazadaException.class,
                () -> cobroServicio.cobrarFactura(2L, 1L, 1L, new BigDecimal("40.0")));

        assertEquals("No tiene una sesión de caja habilitada para cobrar.", e.getMessage());
        verify(facturaRepositorio, never()).save(any(Factura.class));
        verifyNoInteractions(pagoRepositorio, movimientoCajaRepositorio);
        verify(bitacoraOperacionServicio).registrarFallo(2L, 1L, e.getMessage());
    }

    // ----------------------------------------------------------------

    @Test
    void r5RechazaEfectivoInsuficiente() {
        when(factura.estaPendiente()).thenReturn(true);

        when(factura.getCuentaServicio()).thenReturn(cuentaServicio);
        when(cuentaServicio.estaActiva()).thenReturn(true);

        when(cuentaServicio.getServicio()).thenReturn(servicio);
        when(servicio.getActivo()).thenReturn(true);
        when(servicio.getProveedor()).thenReturn(proveedor);
        when(proveedor.getActivo()).thenReturn(true);

        when(sesionCaja.estaAbierta()).thenReturn(true);
        when(sesionCaja.getEmpleado()).thenReturn(empleado);
        when(empleado.getUsuario()).thenReturn(usuario);
        when(usuario.getId()).thenReturn(2L);
        when(usuarioRolRepositorio.existsByUsuario_IdAndRol_CodigoAndRol_ActivoTrue(2L, "ASESOR")).thenReturn(true);
        when(usuario.getActivo()).thenReturn(true);
        when(empleado.getActivo()).thenReturn(true);
        when(sesionCaja.getCaja()).thenReturn(caja);
        when(caja.getActivo()).thenReturn(true);
        when(caja.getSucursal()).thenReturn(sucursal);
        when(sucursal.getActivo()).thenReturn(true);

        when(factura.getMonto()).thenReturn(new BigDecimal("38.00"));
        when(servicio.getComision()).thenReturn(new BigDecimal("2.00"));
        when(factura.getNumeroFactura()).thenReturn("FAC-001");

        SaldoInsuficienteException e = assertThrows(SaldoInsuficienteException.class,
                () -> cobroServicio.cobrarFactura(2L, 1L, 1L, new BigDecimal("39.99")));

        assertEquals("Efectivo insuficiente para pagar la factura FAC-001: recibió $39.99 y necesita $40.00.", e.getMessage());
        verify(facturaRepositorio, never()).save(any(Factura.class));
        verifyNoInteractions(pagoRepositorio, movimientoCajaRepositorio);
        verify(bitacoraOperacionServicio).registrarFallo(2L, 1L, e.getMessage());
    }

    // ----------------------------------------------------------------

    @Test
    void r6RechazaFacturaConPagoRegistrado() {
        when(factura.estaPendiente()).thenReturn(true);

        when(factura.getCuentaServicio()).thenReturn(cuentaServicio);
        when(cuentaServicio.estaActiva()).thenReturn(true);

        when(cuentaServicio.getServicio()).thenReturn(servicio);
        when(servicio.getActivo()).thenReturn(true);
        when(servicio.getProveedor()).thenReturn(proveedor);
        when(proveedor.getActivo()).thenReturn(true);

        when(sesionCaja.estaAbierta()).thenReturn(true);
        when(sesionCaja.getEmpleado()).thenReturn(empleado);
        when(empleado.getUsuario()).thenReturn(usuario);
        when(usuario.getId()).thenReturn(2L);
        when(usuarioRolRepositorio.existsByUsuario_IdAndRol_CodigoAndRol_ActivoTrue(2L, "ASESOR")).thenReturn(true);
        when(usuario.getActivo()).thenReturn(true);
        when(empleado.getActivo()).thenReturn(true);
        when(sesionCaja.getCaja()).thenReturn(caja);
        when(caja.getActivo()).thenReturn(true);
        when(caja.getSucursal()).thenReturn(sucursal);
        when(sucursal.getActivo()).thenReturn(true);

        when(factura.getMonto()).thenReturn(new BigDecimal("38.00"));
        when(servicio.getComision()).thenReturn(new BigDecimal("2.00"));

        when(pagoRepositorio.existsByFactura_Id(1L)).thenReturn(true);

        OperacionRechazadaException e = assertThrows(OperacionRechazadaException.class,
                () -> cobroServicio.cobrarFactura(2L, 1L, 1L, new BigDecimal("40.0")));

        assertEquals("La factura ya tiene un pago registrado.", e.getMessage());
        verify(facturaRepositorio, never()).save(any(Factura.class));
        verify(pagoRepositorio).existsByFactura_Id(1L);
        verify(pagoRepositorio, never()).save(any(Pago.class));
        verifyNoInteractions(movimientoCajaRepositorio);
        verify(bitacoraOperacionServicio).registrarFallo(2L, 1L, e.getMessage());
    }
}
