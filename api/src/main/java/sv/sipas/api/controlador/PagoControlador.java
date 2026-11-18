package sv.sipas.api.controlador;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import sv.sipas.api.dto.CobroRespuesta;
import sv.sipas.api.servicio.PagoServicio;
import sv.sipas.api.servicio.UsuarioServicio;

import java.util.List;

@Tag(name = "Pagos", description = "Consulta de pagos y comprobantes de cobro")
@RestController
@RequestMapping("/api/pagos")
public class PagoControlador {

    private final PagoServicio pagoServicio;
    private final UsuarioServicio usuarioServicio;

    public PagoControlador(PagoServicio pagoServicio,
                           UsuarioServicio usuarioServicio) {
        this.pagoServicio = pagoServicio;
        this.usuarioServicio = usuarioServicio;
    }

    @Operation(
            summary = "Buscar pago por número de comprobante",
            description = "200 con el pago; " +
                    "400 si el comprobante es inválido; " +
                    "403 si no tiene acceso; " +
                    "404 si no existe")
    @GetMapping("/comprobante/{numeroComprobante}")
    public CobroRespuesta buscarPorComprobante(@PathVariable String numeroComprobante,
                                               @AuthenticationPrincipal Jwt token) {
        return pagoServicio.buscarPorComprobante(numeroComprobante, usuarioId(token));
    }

    @Operation(
            summary = "Buscar pago por identificador de factura",
            description = "200 con el pago; " +
                    "400 si el identificador es inválido; " +
                    "403 si no tiene acceso; " +
                    "404 si no existe un pago para la factura")
    @GetMapping("/factura/{facturaId}")
    public CobroRespuesta buscarPorFactura(@PathVariable Long facturaId,
                                           @AuthenticationPrincipal Jwt token) {
        return pagoServicio.buscarPorFactura(facturaId, usuarioId(token));
    }

    @Operation(
            summary = "Listar pagos de una sesión de caja",
            description = "200 con la lista; " +
                    "400 si el identificador es inválido; " +
                    "403 si no tiene acceso; " +
                    "404 si la sesión no existe")
    @GetMapping("/sesion/{sesionCajaId}")
    public List<CobroRespuesta> listarPorSesion(@PathVariable Long sesionCajaId,
                                                @AuthenticationPrincipal Jwt token) {
        return pagoServicio.listarPorSesion(sesionCajaId, usuarioId(token));
    }

    @Operation(
            summary = "Listar pagos con filtros opcionales combinables por sesión, empleado y cliente",
            description = "200 con la lista filtrada; [] si no hay coincidencias. "
                    + "El ADMIN puede consultar todos los pagos, el ASESOR los de sus sesiones "
                    + "y el CLIENTE sus propios pagos. " +
                    "400 si los filtros son inválidos; " +
                    "403 si no tiene acceso a los datos solicitados; " +
                    "404 si el ADMIN indica una sesión, empleado o cliente inexistente, o si el ASESOR indica una sesión o cliente inexistente")
    @GetMapping
    public List<CobroRespuesta> listarPagos(@RequestParam(name = "sesionCajaId", required = false) Long sesionCajaId,
                                            @RequestParam(name = "empleadoId", required = false) Long empleadoId,
                                            @RequestParam(name = "clienteId", required = false) Long clienteId,
                                            @AuthenticationPrincipal Jwt token) {
        return pagoServicio.listarPagos(sesionCajaId, empleadoId, clienteId, usuarioId(token));
    }

    @Operation(
            summary = "Consultar un pago",
            description = "200 con el pago; " +
                    "400 si el identificador es inválido; " +
                    "403 si no tiene acceso; " +
                    "404 si no existe")
    @GetMapping("/{id}")
    public CobroRespuesta buscarPorId(@PathVariable Long id,
                                      @AuthenticationPrincipal Jwt token) {
        return pagoServicio.buscarPorId(id, usuarioId(token));
    }

    private Long usuarioId(Jwt token) {
        return usuarioServicio.obtenerIdUsuarioActivo(token.getSubject());
    }
}
