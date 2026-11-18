package sv.sipas.api.servicio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.sipas.api.dto.TokenRespuesta;
import sv.sipas.api.entidad.Usuario;
import sv.sipas.api.entidad.UsuarioRol;
import sv.sipas.api.excepcion.CredencialesInvalidasException;
import sv.sipas.api.repositorio.UsuarioRepositorio;
import sv.sipas.api.repositorio.UsuarioRolRepositorio;

import java.time.Instant;
import java.util.List;

@Service
public class AutenticacionServicio {

    private static final long DURACION_SEGUNDOS = 3600;
    private static final Logger log = LoggerFactory.getLogger(AutenticacionServicio.class);

    private final UsuarioRepositorio usuarioRepositorio;
    private final UsuarioRolRepositorio usuarioRolRepositorio;
    private final PasswordEncoder bcrypt;
    private final JwtEncoder jwt;

    public AutenticacionServicio(UsuarioRepositorio usuarioRepositorio,
                                 UsuarioRolRepositorio usuarioRolRepositorio,
                                 PasswordEncoder bcrypt,
                                 JwtEncoder jwt) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.usuarioRolRepositorio = usuarioRolRepositorio;
        this.bcrypt = bcrypt;
        this.jwt = jwt;
    }

    @Transactional(readOnly = true)
    public TokenRespuesta entrar(String nombreUsuario, String clave) {
        Usuario usuario = usuarioRepositorio.findByNombreUsuario(nombreUsuario)
                .filter(Usuario::getActivo)
                .orElseThrow(CredencialesInvalidasException::new);

        UsuarioRol usuarioRol = usuarioRolRepositorio
                .findOneByUsuario_Id(usuario.getId())
                .filter(asignacion -> asignacion.getRol().getActivo())
                .orElseThrow(CredencialesInvalidasException::new);

        if (!bcrypt.matches(clave, usuario.getClaveHash())) {
            log.warn("Autenticación rechazada: usuario={}", nombreUsuario);
            throw new CredencialesInvalidasException();
        }

        Instant ahora = Instant.now();
        JwtClaimsSet datos = JwtClaimsSet.builder()
                .issuer("sipas")
                .subject(usuario.getNombreUsuario())
                .issuedAt(ahora)
                .expiresAt(ahora.plusSeconds(DURACION_SEGUNDOS))
                .claim("roles", List.of(usuarioRol.getRol().getCodigo()))
                .build();
        JwsHeader encabezado = JwsHeader.with(MacAlgorithm.HS256).build();

        String token = jwt.encode(JwtEncoderParameters.from(encabezado, datos)).getTokenValue();
        log.info("Autenticación exitosa: usuarioId={}, rol={}",
                usuario.getId(), usuarioRol.getRol().getCodigo());
        return new TokenRespuesta(token, "Bearer", DURACION_SEGUNDOS);
    }
}
