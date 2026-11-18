package sv.sipas.api.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import sv.sipas.api.entidad.Empleado;

import java.util.List;
import java.util.Optional;

public interface EmpleadoRepositorio extends JpaRepository<Empleado, Long> {

    Optional<Empleado> findByUsuario_Id(Long usuarioId);

    List<Empleado> findBySucursal_Id(Long sucursalId);

    boolean existsByCodigo(String codigo);

    boolean existsByUsuario_Id(Long usuarioId);
}
