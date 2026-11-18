package sv.sipas.api.servicio;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.sipas.api.dto.RolRespuesta;
import sv.sipas.api.repositorio.RolRepositorio;

import java.util.List;

@Service
public class RolServicio {

    private final RolRepositorio rolRepositorio;

    public RolServicio(RolRepositorio rolRepositorio) {
        this.rolRepositorio = rolRepositorio;
    }

    @Transactional(readOnly = true)
    public List<RolRespuesta> listarRoles() {
        return rolRepositorio.findAll().stream().map(RolRespuesta::de).toList();
    }
}
