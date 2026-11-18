# SIPAS: Sistema Integral de Pagos de Servicios

## 1. Nombre y descripción del proyecto

SIPAS es una API REST desarrollada en Spring Boot con PostgreSQL. Es una colecturía para pagos de servicios que permite consultar y cobrar facturas de agua, electricidad, internet y telefonía, aplicando una comisión por el procesamiento del pago.

El sistema permite al asesor buscar una cuenta de servicio mediante el identificador del servicio y la referencia, seleccionar una factura pendiente y realizar el cobro. El proveedor se obtiene del servicio asociado. También administra la apertura y cierre de caja dentro de la sucursal, genera un número de comprobante de pago y va guardando las operaciones dentro de una bitácora.

Los clientes pueden consultar sus cuentas de servicio y sus facturas, los datos para este sistema serán de prueba para simular la información de los proveedores.

## 2. Roles del sistema

| Rol     | Quién es                                                  | Qué puede hacer                                                                                                                                                                                                                 |
| ------- | --------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| ADMIN   | Administrador de la colecturía                            | Gestionar proveedores, servicios, sucursales, cajas, empleados y usuarios. Consultar todos los pagos, sesiones de caja y registros de bitácora. Realizar retiros de efectivo de sesiones de caja abiertas, indicando el motivo. |
| ASESOR  | Empleado que atiende a los clientes y procesa los cobros. | Abrir y cerrar su sesión de caja, consultar cuentas de servicio y facturas, cobrar facturas pendientes y consultar los pagos de sus sesiones de caja.                                                                           |
| CLIENTE | Cliente con acceso a la plataforma.                       | Consultar únicamente sus cuentas de servicio, facturas y pagos asociados.                                                                                                                                                       |

Cada usuario con rol CLIENTE estará vinculado a su registro de cliente.
Cada usuario con rol ASESOR estará vinculado a su registro de empleado.

Los catálogos de proveedores y servicios, incluidos sus listados, búsquedas por identificador y mantenimientos, son exclusivos del ADMIN. El ASESOR y el CLIENTE pueden ver los datos del proveedor y servicio incluidos en las cuentas, facturas y pagos que estén autorizados a consultar.

Para asignar CLIENTE o ASESOR, primero debe existir el registro de cliente o empleado asociado al usuario. El orden es: crear el usuario, crear su perfil asociado y asignar el rol. ADMIN no requiere un perfil de cliente ni de empleado.

## 3. Entidades

- **cliente:** Persona titular de las cuentas de servicio. Puede tener un usuario vinculado para consultar sus facturas y pagos.

- **proveedor:** Empresa que presta los servicios cuyas facturas se cobran en la colecturía.

- **servicio:** Servicio ofrecido por un proveedor, con una comisión configurada para procesar sus pagos.

- **cuenta_servicio:** Cuenta de abonado que vincula a un cliente con un servicio mediante una referencia del proveedor.

- **factura:** Obligación de pago de una cuenta de servicio, con importe, fecha de vencimiento y estado.

- **pago:** Registro del cobro completo de una factura, con los importes aplicados, el efectivo recibido y un número único de comprobante.

- **sucursal:** Establecimiento de la colecturía donde operan empleados y cajas.

- **caja:** Punto de cobro perteneciente a una sucursal.

- **sesion_caja:** Jornada de operación de una caja a cargo de un empleado, con fondo inicial, apertura y cierre.

- **movimiento_caja:** Entrada o salida de efectivo que afecta una sesión de caja.

- **empleado:** Trabajador de una sucursal, vinculado a un usuario para operar el sistema.

- **bitacora_operacion:** Registro de los intentos de operación, indicando usuario, tipo de operación, fecha, registro afectado, descripción y resultado EXITOSO o FALLIDO.

- **usuario:** Cuenta de acceso al sistema, con credenciales y estado de activación.

- **rol:** Perfil de permisos del sistema: ADMIN, ASESOR o CLIENTE.

- **usuario_rol:** Asignación de un rol a un usuario. Cada usuario puede tener como máximo un rol asignado, mientras que un mismo rol puede asignarse a varios usuarios. Para iniciar sesión, el usuario debe estar activo y tener un rol activo asignado.

## 4. Operación principal

### Cobrar una factura: Permite al ASESOR registrar el pago completo de una factura en efectivo.

### Entrada

- Identificador de la factura seleccionada.
- Identificador de la sesión de caja.
- Efectivo recibido del cliente.

