package sv.sipas.api.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import sv.sipas.api.entidad.CuentaServicio;

import java.util.List;
import java.util.Optional;

public interface CuentaServicioRepositorio extends JpaRepository<CuentaServicio, Long> {

    Optional<CuentaServicio> findByServicio_IdAndReferencia(Long servicioId, String referencia);

    boolean existsByServicio_IdAndReferencia(Long servicioId, String referencia);

    List<CuentaServicio> findByCliente_Id(Long clienteId);
}
