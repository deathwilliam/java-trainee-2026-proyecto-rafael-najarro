package sv.sipas.api.entidad;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "empleado")
public class Empleado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sucursal_id", nullable = false)
    private Sucursal sucursal;

    @Column(length = 20, nullable = false, unique = true)
    private String codigo;

    @Column(length = 80, nullable = false)
    private String nombres;

    @Column(length = 80, nullable = false)
    private String apellidos;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn = LocalDateTime.now();

    protected Empleado() {
    }

    public Empleado(Usuario usuario, Sucursal sucursal, String codigo, String nombres, String apellidos) {
        this.usuario = usuario;
        this.sucursal = sucursal;
        this.codigo = codigo;
        this.nombres = nombres;
        this.apellidos = apellidos;
    }

    public void activar() {
        this.activo = true;
    }

    public void desactivar() {
        this.activo = false;
    }

    public void cambiarNombres(String nombres) {
        this.nombres = nombres;
    }

    public void cambiarApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public Long getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public Sucursal getSucursal() {
        return sucursal;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombres() {
        return nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public Boolean getActivo() {
        return activo;
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }

    @Override
    public String toString() {
        return String.format(
                "Empleado{id=%s, usuarioId=%s, sucursalId=%s, codigo='%s', nombres='%s', apellidos='%s', activo=%s}",
                id,
                usuario != null ? usuario.getId() : null,
                sucursal != null ? sucursal.getId() : null,
                codigo, nombres, apellidos, activo);
    }
}
