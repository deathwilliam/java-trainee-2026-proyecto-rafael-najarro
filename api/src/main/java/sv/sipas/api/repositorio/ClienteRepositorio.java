package sv.sipas.api.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import sv.sipas.api.entidad.Cliente;

import java.util.Optional;

public interface ClienteRepositorio extends JpaRepository<Cliente, Long> {

    Optional<Cliente> findByDui(String dui);

    boolean existsByDui(String dui);

    Optional<Cliente> findByUsuario_Id(Long usuarioId);

    boolean existsByUsuario_Id(Long usuarioId);
}
