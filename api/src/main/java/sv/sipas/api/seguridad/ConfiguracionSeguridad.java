package sv.sipas.api.seguridad;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.HandlerExceptionResolver;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;


@Configuration
@EnableMethodSecurity
public class ConfiguracionSeguridad {

    private final SecretKey clave;
    private final HandlerExceptionResolver errores;

    public ConfiguracionSeguridad(@Value("${jwt.clave}") String clave,
                                  @Qualifier("handlerExceptionResolver") HandlerExceptionResolver errores) {
        this.clave = new SecretKeySpec(clave.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        this.errores = errores;
    }

    @Bean
    public SecurityFilterChain filtros(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s ->
                        s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(reglas -> reglas
                        .requestMatchers(
                                "/api/auth/login",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/error")
                        .permitAll()

                        // Operaciones de ADMIN
                        .requestMatchers(
                                "/api/usuarios/**",
                                "/api/roles/**",
                                "/api/bitacora/**",
                                "/api/proveedores/**",
                                "/api/servicios/**",
                                "/api/sucursales/**",
                                "/api/empleados/**")
                        .hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/cajas", "/api/sesiones-caja/*/retiros").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/cajas/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/cajas/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/clientes").hasRole("ADMIN")

                        // Operaciones de ASESOR
                        .requestMatchers(HttpMethod.POST,
                                "/api/cobros",
                                "/api/cajas/*/sesiones",
                                "/api/sesiones-caja/*/cierre")
                        .hasRole("ASESOR")

                        .requestMatchers(HttpMethod.GET,
                                "/api/sesiones-caja/**",
                                "/api/movimientos-caja/*",
                                "/api/pagos/sesion/**",
                                "/api/cajas/**").hasAnyRole("ADMIN", "ASESOR")
                        .requestMatchers(HttpMethod.GET, "/api/clientes/**").hasAnyRole("ADMIN", "CLIENTE")

                        // Operaciones para los 3 roles
                        .requestMatchers(HttpMethod.GET,
                                "/api/pagos/**",
                                "/api/facturas/**",
                                "/api/cuentas-servicio/**").hasAnyRole("ADMIN", "ASESOR", "CLIENTE")
                        .anyRequest().authenticated())
                .oauth2ResourceServer(o -> o
                        .jwt(Customizer.withDefaults())
                        .authenticationEntryPoint((peticion, respuesta, e) ->
                                errores.resolveException(peticion, respuesta, null, e))
                        .accessDeniedHandler((peticion, respuesta, e) ->
                                errores.resolveException(peticion, respuesta, null, e)));
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtEncoder jwtEncoder() {
        return new NimbusJwtEncoder(new ImmutableSecret<>(clave));
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withSecretKey(clave).macAlgorithm(MacAlgorithm.HS256).build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter roles = new JwtGrantedAuthoritiesConverter();
        roles.setAuthoritiesClaimName("roles");
        roles.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter convertidor = new JwtAuthenticationConverter();
        convertidor.setJwtGrantedAuthoritiesConverter(roles);
        return convertidor;
    }
}
