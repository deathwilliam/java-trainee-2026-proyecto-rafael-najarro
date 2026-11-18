package sv.sipas.api.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import sv.sipas.api.entidad.UsuarioRol;

import java.util.List;
import java.util.Optional;

public interface UsuarioRolRepositorio extends JpaRepository<UsuarioRol, Long> {

    List<UsuarioRol> findByUsuario_Id(Long usuarioId);

    boolean existsByUsuario_IdAndRol_Id(Long usuarioId, Long rolId);

    boolean existsByUsuario_IdAndRol_Codigo(Long usuarioId, String codigo);

    boolean existsByUsuario_IdAndRol_CodigoAndRol_ActivoTrue(Long usuarioId, String codigo);

    UsuarioRol findByIdAndRol_Id(Long usuarioId, Long rolId);

    boolean existsByUsuario_Id(Long usuarioId);

    Optional<UsuarioRol> findOneByUsuario_Id(Long usuarioId);
}
