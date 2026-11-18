package sv.sipas.api;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import sv.sipas.api.repositorio.PagoRepositorio;

import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SeguridadApiTest {

    private static final String COBRO = """
            {"facturaId": 2, "sesionCajaId": 2, "efectivoRecibido": 30.00}
            """;

    private static final String COBRO_CON_OTRO_USUARIO = """
        {"facturaId": 2, "sesionCajaId": 2, "efectivoRecibido": 30.00, "usuario": "admin"}
        """;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private PagoRepositorio pagoRepositorio;

    private String tokenDe(String usuario) throws Exception {
        String respuesta = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "usuario": "%s",
                                  "clave": "SipasDemo2026!"
                                }
                                """.formatted(usuario)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andReturn().getResponse().getContentAsString();

        return JsonPath.read(respuesta, "$.token");
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    @Test
    void sinTokenDa401ConProblemDetail() throws Exception {
        mvc.perform(get("/api/facturas/2"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.title").value("No autenticado"))
                .andExpect(jsonPath("$.detail").isNotEmpty());
    }

    @Test
    void tokenFalsoDa401ConProblemDetail() throws Exception {
        mvc.perform(get("/api/facturas/2")
                        .header("Authorization", bearer("esto.no.sirve")))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.title").value("No autenticado"))
                .andExpect(jsonPath("$.detail").isNotEmpty());
    }


    @Test
    void claveIncorrectaDa401ConProblemDetail() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "usuario": "ana.cliente",
                                  "clave": "clave-incorrecta"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.title").value("No autenticado"))
                .andExpect(jsonPath("$.detail").isNotEmpty());
    }

    @Test
    void clienteConsultaSuFacturaPeroNoPuedeCobrar() throws Exception {
        String token = tokenDe("ana.cliente");

        mvc.perform(get("/api/facturas/2")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2));

        mvc.perform(post("/api/cobros")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(COBRO))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.title").value("Sin permiso"))
                .andExpect(jsonPath("$.detail").isNotEmpty());
    }

    @Test
    void asesorCobraYBitacoraUsaUsuarioDelToken() throws Exception {
        String asesor = tokenDe("asesor.central");

        mvc.perform(post("/api/cobros")
                        .header("Authorization", bearer(asesor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(COBRO_CON_OTRO_USUARIO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalCobrado").value(23.0))
                .andExpect(jsonPath("$.cambio").value(7.0));

        mvc.perform(get("/api/facturas/2")
                        .header("Authorization", bearer(asesor)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estadoFactura").value("PAGADA"));

        Long pagoId = pagoRepositorio.findByFactura_Id(2L)
                .orElseThrow()
                .getId();

        String registro = """
                $[?(@.entidadAfectada == 'pago' && @.registroAfectadoId == %d)]
                """.formatted(pagoId).trim();

        String admin = tokenDe("admin");

        mvc.perform(get("/api/bitacora")
                        .header("Authorization", bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(registro + ".usuarioId")
                        .value(contains(2)))
                .andExpect(jsonPath(registro + ".tipoOperacion")
                        .value(contains("COBRO_FACTURA")))
                .andExpect(jsonPath(registro + ".resultado")
                        .value(contains("EXITOSO")));
    }

    @Test
    void asesorNoConsultaBitacoraPeroAdminSi() throws Exception {
        String asesor = tokenDe("asesor.central");

        mvc.perform(get("/api/bitacora")
                        .header("Authorization", bearer(asesor)))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Sin permiso"))
                .andExpect(jsonPath("$.detail").isNotEmpty());

        String admin = tokenDe("admin");

        mvc.perform(get("/api/bitacora")
                        .header("Authorization", bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void clienteNoPuedeConsultarFacturaAjena() throws Exception {
        String cliente = tokenDe("ana.cliente");

        // La factura 4 pertenece a Carlos.
        mvc.perform(get("/api/facturas/4")
                        .header("Authorization", bearer(cliente)))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Acceso denegado"))
                .andExpect(jsonPath("$.detail").isNotEmpty());
    }
}