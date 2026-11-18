-- SIPAS: datos ficticios para una base recien creada por Hibernate (ddl-auto=create).
-- Cada sentencia ocupa una linea para el lector predeterminado de import.sql.
-- Usuarios de prueba: admin, asesor.central, asesor.santaana, ana.cliente, carlos.cliente, maria.cliente.
-- Contrasena de prueba para todos: SipasDemo2026! (almacenada como hash BCrypt).

-- 1. Catalogos y establecimientos: no dependen de personas.
INSERT INTO rol (id, codigo, descripcion, activo, creado_en) VALUES (1, 'ADMIN', 'Administrador de la colecturia', true, now()), (2, 'ASESOR', 'Asesor de cobros', true, now()), (3, 'CLIENTE', 'Cliente con acceso a consultas', true, now());
INSERT INTO proveedor (id, codigo, nombre, activo, creado_en) VALUES (1, 'AGUA-DEMO', 'Proveedor de Agua Demo', true, now()), (2, 'ENERGIA-DEMO', 'Proveedor de Energia Demo', true, now()), (3, 'INTERNET-DEMO', 'Proveedor de Internet Demo', true, now()), (4, 'TELEFONIA-DEMO', 'Proveedor de Telefonia Demo', true, now());
INSERT INTO sucursal (id, codigo, nombre, direccion, activo, creado_en) VALUES (1, 'CENTRAL', 'Sucursal Central', 'San Salvador, direccion de prueba', true, now()), (2, 'SANTA-ANA', 'Sucursal Santa Ana', 'Santa Ana, direccion de prueba', true, now());
INSERT INTO servicio (id, proveedor_id, codigo, nombre, comision, activo, creado_en) VALUES (1, 1, 'AGUA', 'Agua potable', 1.00, true, now()), (2, 2, 'ENERGIA', 'Energia electrica', 1.50, true, now()), (3, 3, 'INTERNET', 'Internet residencial', 0.75, true, now()), (4, 4, 'TELEFONIA', 'Telefonia movil', 0.50, true, now());

-- 2. Accesos y personas: los usuarios existen antes de vincular empleados y clientes.
INSERT INTO usuario (id, nombre_usuario, clave_hash, activo, creado_en) VALUES (1, 'admin', '$2a$10$cAWdsnDMsCblJbpcDXvJMOK4ounrfN153VIUBrmzpHOiKjGtocHaa', true, now()), (2, 'asesor.central', '$2a$10$cAWdsnDMsCblJbpcDXvJMOK4ounrfN153VIUBrmzpHOiKjGtocHaa', true, now()), (3, 'asesor.santaana', '$2a$10$cAWdsnDMsCblJbpcDXvJMOK4ounrfN153VIUBrmzpHOiKjGtocHaa', true, now()), (4, 'ana.cliente', '$2a$10$cAWdsnDMsCblJbpcDXvJMOK4ounrfN153VIUBrmzpHOiKjGtocHaa', true, now()), (5, 'carlos.cliente', '$2a$10$cAWdsnDMsCblJbpcDXvJMOK4ounrfN153VIUBrmzpHOiKjGtocHaa', true, now()), (6, 'maria.cliente', '$2a$10$cAWdsnDMsCblJbpcDXvJMOK4ounrfN153VIUBrmzpHOiKjGtocHaa', true, now());
INSERT INTO empleado (id, usuario_id, sucursal_id, codigo, nombres, apellidos, activo, creado_en) VALUES (1, 2, 1, 'EMP-001', 'Luis Alberto', 'Perez Gomez', true, now()), (2, 3, 2, 'EMP-002', 'Sofia Isabel', 'Lopez Rivera', true, now());
INSERT INTO cliente (id, usuario_id, dui, nombres, apellidos, telefono, correo, activo, creado_en) VALUES (1, 4, '00000001-8', 'Ana Lucia', 'Martinez Ruiz', '7000-0001', 'ana@example.com', true, now()), (2, 5, '00000002-6', 'Carlos Daniel', 'Hernandez Cruz', '7000-0002', 'carlos@example.com', true, now()), (3, 6, '00000003-4', 'Maria Elena', 'Garcia Flores', '7000-0003', 'maria@example.com', true, now()), (4, NULL, '00000004-2', 'Jose Antonio', 'Ramirez Diaz', NULL, NULL, true, now());

