package sv.sipas.api.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import sv.sipas.api.entidad.Servicio;

import java.util.List;

public interface ServicioRepositorio extends JpaRepository<Servicio, Long> {

    List<Servicio> findByProveedor_IdAndActivoTrue(Long proveedorId);

    boolean existsByProveedor_IdAndCodigo(Long proveedorId, String codigo);
}
