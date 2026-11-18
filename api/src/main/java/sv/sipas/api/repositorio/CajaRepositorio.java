package sv.sipas.api.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import sv.sipas.api.entidad.Caja;

import java.util.List;

public interface CajaRepositorio extends JpaRepository<Caja, Long> {

    List<Caja> findBySucursal_IdAndActivoTrue(Long sucursalId);

    boolean existsBySucursal_IdAndCodigo(Long sucursalId, String codigo);
}