-- 3. Asignaciones, cajas y cuentas: referencian los catalogos y las personas anteriores.
INSERT INTO usuario_rol (id, usuario_id, rol_id, fecha_hora) VALUES (1, 1, 1, now()), (2, 2, 2, now()), (3, 3, 2, now()), (4, 4, 3, now()), (5, 5, 3, now()), (6, 6, 3, now());
INSERT INTO caja (id, sucursal_id, codigo, nombre, activo, creado_en) VALUES (1, 1, 'CAJA-01', 'Caja Central 01', true, now()), (2, 2, 'CAJA-01', 'Caja Santa Ana 01', true, now());
INSERT INTO cuenta_servicio (id, cliente_id, servicio_id, referencia, descripcion, estado, creado_en) VALUES (1, 1, 1, 'AGUA-0001', 'Agua de vivienda de Ana', 'ACTIVA', now()), (2, 1, 2, 'ENERGIA-0001', 'Energia de vivienda de Ana', 'ACTIVA', now()), (3, 2, 3, 'INTERNET-0001', 'Internet de vivienda de Carlos', 'ACTIVA', now()), (4, 3, 4, 'TELEFONO-0001', 'Linea movil de Maria', 'ACTIVA', now()), (5, 4, 1, 'AGUA-0002', 'Agua de cliente sin acceso en linea', 'ACTIVA', now()), (6, 2, 4, 'TELEFONO-0002', 'Linea inactiva de Carlos', 'INACTIVA', now());

-- 4. Facturas y sesiones: primero deben existir sus cuentas, cajas y empleados.
-- La factura 2 esta pendiente y vencida: se permite cobrarla sin recargo (R17).
INSERT INTO factura (id, cuenta_servicio_id, numero_factura, periodo, monto, fecha_emision, fecha_vencimiento, estado, creado_en) VALUES (1, 1, 'AGUA-202609-001', '2026-09-01', 18.50, '2026-09-01', '2026-09-15', 'PAGADA', '2026-09-01 09:00:00'), (2, 1, 'AGUA-202610-001', '2026-10-01', 22.00, '2026-10-01', '2026-10-15', 'PENDIENTE', '2026-10-01 09:00:00'), (3, 2, 'ENERGIA-202610-001', '2026-10-01', 45.00, '2026-10-01', '2026-10-25', 'PAGADA', '2026-10-01 09:00:00'), (4, 3, 'INTERNET-202610-001', '2026-10-01', 29.99, '2026-10-01', '2026-10-25', 'PENDIENTE',        '2026-10-01 09:00:00'), (5, 4, 'TELEFONO-202610-001', '2026-10-01', 12.00, '2026-10-01', '2026-10-25', 'PAGADA', '2026-10-01 09:00:00'), (6, 5, 'AGUA-202610-002', '2026-10-01', 15.25, '2026-10-01', '2026-10-25', 'PENDIENTE', '2026-10-01 09:00:00'), (7, 6, 'TELEFONO-202610-002', '2026-10-01', 8.00, '2026-10-01', '2026-10-25', 'PENDIENTE',        '2026-10-01 09:00:00');
-- Sesion 1 cerrada: saldo esperado = 100.00 + 19.50 + 46.50 - 20.00 = 146.00.
-- Sesiones 2 y 3 abiertas: una por caja y empleado, sin duplicados (R12).
INSERT INTO sesion_caja (id, caja_id, empleado_id, fondo_inicial, fecha_apertura, fecha_cierre, efectivo_contado, estado) VALUES (1, 1, 1, 100.00, '2026-10-22 08:00:00', '2026-10-22 17:00:00', 146.00, 'CERRADA'), (2, 1, 1, 100.00, '2026-10-23 08:00:00', NULL, NULL, 'ABIERTA'), (3, 2, 2, 75.00, '2026-10-23 08:00:00', NULL, NULL, 'ABIERTA');