El usuario que ejecuta la operación se obtiene de la sesión autenticada. El importe de la factura y la comisión se consultan en la base de datos.

### Validaciones previas

El sistema comprueba las reglas R1 a R6: factura pendiente y sin pago previo, cuenta y servicio habilitados, sesión de caja válida y efectivo suficiente.

El total a cobrar se calcula como:

`total = factura.monto + servicio.comision`

### Cambios realizados

1. Insertar en `pago` la factura, la sesión de caja, el número único de comprobante, el efectivo recibido y la copia histórica de la comisión aplicada. El importe de la factura se obtiene de `factura.monto`.
2. Actualizar `factura.estado` de `PENDIENTE` a `PAGADA`.
3. Insertar en `movimiento_caja` un movimiento de tipo `INGRESO_COBRO`, asociado al pago y a su sesión, por el total cobrado.
4. Insertar en `bitacora_operacion` el registro de tipo `COBRO_FACTURA` y resultado `EXITOSO`, indicando el usuario y el pago generado.

Se verifica que el ingreso de caja sea igual a `factura.monto + pago.comision_aplicada` y que no exista otro cobro de la misma factura, incluso ante solicitudes simultáneas.

### Comportamiento ante fallos

Si falla cualquiera de los pasos, se revierte toda la transacción del cobro: no quedan registrados el pago, el movimiento de caja ni la entrada de bitácora EXITOSO. También se deshace cualquier cambio realizado al estado de la factura.

Después del rollback, el fallo se registra en una transacción independiente en `bitacora_operacion`, con resultado `FALLIDO`, usuario, descripción del error y referencia a la factura existente que se intentó cobrar. No se utiliza el identificador de un pago revertido.

Por ejemplo, si falla el registro del movimiento de caja después de actualizar la factura, se deshacen la actualización y el pago; posteriormente se registra el intento FALLIDO por separado.

### Salida

El sistema devuelve:

- Número de comprobante.
- Proveedor, servicio y referencia de la cuenta.
- Número de factura.
- Importe de la factura.
- Comisión aplicada.
- Total cobrado.
- Efectivo recibido.
- Cambio.
- Fecha y hora del pago.

El cambio se calcula como:

`cambio = pago.efectivo_recibido - factura.monto - pago.comision_aplicada`

## 5. Reglas de negocio

