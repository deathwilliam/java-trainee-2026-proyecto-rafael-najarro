package sv.sipas.api.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sv.sipas.api.entidad.MovimientoCaja;
import sv.sipas.api.entidad.enums.TipoMovimientoCaja;

import java.math.BigDecimal;
import java.util.List;

public interface MovimientoCajaRepositorio extends JpaRepository<MovimientoCaja, Long> {

    List<MovimientoCaja> findBySesionCaja_IdOrderByFechaHoraAsc(Long sesionCajaId);

    @Query("select coalesce(sum(m.monto), 0) from MovimientoCaja m where m.sesionCaja.id = :sesionCajaId " +
            "and m.tipo = :tipo")
    BigDecimal sumarPorSesionYTipo(@Param("sesionCajaId") Long sesionCajaId, @Param("tipo") TipoMovimientoCaja tipo);
}
