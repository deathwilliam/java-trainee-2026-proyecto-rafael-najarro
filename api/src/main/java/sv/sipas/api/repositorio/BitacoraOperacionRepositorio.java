package sv.sipas.api.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sv.sipas.api.entidad.BitacoraOperacion;
import sv.sipas.api.entidad.enums.ResultadoOperacion;
import sv.sipas.api.entidad.enums.TipoOperacion;

import java.util.List;

public interface BitacoraOperacionRepositorio extends JpaRepository<BitacoraOperacion, Long> {

    List<BitacoraOperacion> findByUsuario_IdOrderByFechaHoraDesc(Long usuarioId);

    List<BitacoraOperacion> findByTipoOperacionAndResultadoOrderByFechaHoraDesc(TipoOperacion tipoOperacion,
                                                                                ResultadoOperacion resultado);

    List<BitacoraOperacion> findByEntidadAfectadaAndRegistroAfectadoIdOrderByFechaHoraDesc(String entidadAfectada,
                                                                                           Long registroAfectadoId);

    List<BitacoraOperacion> findByUsuario_IdAndTipoOperacionAndResultadoOrderByFechaHoraDesc(
            Long usuarioId, TipoOperacion tipoOperacion, ResultadoOperacion resultado);

    @Query("select b from BitacoraOperacion b " +
            "where (:usuarioId is null or b.usuario.id = :usuarioId) " +
            "and (:tipoOperacion is null or b.tipoOperacion = :tipoOperacion) " +
            "and (:resultado is null or b.resultado = :resultado) " +
            "order by b.fechaHora desc")
    List<BitacoraOperacion> buscarConFiltros(
            @Param("usuarioId") Long usuarioId,
            @Param("tipoOperacion") TipoOperacion tipoOperacion,
            @Param("resultado") ResultadoOperacion resultado);
}
