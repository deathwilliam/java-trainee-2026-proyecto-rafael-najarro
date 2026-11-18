package sv.sipas.api.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import sv.sipas.api.entidad.Sucursal;

import java.util.List;

public interface SucursalRepositorio extends JpaRepository<Sucursal, Long> {

    List<Sucursal> findByActivoTrue();

    boolean existsByCodigo(String codigo);
}
