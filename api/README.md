# SIPAS: Sistema Integral de Pagos de Servicios

**Video:** [Digital Trainee 2026 - Rafael Najarro - SIPAS](https://youtu.be/Ua9M55F4KF8)

API REST para una colecturía que cobra facturas de agua, electricidad, internet y telefonía. Permite consultar cuentas y facturas, cobrar en efectivo, administrar sesiones de caja y registrar operaciones en una bitácora.

Los proveedores, clientes, cuentas y facturas son datos ficticios de demostración.

## Tecnologías

Java 21, Spring Boot 4.1.2, Spring Data JPA, Spring Security con JWT, PostgreSQL 17, Swagger, Docker Compose, JUnit, Mockito y MockMvc.

## Levantar desde cero

Requisitos: Git y Docker Desktop iniciado, con Docker Compose disponible.

Clonar el repositorio y entrar al proyecto:

```powershell
git clone https://github.com/deathwilliam/java-trainee-2026-rafael-najarro.git
cd java-trainee-2026-rafael-najarro/proyecto/api
```

Crear un archivo `.env` junto a `docker-compose.yml`. Puede utilizarse `.env.ejemplo` como referencia.

Definir en `.env` una clave propia de al menos 32 caracteres ASCII:

```dotenv
JWT_CLAVE=REEMPLAZAR_POR_UNA_CLAVE_PROPIA_DE_AL_MENOS_32_CARACTERES
```

Validar la configuración y levantar la API junto con PostgreSQL:

```powershell
docker compose up --build -d
docker compose ps
docker compose logs -f api
```

La API está lista cuando aparece `Started ApiApplication`. PostgreSQL debe aparecer como `healthy`.

- Swagger: http://localhost:8081/swagger-ui.html
- OpenAPI: http://localhost:8081/v3/api-docs
- API: http://localhost:8081/api

### Puertos y conexión

| Uso | Dirección |
|---|---|
| API | `localhost:8081` |
| PostgreSQL de Compose, desde DBeaver | `localhost:5435` |
| PostgreSQL, desde el contenedor de la API | `db:5432` |
| PostgreSQL para ejecución local y pruebas | `localhost:5433` |

Para conectar DBeaver a la base de Compose:

- Base: `sipas`
- Usuario: `banco`
- Contraseña de demostración: `banco123`

La configuración local predeterminada utiliza `localhost:5433/sipas`; Compose reemplaza esa dirección por `db:5432/sipas`.

### Detener o reiniciar los datos

```powershell
docker compose down
```

Para eliminar también el volumen y levantar nuevamente desde una base vacía:

```powershell
docker compose down -v
docker compose up --build -d
```

## Configuración local

| Variable | Uso | Valor predeterminado |
|---|---|---|
| `JWT_CLAVE` | Firma y validación del JWT | Obligatoria, sin valor predeterminado |
| `DB_URL` | Conexión a PostgreSQL | `jdbc:postgresql://localhost:5433/sipas` |
| `DB_USUARIO` | Usuario de PostgreSQL | `banco` |
| `DB_CLAVE` | Contraseña de PostgreSQL | `banco123` |

Para ejecutar desde IntelliJ se necesita Java 21 y una base `sipas` creada en PostgreSQL. Configurar `JWT_CLAVE` en las variables de entorno de la ejecución y arrancar `ApiApplication`.

El archivo `.env` lo utiliza Docker Compose; para ejecutar desde IntelliJ se configuran las variables en su configuración de ejecución.

## Las pruebas

Las pruebas necesitan PostgreSQL en el puerto `5433`, con una base local para la API (`sipas`) y otra exclusiva para pruebas (`sipas_test`). La primera vez, ejecute:

```powershell
docker run --name sipas-db -e POSTGRES_USER=banco -e POSTGRES_PASSWORD=banco123 -p 5433:5432 -d postgres:17-alpine
docker exec sipas-db pg_isready -U banco -d banco
docker exec sipas-db psql -U banco -d banco -c "CREATE DATABASE sipas;"
docker exec sipas-db psql -U banco -d banco -c "CREATE DATABASE sipas_test;"
```

Después, desde proyecto/api, ejecute:

```powershell
.\mvnw.cmd test
```

El perfil `test` usa `sipas_test`, proporciona su propia clave JWT y recrea las tablas antes de ejecutar las pruebas. No necesita definir `JWT_CLAVE`.

| Clase | Qué comprueba | Necesita la base |
| :--- | :--- | :---: |
| `CobroFacturaReglasTest` | Las reglas del cobro con Mockito: factura, cuenta, servicio, proveedor, sesión y efectivo | No |
| `FacturaServicioTest` | Cobro completo, pago, movimiento, cambio de estado y rollback | Sí |
| `SesionCajaServicioTest` | Apertura de caja e índices únicos de sesiones abiertas | Sí |
| `SeguridadApiTest` | Login, 401, 403, roles, propiedad e identidad obtenida desde el token, con MockMvc | Sí |
| `RestriccionesBaseDatosTest` | Restricciones `CHECK`, formato del DUI, importes, movimientos, cierres y fechas válidas o inválidas | Sí |
| `PrimerasPruebasTest` | Datos iniciales, entidades, consultas y restricciones generales | Sí |
| `ApiApplicationTests` | Que el contexto completo de la aplicación inicia correctamente | Sí |

## Usuarios y roles de prueba

La contraseña de todos los usuarios de demostración es **`SipasDemo2026!`**. En la tabla `usuario` se almacena el hash BCrypt.

| Usuario | Rol | Acceso |
|---|---|---|
| `admin` | ADMIN | Administración, consultas generales, retiros y bitácora |
| `asesor.central` | ASESOR | Caja de la sucursal Central y cobros |
| `asesor.santaana` | ASESOR | Caja de la sucursal Santa Ana y cobros |
| `ana.cliente` | CLIENTE | Sus cuentas, facturas y pagos |
| `carlos.cliente` | CLIENTE | Sus cuentas, facturas y pagos |
| `maria.cliente` | CLIENTE | Sus cuentas, facturas y pagos |

El ASESOR consulta los pagos y las sesiones que le corresponden. El CLIENTE consulta únicamente información asociada a su perfil.

Los catálogos de proveedores y servicios son exclusivos de ADMIN.

Cada usuario admite como máximo un rol. Para asignar ASESOR o CLIENTE debe existir previamente su perfil de empleado o cliente asociado.

## Autenticación en Swagger

Ejecutar `POST /api/auth/login`:

```json
{
  "usuario": "asesor.central",
  "clave": "SipasDemo2026!"
}
```

Copiar el campo `token`, abrir **Authorize** y pegarlo sin escribir `Bearer`.

Para cambiar de usuario, utilizar **Logout** y autorizar con el nuevo token.

El JWT dura una hora. La identidad de las operaciones se obtiene de su `subject`, no de un usuario enviado en el JSON.

## Operación principal: cobrar una factura

`POST /api/cobros`, exclusivo del ASESOR.

Después de iniciar desde los datos de `import.sql`, la factura 2 está pendiente y la sesión 2 pertenece a `asesor.central`.

```json
{
  "facturaId": 2,
  "sesionCajaId": 2,
  "efectivoRecibido": 30.00
}
```

Resultado esperado: **201**, monto de factura **22.00**, comisión **1.00**, total **23.00** y cambio **7.00**, junto al comprobante y los datos del servicio.

El cobro realiza los siguientes cambios dentro de una transacción:

1. Registro del pago y de la comisión aplicada.
2. Cambio de la factura a `PAGADA`.
3. Registro del movimiento `INGRESO_COBRO`.
4. Registro de bitácora `EXITOSO`.

Si falla un paso, se revierte el cobro completo. `CobroServicio` registra posteriormente el intento `FALLIDO` mediante otro servicio con `REQUIRES_NEW`.

Los comprobantes se generan utilizando una secuencia de PostgreSQL.

### Demostrar un rechazo

Repetir el cobro de la misma factura devuelve **400** porque ya no está pendiente. No se genera otro pago ni otro ingreso; el intento fallido queda en la bitácora.

La bitácora se consulta con el token de ADMIN:

```text
GET /api/bitacora
```

## Reglas de negocio

| Regla | Descripción | Aplicación |
|---|---|---|
| R1 | Solo se cobran facturas pendientes | Servicio de cobro |
| R2 | La cuenta de servicio debe estar activa | Servicio de cobro |
| R3 | Servicio y proveedor deben estar activos | Servicio de cobro |
| R4 | Sesión abierta, propia del asesor y con usuario, empleado, caja y sucursal habilitados | Servicio de cobro |
| R5 | El efectivo recibido debe cubrir factura más comisión | Servicio de cobro |
| R6 | Una factura admite como máximo un pago | Servicio y `UNIQUE` |
| R7 | Importes positivos; comisiones y fondo inicial no negativos | Validaciones Java y `CHECK` |
| R8 | Se conserva la comisión aplicada al cobrar y los datos económicos del pago | Servicio y operaciones disponibles en la API |
| R9 | Cada pago genera un ingreso por el total cobrado, en su misma sesión | Servicio transaccional y `UNIQUE` |
| R10 | DUI con formato válido y sin duplicados | Entidad, `UNIQUE` y `CHECK` |
| R11 | No se repite servicio y referencia en una cuenta | `UNIQUE` compuesto |
| R12 | Una sesión abierta por caja y por empleado; empleado de la sucursal de la caja | Servicio e índices únicos parciales |
| R13 | Se opera únicamente sobre sesiones abiertas; una cerrada no se reabre | Servicios |
| R14 | Solo ADMIN retira, con motivo y efectivo disponible suficiente | Seguridad y servicio |
| R15 | Ingreso con pago; retiro sin pago y con motivo no vacío | Entidad y `CHECK` |
| R16 | Cierre con efectivo no negativo y fecha igual o posterior a apertura | Servicio, entidad y `CHECK` |
| R17 | Vencimiento igual o posterior a emisión; se permite cobrar vencidas sin recargo | Entidad, `CHECK` y servicio |
| R18 | CLIENTE consulta únicamente su información | Servicios de autorización |

### Cuadre de caja

```text
total cobrado = monto de factura + comisión aplicada
cambio = efectivo recibido - total cobrado
saldo esperado = fondo inicial + ingresos por cobro - retiros
diferencia = efectivo contado - saldo esperado
```

El fondo inicial no se duplica como movimiento. El saldo esperado y la diferencia se calculan. Una diferencia de efectivo no impide cerrar la sesión.

## Endpoints

Todas las rutas usan el prefijo `/api`. Sin token responden **401**, excepto `POST /auth/login` y Swagger. Un usuario autenticado sin permiso recibe **403**.

### Autenticación y operación principal

| Método y ruta | Qué hace | Quién | Responde |
| :--- | :--- | :--- | :--- |
| `POST /auth/login` | Entrega un token JWT | Público | 200 · 401 |
| `GET /auth/yo` | Devuelve el usuario autenticado | Con token | 200 |
| `POST /cobros` | Cobra una factura en efectivo | ASESOR | 201 · 400 · 403 |

### Consultas

| Método y ruta | Qué hace | Quién | Responde |
| :--- | :--- | :--- | :--- |
| `GET /clientes` | Lista clientes | ADMIN | 200 |
| `GET /clientes/{id}` | Consulta un cliente | ADMIN o CLIENTE propietario | 200 · 400 · 403 · 404 |
| `GET /clientes/{clienteId}/cuentas-servicio` | Lista las cuentas de un cliente | ADMIN o CLIENTE propietario | 200 · 400 · 403 · 404 |
| `GET /cuentas-servicio` | Busca una cuenta por servicio y referencia | ADMIN, ASESOR o CLIENTE con acceso | 200 · 400 · 403 · 404 |
| `GET /cuentas-servicio/{id}` | Consulta una cuenta de servicio | ADMIN, ASESOR o CLIENTE con acceso | 200 · 400 · 403 · 404 |
| `GET /facturas` | Lista las facturas de una cuenta | ADMIN, ASESOR o CLIENTE con acceso | 200 · 400 · 403 · 404 |
| `GET /facturas/{id}` | Consulta una factura | ADMIN, ASESOR o CLIENTE con acceso | 200 · 400 · 403 · 404 |
| `GET /pagos` | Lista pagos aplicando filtros | ADMIN, ASESOR o CLIENTE con acceso | 200 · 400 · 403 · 404 |
| `GET /pagos/{id}` | Consulta un pago | ADMIN, ASESOR o CLIENTE con acceso | 200 · 400 · 403 · 404 |
| `GET /pagos/comprobante/{numeroComprobante}` | Busca un pago por comprobante | ADMIN, ASESOR o CLIENTE con acceso | 200 · 400 · 403 · 404 |
| `GET /pagos/factura/{facturaId}` | Busca el pago de una factura | ADMIN, ASESOR o CLIENTE con acceso | 200 · 400 · 403 · 404 |
| `GET /pagos/sesion/{sesionCajaId}` | Lista los pagos de una sesión | ADMIN o ASESOR propietario | 200 · 400 · 403 · 404 |
| `GET /bitacora` | Lista operaciones registradas | ADMIN | 200 |
| `GET /bitacora/{id}` | Consulta una operación de bitácora | ADMIN | 200 · 400 · 404 |
| `GET /roles` | Lista los roles disponibles | ADMIN | 200 |

Filtros de consultas:

- **Cuentas de servicio:** `servicioId` y `referencia` son obligatorios.
- **Facturas:** `cuentaServicioId` es obligatorio; `estado` es opcional.
- **Pagos:** `sesionCajaId`, `empleadoId` y `clienteId` son opcionales y se pueden combinar.
- **Bitácora:** `usuarioId`, `tipoOperacion` y `resultado` son opcionales y se pueden combinar.

### Cajas y sesiones

| Método y ruta | Qué hace | Quién | Responde |
| :--- | :--- | :--- | :--- |
| `GET /cajas` | Lista cajas | ADMIN, ASESOR | 200 |
| `GET /cajas/{id}` | Consulta una caja | ADMIN, ASESOR | 200 · 400 · 404 |
| `POST /cajas` | Crea una caja | ADMIN | 201 · 400 · 403 · 404 · 409 |
| `PATCH /cajas/{id}` | Modifica una caja | ADMIN | 200 · 400 · 403 · 404 · 409 |
| `DELETE /cajas/{id}` | Desactiva una caja | ADMIN | 204 · 400 · 403 · 404 · 409 |
| `POST /cajas/{cajaId}/sesiones` | Abre una sesión de caja | ASESOR | 201 · 400 · 403 |
| `GET /sesiones-caja` | Lista sesiones de caja | ADMIN o ASESOR con acceso | 200 · 400 · 403 · 404 |
| `GET /sesiones-caja/{id}` | Consulta una sesión | ADMIN o ASESOR propietario | 200 · 400 · 403 · 404 |
| `GET /sesiones-caja/{sesionCajaId}/resumen` | Consulta el cuadre de una sesión | ADMIN o ASESOR propietario | 200 · 400 · 403 · 404 |
| `GET /sesiones-caja/{sesionCajaId}/movimientos` | Lista movimientos de una sesión | ADMIN o ASESOR propietario | 200 · 400 · 403 · 404 |
| `POST /sesiones-caja/{sesionCajaId}/cierre` | Cierra una sesión y devuelve el cuadre | ASESOR propietario | 200 · 400 · 403 · 404 |
| `POST /sesiones-caja/{sesionCajaId}/retiros` | Registra un retiro de efectivo | ADMIN | 201 · 400 · 403 |
| `GET /movimientos-caja/{id}` | Consulta un movimiento | ADMIN o ASESOR propietario | 200 · 400 · 403 · 404 |

Filtros de sesiones:

- **Sesiones de caja:** `empleadoId` y `estado` son opcionales y se pueden combinar.
- La apertura recibe `fondoInicial`.
- El cierre recibe `efectivoContado` y devuelve el cuadre.
- El retiro recibe `monto` y `motivo`.

### Administración

| Método y ruta | Qué hace | Quién | Responde |
| :--- | :--- | :--- | :--- |
| `GET /usuarios` | Lista usuarios | ADMIN | 200 |
| `GET /usuarios/{id}` | Consulta un usuario | ADMIN | 200 · 400 · 404 |
| `POST /usuarios` | Crea un usuario | ADMIN | 201 · 400 · 409 |
| `PATCH /usuarios/{id}` | Modifica un usuario | ADMIN | 200 · 400 · 404 · 409 |
| `DELETE /usuarios/{id}` | Desactiva un usuario | ADMIN | 204 · 400 · 404 |
| `GET /proveedores` | Lista proveedores | ADMIN | 200 |
| `GET /proveedores/{id}` | Consulta un proveedor | ADMIN | 200 · 400 · 404 |
| `POST /proveedores` | Crea un proveedor | ADMIN | 201 · 400 · 409 |
| `PATCH /proveedores/{id}` | Modifica un proveedor | ADMIN | 200 · 400 · 404 · 409 |
| `DELETE /proveedores/{id}` | Desactiva un proveedor | ADMIN | 204 · 400 · 404 |
| `GET /servicios` | Lista servicios de un proveedor | ADMIN | 200 · 400 · 404 |
| `GET /servicios/{id}` | Consulta un servicio | ADMIN | 200 · 400 · 404 |
| `POST /servicios` | Crea un servicio | ADMIN | 201 · 400 · 404 · 409 |
| `PATCH /servicios/{id}` | Modifica un servicio | ADMIN | 200 · 400 · 404 |
| `DELETE /servicios/{id}` | Desactiva un servicio | ADMIN | 204 · 400 · 404 |
| `GET /sucursales` | Lista sucursales | ADMIN | 200 |
| `GET /sucursales/{id}` | Consulta una sucursal | ADMIN | 200 · 400 · 404 |
| `POST /sucursales` | Crea una sucursal | ADMIN | 201 · 400 · 409 |
| `PATCH /sucursales/{id}` | Modifica una sucursal | ADMIN | 200 · 400 · 404 · 409 |
| `DELETE /sucursales/{id}` | Desactiva una sucursal | ADMIN | 204 · 400 · 404 |
| `GET /empleados` | Lista empleados | ADMIN | 200 |
| `GET /empleados/{id}` | Consulta un empleado | ADMIN | 200 · 400 · 404 |
| `POST /empleados` | Crea un empleado | ADMIN | 201 · 400 · 404 · 409 |
| `PATCH /empleados/{id}` | Modifica un empleado | ADMIN | 200 · 400 · 404 · 409 |
| `DELETE /empleados/{id}` | Desactiva un empleado | ADMIN | 204 · 400 · 404 · 409 |

Filtro de administración:

- **Servicios:** `proveedorId` es obligatorio para listar servicios.

### Roles de usuario

| Método y ruta | Qué hace | Quién | Responde |
| :--- | :--- | :--- | :--- |
| `GET /usuarios/{id}/roles` | Consulta el rol de un usuario | ADMIN | 200 · 400 · 404 |
| `POST /usuarios/{id}/roles` | Asigna un rol a un usuario | ADMIN | 201 · 400 · 404 · 409 |
| `DELETE /usuarios/{id}/roles/{rolId}` | Retira el rol de un usuario | ADMIN | 204 · 400 · 404 |

Swagger documenta los cuerpos de entrada, parámetros y respuestas de cada endpoint.

## Errores y seguridad

La API utiliza `ProblemDetail`, con `title`, `status` y `detail`.

| Código | Significado |
|---|---|
| 400 | Datos inválidos o regla de negocio rechazada |
| 401 | Credenciales incorrectas, token ausente, inválido o vencido |
| 403 | Rol insuficiente o acceso a información ajena |
| 404 | Recurso no encontrado |
| 409 | Duplicados, relaciones o restricciones de integridad |

Los permisos se aplican mediante la cadena de filtros y `@PreAuthorize`. Las restricciones de propiedad se comprueban en los servicios.

## Cómo está organizado

```text
api/
├── src/
│   ├── main/
│   │   ├── java/sv/sipas/api/
│   │   │   ├── ApiApplication.java
│   │   │   ├── controlador/   recibe las solicitudes HTTP y responde con DTOs
│   │   │   ├── dto/           datos que entran y salen de la API
│   │   │   ├── entidad/       entidades que representan las tablas
│   │   │   │   └── enums/     estados y tipos usados por las entidades
│   │   │   ├── excepcion/     excepciones de negocio
│   │   │   ├── peticiones/    ejemplos JSON de solicitudes
│   │   │   ├── repositorio/   acceso a PostgreSQL mediante JPA
│   │   │   ├── seguridad/     JWT, filtros y configuración de seguridad
│   │   │   └── servicio/      reglas de negocio y transacciones
│   │   └── resources/
│   │       ├── application.properties
│   │       └── import.sql     datos de demostración y restricciones SQL
│   └── test/
│       ├── java/              pruebas unitarias y de integración
│       └── resources/
│           └── application-test.properties
├── docker-compose.yml          API y PostgreSQL para demostración
├── Dockerfile                  imagen ejecutable de la API
├── pom.xml                     dependencias y configuración Maven
└── README.md                   guía de ejecución y uso
```

Un controlador no accede directamente a un repositorio: llama a un servicio. Las entidades no se exponen como respuesta HTTP; la API utiliza DTOs. La clave de firma JWT se configura mediante `JWT_CLAVE` y no se guarda en el repositorio.
