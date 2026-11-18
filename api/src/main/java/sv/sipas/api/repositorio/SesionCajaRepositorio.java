package sv.sipas.api.repositorio;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sv.sipas.api.entidad.SesionCaja;
import sv.sipas.api.entidad.enums.EstadoSesionCaja;

import java.util.List;
import java.util.Optional;

public interface SesionCajaRepositorio extends JpaRepository<SesionCaja, Long> {

    boolean existsByCaja_IdAndEstado(Long cajaId, EstadoSesionCaja estado);

    boolean existsByEmpleado_IdAndEstado(Long empleadoId, EstadoSesionCaja estado);

    boolean existsByCaja_Sucursal_IdAndEstado(Long sucursalId, EstadoSesionCaja estado);

    List<SesionCaja> findByEmpleado_IdAndEstado(Long empleadoId, EstadoSesionCaja estado);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from SesionCaja s where s.id = :id")
    Optional<SesionCaja> buscarParaOperacion(@Param("id") Long id);

    List<SesionCaja> findByEmpleado_Id(Long empleadoId);

    List<SesionCaja> findByEstado(EstadoSesionCaja estado);
}