-- 5. Cobros y movimientos: cada factura pagada tiene un pago y cada pago un ingreso.
-- Los pagos de la sesion 1 ocurrieron antes de su cierre; la comision es la vigente al cobrar.
INSERT INTO pago (id, factura_id, sesion_caja_id, numero_comprobante, comision_aplicada, efectivo_recibido, fecha_hora) VALUES (1, 1, 1, 'SIPAS-20261022-000001', 1.00, 20.00, '2026-10-22 09:00:00'), (2, 3, 1, 'SIPAS-20261022-000002', 1.50, 50.00, '2026-10-22 10:00:00'), (3, 5, 3, 'SIPAS-20261023-000003', 0.50, 20.00, '2026-10-23 09:00:00');
INSERT INTO movimiento_caja (id, sesion_caja_id, pago_id, usuario_id, tipo, monto, motivo, fecha_hora) VALUES (1, 1, 1, 2, 'INGRESO_COBRO', 19.50, NULL, '2026-10-22 09:00:00'), (2, 1, 2, 2, 'INGRESO_COBRO', 46.50, NULL, '2026-10-22 10:00:00'), (3, 1, NULL, 1, 'RETIRO', 20.00, 'Retiro parcial de efectivo por administrador', '2026-10-22 11:00:00'), (4, 3, 3, 3, 'INGRESO_COBRO', 12.50, NULL, '2026-10-23 09:00:00');

-- 6. Bitacora: los registros afectados ya existen; un fallo apunta a la factura, no a un pago revertido.
INSERT INTO bitacora_operacion (id, usuario_id, tipo_operacion, entidad_afectada, registro_afectado_id, descripcion, resultado, fecha_hora) VALUES (1, 2, 'APERTURA_CAJA', 'sesion_caja', 1, 'Apertura con fondo inicial de 100.00', 'EXITOSO', '2026-10-22 08:00:00'), (2, 2, 'COBRO_FACTURA', 'pago', 1, 'Cobro de agua por 19.50; efectivo 20.00; cambio 0.50', 'EXITOSO', '2026-10-22 09:00:00'), (3, 2, 'COBRO_FACTURA', 'pago', 2, 'Cobro de energia por 46.50; efectivo 50.00; cambio 3.50', 'EXITOSO', '2026-10-22 10:00:00'), (4, 1, 'RETIRO_EFECTIVO', 'movimiento_caja', 3, 'Retiro autorizado de 20.00', 'EXITOSO', '2026-10-22 11:00:00'), (5, 2, 'CIERRE_CAJA', 'sesion_caja', 1, 'Cierre con efectivo contado de 146.00 y diferencia de 0.00', 'EXITOSO', '2026-10-22 17:00:00'), (6, 2, 'APERTURA_CAJA', 'sesion_caja', 2, 'Apertura con fondo inicial de 100.00', 'EXITOSO', '2026-10-23 08:00:00'), (7, 3, 'APERTURA_CAJA', 'sesion_caja', 3, 'Apertura con fondo inicial de 75.00', 'EXITOSO', '2026-10-23 08:00:00'), (8, 3, 'COBRO_FACTURA', 'pago', 3, 'Cobro de telefonia por 12.50; efectivo 20.00; cambio 7.50', 'EXITOSO', '2026-10-23 09:00:00'), (9, 2, 'COBRO_FACTURA', 'factura', 3, 'Intento rechazado: la factura ya tiene un pago registrado', 'FALLIDO', '2026-10-23 09:10:00');