| #   | Regla                                                                                                                                                                                                                              | Dónde se cumple                                                                                                                          | Si no se cumple                                                            |
| --- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------- |
| R1  | Solo se cobra una `factura` cuyo `estado` sea `PENDIENTE`.                                                                                                                                                                         | Servicio, al cobrar.                                                                                                                     | Se rechaza: “La factura no está pendiente de pago”.                        |
| R2  | La `cuenta_servicio.estado` debe ser `ACTIVA`.                                                                                                                                                                                     | Servicio, al cobrar.                                                                                                                     | Se rechaza: “La cuenta de servicio está inactiva”.                         |
| R3  | El `servicio.activo` y el `proveedor.activo` deben ser verdaderos.                                                                                                                                                                 | Servicio, al cobrar.                                                                                                                     | Se rechaza: “El servicio no está habilitado para cobros”.                  |
| R4  | La `sesion_caja.estado` debe ser `ABIERTA` y pertenecer al empleado del usuario autenticado con rol ASESOR. El usuario, empleado, caja y sucursal deben estar activos.                                                             | Servicio, al cobrar.                                                                                                                     | Se rechaza: “No tiene una sesión de caja habilitada para cobrar”.          |
| R5  | `pago.efectivo_recibido` debe ser mayor o igual que `factura.monto + pago.comision_aplicada`.                                                                                                                                      | Servicio de cobro.                                                                                                                       | Se rechaza: “El efectivo recibido es insuficiente”.                        |
| R6  | Una factura puede tener como máximo un `pago`.                                                                                                                                                                                     | Servicio y UNIQUE en `pago.factura_id`.                                                                                                  | Se rechaza: “La factura ya tiene un pago registrado”.                      |
| R7  | `factura.monto`, `movimiento_caja.monto` y `pago.efectivo_recibido` deben ser mayores que cero. `servicio.comision`, `pago.comision_aplicada` y `sesion_caja.fondo_inicial` deben ser mayores o iguales que cero.                  | Tablas, mediante CHECK.                                                                                                                  | Se rechaza el registro con un importe no permitido.                        |
| R8  | Al cobrar, `pago.comision_aplicada` debe copiar la comisión vigente del servicio y conservarse sin cambios. El importe se obtiene de `factura.monto`, cuyos datos económicos no pueden modificarse después del pago.               | Servicio de cobro y restricciones de modificación.                                                                                       | Se rechaza la modificación o se revierte el cobro.                         |
| R9  | Cada pago debe generar exactamente un `movimiento_caja` de tipo `INGRESO_COBRO`, con la misma sesión y monto igual a `factura.monto + pago.comision_aplicada`. Este monto es DERIVADO y se mantiene mediante esta regla de cuadre. | Servicio transaccional y UNIQUE en `movimiento_caja.pago_id`.                                                                            | Se revierte todo el cobro.                                                 |
| R10 | `cliente.dui` debe tener el formato de ocho dígitos, guion y un dígito, y no repetirse.                                                                                                                                            | Servicio para formato y UNIQUE en la tabla.                                                                                              | Se rechaza: “El DUI es inválido o ya está registrado”.                     |
| R11 | La combinación `servicio_id` y `referencia` no puede repetirse en `cuenta_servicio`.                                                                                                                                               | Tabla, mediante UNIQUE compuesto.                                                                                                        | Se rechaza: “La cuenta de servicio ya está registrada”.                    |
| R12 | Una caja y un empleado solo pueden tener una `sesion_caja` en estado `ABIERTA` a la vez. El empleado debe estar activo y asignado a la sucursal de la caja.                                                                        | Servicio de apertura, consultando sesiones abiertas. Los índices únicos parciales se incorporarán posteriormente mediante un script SQL. | Se rechaza la apertura de la sesión.                                       |
| R13 | Solo se permiten cobros y movimientos en sesiones abiertas. Una sesión cerrada no puede reabrirse ni modificar su conteo.                                                                                                          | Servicios de cobro, retiro y cierre.                                                                                                     | Se rechaza: “La sesión de caja está cerrada”.                              |
| R14 | Solo un usuario con rol ADMIN puede registrar un `RETIRO`. Debe indicar un motivo no vacío y un monto que no supere el efectivo disponible de la sesión.                                                                           | Servicio de retiro.                                                                                                                      | Se rechaza: “Retiro no autorizado, sin motivo o con fondos insuficientes”. |
| R15 | Un `INGRESO_COBRO` debe tener `pago_id` y puede tener `motivo` NULL. Un `RETIRO` debe tener `pago_id` NULL y un `motivo` obligatorio y no vacío.                                                                                   | Tabla `movimiento_caja`, mediante CHECK.                                                                                                 | Se rechaza el movimiento.                                                  |
| R16 | Para cerrar una sesión se requieren `efectivo_contado` mayor o igual que cero y `fecha_cierre` no anterior a `fecha_apertura`. Se cambia el estado a `CERRADA`.                                                                    | Servicio y CHECK en `sesion_caja`.                                                                                                       | Se rechaza: “Los datos de cierre son inválidos”.                           |
| R17 | `factura.fecha_vencimiento` no puede ser anterior a `fecha_emision`. Una factura pendiente puede cobrarse después de vencer, sin recargos.                                                                                         | CHECK en la tabla y servicio de cobro.                                                                                                   | Se rechaza una factura con fechas inconsistentes.                          |
| R18 | Un usuario con rol CLIENTE solo consulta registros asociados al `cliente` vinculado mediante `cliente.usuario_id`.                                                                                                                 | Servicio y autorización de acceso.                                                                                                       | Se rechaza el acceso a información ajena.                                  |

### Cálculos de caja

El fondo inicial se registra únicamente en `sesion_caja`; no se registra nuevamente como movimiento.

`saldo esperado = fondo_inicial + suma(INGRESO_COBRO) - suma(RETIRO)`

`diferencia = efectivo_contado - saldo esperado`

El saldo esperado y la diferencia se calculan; no se almacenan como columnas. Una diferencia de caja no impide cerrar la sesión y queda disponible para revisión del ADMIN.

Los pagos, movimientos y entradas de bitácora confirmados no se editan ni eliminan. Las facturas pagadas no permiten modificar sus datos económicos.

## 6. Fuera del alcance

- Integraciones reales con proveedores, bancos o pasarelas de pago.
- Contratación de servicios y emisión comercial de facturas. Las cuentas y facturas se cargarán como datos de prueba.
- Pagos con tarjeta, transferencias, pagos parciales y monedas distintas del dólar estadounidense.
- Anulaciones, devoluciones y recargos por mora.
- Liquidaciones a proveedores y generación de comprobantes en PDF. El comprobante se devuelve como información desde la API.
