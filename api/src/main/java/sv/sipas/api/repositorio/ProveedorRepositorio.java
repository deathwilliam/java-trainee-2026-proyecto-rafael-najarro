package sv.sipas.api.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import sv.sipas.api.entidad.Proveedor;

import java.util.List;

public interface ProveedorRepositorio extends JpaRepository<Proveedor, Long> {

    List<Proveedor> findByActivoTrue();

    boolean existsByCodigo(String codigo);
}