-- 7. Sincronizar las secuencias IDENTITY despues de insertar IDs explicitos.
-- La proxima insercion de cada entidad recibira un ID posterior a los datos de prueba.
SELECT setval(pg_get_serial_sequence('rol', 'id'), (SELECT MAX(id) FROM rol), true);
SELECT setval(pg_get_serial_sequence('proveedor', 'id'), (SELECT MAX(id) FROM proveedor), true);
SELECT setval(pg_get_serial_sequence('sucursal', 'id'), (SELECT MAX(id) FROM sucursal), true);
SELECT setval(pg_get_serial_sequence('servicio', 'id'), (SELECT MAX(id) FROM servicio), true);
SELECT setval(pg_get_serial_sequence('usuario', 'id'), (SELECT MAX(id) FROM usuario), true);
SELECT setval(pg_get_serial_sequence('empleado', 'id'), (SELECT MAX(id) FROM empleado), true);
SELECT setval(pg_get_serial_sequence('cliente', 'id'), (SELECT MAX(id) FROM cliente), true);
SELECT setval(pg_get_serial_sequence('usuario_rol', 'id'), (SELECT MAX(id) FROM usuario_rol), true);
SELECT setval(pg_get_serial_sequence('caja', 'id'), (SELECT MAX(id) FROM caja), true);
SELECT setval(pg_get_serial_sequence('cuenta_servicio', 'id'), (SELECT MAX(id) FROM cuenta_servicio), true);
SELECT setval(pg_get_serial_sequence('factura', 'id'), (SELECT MAX(id) FROM factura), true);
SELECT setval(pg_get_serial_sequence('sesion_caja', 'id'), (SELECT MAX(id) FROM sesion_caja), true);
SELECT setval(pg_get_serial_sequence('pago', 'id'), (SELECT MAX(id) FROM pago), true);
SELECT setval(pg_get_serial_sequence('movimiento_caja', 'id'), (SELECT MAX(id) FROM movimiento_caja), true);
SELECT setval(pg_get_serial_sequence('bitacora_operacion', 'id'), (SELECT MAX(id) FROM bitacora_operacion), true);

-- Secuencia para generar el numero de comprobante
CREATE SEQUENCE IF NOT EXISTS comprobante_seq START WITH 1 INCREMENT BY 1;

-- R12: una sola sesion abierta por caja y por empleado.
CREATE UNIQUE INDEX uk_sesion_caja_abierta_caja ON sesion_caja (caja_id) WHERE estado = 'ABIERTA';
CREATE UNIQUE INDEX uk_sesion_caja_abierta_empleado ON sesion_caja (empleado_id) WHERE estado = 'ABIERTA';

-- R7: importes positivos.
ALTER TABLE factura ADD CONSTRAINT ck_factura_monto_positivo CHECK (monto > 0);
ALTER TABLE movimiento_caja ADD CONSTRAINT ck_movimiento_monto_positivo CHECK (monto > 0);
ALTER TABLE pago ADD CONSTRAINT ck_pago_efectivo_positivo CHECK (efectivo_recibido > 0);

-- R7: valores que permiten cero.
ALTER TABLE servicio ADD CONSTRAINT ck_servicio_comision_no_negativa CHECK (comision >= 0);
ALTER TABLE pago ADD CONSTRAINT ck_pago_comision_no_negativa CHECK (comision_aplicada >= 0);
ALTER TABLE sesion_caja ADD CONSTRAINT ck_sesion_fondo_no_negativo CHECK (fondo_inicial >= 0);

-- R10: DUI con ocho dígitos, guion y un dígito final.
ALTER TABLE cliente ADD CONSTRAINT ck_cliente_dui_formato CHECK (dui ~ '^[0-9]{8}-[0-9]$');

-- R15: datos requeridos segun el tipo de movimiento.
ALTER TABLE movimiento_caja ADD CONSTRAINT ck_movimiento_tipo_datos CHECK ((tipo = 'INGRESO_COBRO' AND pago_id IS NOT NULL) OR (tipo = 'RETIRO' AND pago_id IS NULL AND motivo IS NOT NULL AND motivo ~ '[^[:space:]]'));

-- R16: una sesion abierta no tiene cierre; una cerrada requiere datos validos.
ALTER TABLE sesion_caja ADD CONSTRAINT ck_sesion_datos_cierre CHECK ((estado = 'ABIERTA' AND fecha_cierre IS NULL AND efectivo_contado IS NULL) OR (estado = 'CERRADA' AND fecha_cierre IS NOT NULL AND efectivo_contado IS NOT NULL AND efectivo_contado >= 0 AND fecha_cierre >= fecha_apertura));

-- R17: fechas consistentes en las facturas.
ALTER TABLE factura ADD CONSTRAINT ck_factura_fechas CHECK (fecha_vencimiento >= fecha_emision);
