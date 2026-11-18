package sv.sipas.api.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sv.sipas.api.entidad.Pago;

import java.util.List;
import java.util.Optional;

public interface PagoRepositorio extends JpaRepository<Pago, Long> {

    boolean existsByFactura_Id(Long facturaId);

    Optional<Pago> findByFactura_Id(Long facturaId);

    Optional<Pago> findByNumeroComprobante(String numeroComprobante);

    List<Pago> findBySesionCaja_Id(Long sesionCajaId);

    List<Pago> findBySesionCaja_Empleado_Id(Long empleadoId);

    List<Pago> findByFactura_CuentaServicio_Cliente_Id(Long clienteId);

    @Query(value = "SELECT nextval('comprobante_seq')", nativeQuery = true)
    Long obtenerSiguienteNumeroComprobante();

    List<Pago> findBySesionCaja_IdAndSesionCaja_Empleado_IdAndFactura_CuentaServicio_Cliente_Id(Long sesionCajaId,
                                                                                                Long empleadoId,
                                                                                                Long clienteId);

    @Query("select p from Pago p " +
            "where (:sesionCajaId is null or p.sesionCaja.id = :sesionCajaId) " +
            "and (:empleadoId is null or p.sesionCaja.empleado.id = :empleadoId) " +
            "and (:clienteId is null or p.factura.cuentaServicio.cliente.id = :clienteId) " +
            "order by p.fechaHora desc")
    List<Pago> buscarConFiltros(
            @Param("sesionCajaId") Long sesionCajaId,
            @Param("empleadoId") Long empleadoId,
            @Param("clienteId") Long clienteId);
}
