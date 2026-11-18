package sv.sipas.api.repositorio;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sv.sipas.api.entidad.Factura;
import sv.sipas.api.entidad.enums.EstadoFactura;

import java.util.List;
import java.util.Optional;

public interface FacturaRepositorio extends JpaRepository<Factura, Long> {

    List<Factura> findByCuentaServicio_Id(Long cuentaServicioId);

    List<Factura> findByCuentaServicio_IdAndEstado(Long cuentaServicioId, EstadoFactura estado);

    List<Factura> findByCuentaServicio_Cliente_Id(Long clienteId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select f from Factura f where f.id = :facturaId")
    Optional<Factura> buscarParaCobro(@Param("facturaId") Long facturaId);
}
