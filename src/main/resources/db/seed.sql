-- Datos de ejemplo: gastronomia y restaurantes ecuatorianos, con variedad
-- deliberada de niveles de stock (normal, minimo, critico, agotado) para
-- poder probar todo el sistema (disponibilidad bloqueada, alertas,
-- metricas, reportes) incluyendo escenarios negativos. Solo se ejecuta una
-- vez, cuando la tabla restaurantes esta vacia (ver ConexionBD).

-- ============== RESTAURANTES (id 1-6) ==============
-- nombre, descripcion, color (hex de marca: tonos distintos y suaves, pensados
-- para no chocar con el fondo oscuro del resto de la interfaz).
INSERT INTO restaurantes (nombre, descripcion, color) VALUES
('El Hornado de la Rumiñahui', 'Especialistas en hornado y fritada de la sierra ecuatoriana.', '#C97B63'),
('Cevichería Manabita El Marinero', 'Ceviches y mariscos frescos al estilo manabita.', '#5B9AA0'),
('Fritada La Chola Cuencana', 'Fritada, mote y llapingachos al estilo cuencano.', '#B08968'),
('Encebollados El Puerto', 'Encebollado guayaquileño y comida costeña.', '#7A9E7E'),
('Parrilladas Los Andes', 'Carnes a la parrilla y platos fuertes de la sierra.', '#8C7AA9'),
('Dulces y Coladas La Morlaca', 'Coladas, humitas, quimbolitos y dulces tradicionales.', '#C9A66B');

-- ============== INSUMOS (id 1-28) ==============
-- nombre, unidad, stock, costo_unitario, stock_minimo
-- Marcados CRITICO (stock < minimo) o AGOTADO (stock = 0) a proposito.
INSERT INTO insumos (nombre, unidad, stock, costo_unitario, stock_minimo) VALUES
('Carne de cerdo', 'kg', 45.0, 4.20, 10.0),
('Carne de res', 'kg', 38.0, 5.10, 10.0),
('Pollo entero', 'kg', 30.0, 3.30, 8.0),
('Cuy', 'unidades', 2.0, 12.00, 5.0),          -- CRITICO
('Camarón', 'kg', 18.0, 7.50, 6.0),
('Corvina', 'kg', 12.0, 6.20, 5.0),
('Concha', 'kg', 0.0, 8.00, 4.0),              -- AGOTADO
('Chorizo', 'kg', 9.0, 4.80, 5.0),
('Papa chola', 'kg', 60.0, 0.55, 15.0),
('Yuca', 'kg', 25.0, 0.60, 8.0),
('Verde', 'kg', 40.0, 0.45, 10.0),
('Maduro', 'kg', 20.0, 0.50, 8.0),
('Choclo', 'kg', 15.0, 0.70, 6.0),
('Mote', 'kg', 22.0, 0.90, 6.0),
('Chochos', 'kg', 0.0, 1.20, 3.0),             -- AGOTADO
('Maní', 'kg', 8.0, 2.50, 4.0),
('Arroz', 'kg', 70.0, 0.65, 15.0),
('Lenteja', 'kg', 14.0, 1.10, 5.0),
('Cebolla paiteña', 'kg', 3.0, 0.80, 6.0),     -- CRITICO
('Tomate riñón', 'kg', 18.0, 0.75, 6.0),
('Cilantro', 'kg', 1.0, 0.40, 2.0),            -- CRITICO
('Ají', 'kg', 5.0, 1.00, 2.0),
('Achiote', 'l', 4.0, 3.20, 1.5),
('Limón', 'kg', 10.0, 0.90, 4.0),
('Queso fresco andino', 'kg', 16.0, 3.80, 5.0),
('Aguacate', 'kg', 12.0, 1.30, 4.0),
('Aceite vegetal', 'l', 25.0, 2.00, 6.0),
('Panela', 'kg', 1.5, 1.60, 3.0);              -- CRITICO

-- ============== PLATOS (id 1-28) ==============
INSERT INTO platos (nombre, restaurante_id) VALUES
('Hornado completo', 1),
('Fritada mixta', 1),
('Llapingachos con chorizo', 1),
('Caldo de patas', 1),
('Mote pillo', 1),
('Tamales de maíz', 1),
('Chugchucaras', 1),
('Ceviche de camarón', 2),
('Ceviche de concha', 2),
('Encebollado costero', 2),
('Arroz marinero', 2),
('Camarones apanados', 2),
('Fritada cuencana', 3),
('Cuy asado', 3),
('Mote sucio', 3),
('Tortillas de maíz', 3),
('Encebollado tradicional', 4),
('Guatita', 4),
('Seco de pollo', 4),
('Caldo de bola', 4),
('Bolón de verde', 4),
('Parrillada mixta', 5),
('Churrasco', 5),
('Chuletas a la plancha', 5),
('Costillas BBQ', 5),
('Humitas', 6),
('Colada morada', 6),
('Quimbolitos', 6);

-- ============== PLATO_INGREDIENTES ==============
INSERT INTO plato_ingredientes (plato_id, insumo_id, cantidad, unidad_receta) VALUES
(1, 1, 300, 'g'), (1, 9, 250, 'g'), (1, 14, 150, 'g'),
(2, 1, 350, 'g'), (2, 11, 200, 'g'), (2, 9, 150, 'g'),
(3, 9, 300, 'g'), (3, 25, 80, 'g'), (3, 8, 150, 'g'),
(4, 1, 250, 'g'), (4, 9, 150, 'g'),
(5, 14, 300, 'g'), (5, 27, 20, 'ml'),
(6, 13, 200, 'g'), (6, 25, 50, 'g'),
(7, 1, 400, 'g'), (7, 9, 200, 'g'), (7, 15, 100, 'g'),   -- usa Chochos (agotado)
(8, 5, 250, 'g'), (8, 24, 50, 'g'),
(9, 7, 200, 'g'),                                          -- usa Concha (agotado)
(10, 6, 300, 'g'), (10, 11, 100, 'g'),
(11, 17, 250, 'g'), (11, 5, 150, 'g'), (11, 7, 50, 'g'),   -- usa Concha (agotado)
(12, 5, 300, 'g'), (12, 27, 30, 'ml'),
(13, 1, 350, 'g'), (13, 14, 150, 'g'), (13, 11, 150, 'g'),
(14, 4, 1, 'unidades'), (14, 9, 200, 'g'),                 -- usa Cuy (critico)
(15, 14, 300, 'g'), (15, 1, 100, 'g'),
(16, 13, 150, 'g'), (16, 25, 60, 'g'),
(17, 6, 300, 'g'), (17, 19, 80, 'g'),                      -- usa Cebolla paiteña (critico)
(18, 2, 250, 'g'), (18, 9, 150, 'g'),
(19, 3, 300, 'g'), (19, 17, 150, 'g'),
(20, 11, 200, 'g'), (20, 2, 150, 'g'),
(21, 11, 200, 'g'), (21, 25, 50, 'g'),
(22, 2, 400, 'g'), (22, 1, 300, 'g'), (22, 3, 250, 'g'),
(23, 2, 350, 'g'), (23, 20, 100, 'g'),
(24, 1, 300, 'g'), (24, 9, 200, 'g'),
(25, 1, 400, 'g'), (25, 23, 20, 'ml'),
(26, 13, 250, 'g'), (26, 25, 60, 'g'),
(27, 16, 80, 'g'), (27, 28, 100, 'g'),                     -- usa Panela (critico)
(28, 13, 200, 'g'), (28, 28, 80, 'g');                     -- usa Panela (critico)

-- ============== PROVEEDORES (id 1-6) ==============
INSERT INTO proveedores (nombre, telefono, correo) VALUES
('Mercado Mayorista de Quito', '0991112233', 'ventas@mercadoquito.ec'),
('Distribuidora Sierra Norte', '0987654321', 'contacto@sierranorte.ec'),
('Camaronera del Pacífico', '0978889900', 'info@camaronerapacifico.ec'),
('Lácteos San Pedro', '0965554433', 'pedidos@lacteossanpedro.ec'),
('Agroindustrias Manabí', '0932221100', 'ventas@agromanabi.ec'),
('Avícola El Gallo Dorado', '0945556677', 'contacto@gallodorado.ec');

-- ============== PROVEEDOR_INSUMO (vinculos) ==============
INSERT INTO proveedor_insumo (insumo_id, proveedor_id) VALUES
(1, 2), (2, 2), (3, 6), (4, 5), (5, 3), (6, 3), (7, 3),
(9, 1), (14, 1), (19, 5), (21, 5), (25, 4), (28, 1);

-- ============== PEDIDOS ENTREGADO (metricas/reportes) ==============
-- Volumen variado y sesgado por plato para poder demostrar Top 5 + "Otros".
INSERT INTO pedidos (descripcion, marca, plato_id, estado, creado_en) VALUES
('Hornado completo', 'El Hornado de la Rumiñahui', 1, 'ENTREGADO', CURRENT_TIMESTAMP),
('Hornado completo', 'El Hornado de la Rumiñahui', 1, 'ENTREGADO', CURRENT_TIMESTAMP),
('Hornado completo', 'El Hornado de la Rumiñahui', 1, 'ENTREGADO', CURRENT_TIMESTAMP),
('Hornado completo', 'El Hornado de la Rumiñahui', 1, 'ENTREGADO', CURRENT_TIMESTAMP),
('Hornado completo', 'El Hornado de la Rumiñahui', 1, 'ENTREGADO', CURRENT_TIMESTAMP),
('Hornado completo', 'El Hornado de la Rumiñahui', 1, 'ENTREGADO', CURRENT_TIMESTAMP),
('Fritada mixta', 'El Hornado de la Rumiñahui', 2, 'ENTREGADO', CURRENT_TIMESTAMP),
('Fritada mixta', 'El Hornado de la Rumiñahui', 2, 'ENTREGADO', CURRENT_TIMESTAMP),
('Fritada mixta', 'El Hornado de la Rumiñahui', 2, 'ENTREGADO', CURRENT_TIMESTAMP),
('Fritada mixta', 'El Hornado de la Rumiñahui', 2, 'ENTREGADO', CURRENT_TIMESTAMP),
('Fritada mixta', 'El Hornado de la Rumiñahui', 2, 'ENTREGADO', CURRENT_TIMESTAMP),
('Llapingachos con chorizo', 'El Hornado de la Rumiñahui', 3, 'ENTREGADO', CURRENT_TIMESTAMP),
('Llapingachos con chorizo', 'El Hornado de la Rumiñahui', 3, 'ENTREGADO', CURRENT_TIMESTAMP),
('Llapingachos con chorizo', 'El Hornado de la Rumiñahui', 3, 'ENTREGADO', CURRENT_TIMESTAMP),
('Caldo de patas', 'El Hornado de la Rumiñahui', 4, 'ENTREGADO', CURRENT_TIMESTAMP),
('Caldo de patas', 'El Hornado de la Rumiñahui', 4, 'ENTREGADO', CURRENT_TIMESTAMP),
('Mote pillo', 'El Hornado de la Rumiñahui', 5, 'ENTREGADO', CURRENT_TIMESTAMP),
('Mote pillo', 'El Hornado de la Rumiñahui', 5, 'ENTREGADO', CURRENT_TIMESTAMP),
('Tamales de maíz', 'El Hornado de la Rumiñahui', 6, 'ENTREGADO', CURRENT_TIMESTAMP),
('Chugchucaras', 'El Hornado de la Rumiñahui', 7, 'ENTREGADO', CURRENT_TIMESTAMP),

('Ceviche de camarón', 'Cevichería Manabita El Marinero', 8, 'ENTREGADO', CURRENT_TIMESTAMP),
('Ceviche de camarón', 'Cevichería Manabita El Marinero', 8, 'ENTREGADO', CURRENT_TIMESTAMP),
('Ceviche de camarón', 'Cevichería Manabita El Marinero', 8, 'ENTREGADO', CURRENT_TIMESTAMP),
('Ceviche de camarón', 'Cevichería Manabita El Marinero', 8, 'ENTREGADO', CURRENT_TIMESTAMP),
('Ceviche de camarón', 'Cevichería Manabita El Marinero', 8, 'ENTREGADO', CURRENT_TIMESTAMP),
('Ceviche de concha', 'Cevichería Manabita El Marinero', 9, 'ENTREGADO', CURRENT_TIMESTAMP),
('Encebollado costero', 'Cevichería Manabita El Marinero', 10, 'ENTREGADO', CURRENT_TIMESTAMP),
('Encebollado costero', 'Cevichería Manabita El Marinero', 10, 'ENTREGADO', CURRENT_TIMESTAMP),
('Encebollado costero', 'Cevichería Manabita El Marinero', 10, 'ENTREGADO', CURRENT_TIMESTAMP),
('Encebollado costero', 'Cevichería Manabita El Marinero', 10, 'ENTREGADO', CURRENT_TIMESTAMP),
('Arroz marinero', 'Cevichería Manabita El Marinero', 11, 'ENTREGADO', CURRENT_TIMESTAMP),
('Arroz marinero', 'Cevichería Manabita El Marinero', 11, 'ENTREGADO', CURRENT_TIMESTAMP),
('Camarones apanados', 'Cevichería Manabita El Marinero', 12, 'ENTREGADO', CURRENT_TIMESTAMP),

('Fritada cuencana', 'Fritada La Chola Cuencana', 13, 'ENTREGADO', CURRENT_TIMESTAMP),
('Fritada cuencana', 'Fritada La Chola Cuencana', 13, 'ENTREGADO', CURRENT_TIMESTAMP),
('Fritada cuencana', 'Fritada La Chola Cuencana', 13, 'ENTREGADO', CURRENT_TIMESTAMP),
('Fritada cuencana', 'Fritada La Chola Cuencana', 13, 'ENTREGADO', CURRENT_TIMESTAMP),
('Fritada cuencana', 'Fritada La Chola Cuencana', 13, 'ENTREGADO', CURRENT_TIMESTAMP),
('Cuy asado', 'Fritada La Chola Cuencana', 14, 'ENTREGADO', CURRENT_TIMESTAMP),
('Mote sucio', 'Fritada La Chola Cuencana', 15, 'ENTREGADO', CURRENT_TIMESTAMP),
('Mote sucio', 'Fritada La Chola Cuencana', 15, 'ENTREGADO', CURRENT_TIMESTAMP),
('Tortillas de maíz', 'Fritada La Chola Cuencana', 16, 'ENTREGADO', CURRENT_TIMESTAMP),

('Encebollado tradicional', 'Encebollados El Puerto', 17, 'ENTREGADO', CURRENT_TIMESTAMP),
('Encebollado tradicional', 'Encebollados El Puerto', 17, 'ENTREGADO', CURRENT_TIMESTAMP),
('Guatita', 'Encebollados El Puerto', 18, 'ENTREGADO', CURRENT_TIMESTAMP),
('Guatita', 'Encebollados El Puerto', 18, 'ENTREGADO', CURRENT_TIMESTAMP),
('Guatita', 'Encebollados El Puerto', 18, 'ENTREGADO', CURRENT_TIMESTAMP),
('Guatita', 'Encebollados El Puerto', 18, 'ENTREGADO', CURRENT_TIMESTAMP),
('Seco de pollo', 'Encebollados El Puerto', 19, 'ENTREGADO', CURRENT_TIMESTAMP),
('Seco de pollo', 'Encebollados El Puerto', 19, 'ENTREGADO', CURRENT_TIMESTAMP),
('Seco de pollo', 'Encebollados El Puerto', 19, 'ENTREGADO', CURRENT_TIMESTAMP),
('Seco de pollo', 'Encebollados El Puerto', 19, 'ENTREGADO', CURRENT_TIMESTAMP),
('Seco de pollo', 'Encebollados El Puerto', 19, 'ENTREGADO', CURRENT_TIMESTAMP),
('Caldo de bola', 'Encebollados El Puerto', 20, 'ENTREGADO', CURRENT_TIMESTAMP),
('Caldo de bola', 'Encebollados El Puerto', 20, 'ENTREGADO', CURRENT_TIMESTAMP),
('Bolón de verde', 'Encebollados El Puerto', 21, 'ENTREGADO', CURRENT_TIMESTAMP),
('Bolón de verde', 'Encebollados El Puerto', 21, 'ENTREGADO', CURRENT_TIMESTAMP),

('Parrillada mixta', 'Parrilladas Los Andes', 22, 'ENTREGADO', CURRENT_TIMESTAMP),
('Parrillada mixta', 'Parrilladas Los Andes', 22, 'ENTREGADO', CURRENT_TIMESTAMP),
('Parrillada mixta', 'Parrilladas Los Andes', 22, 'ENTREGADO', CURRENT_TIMESTAMP),
('Parrillada mixta', 'Parrilladas Los Andes', 22, 'ENTREGADO', CURRENT_TIMESTAMP),
('Churrasco', 'Parrilladas Los Andes', 23, 'ENTREGADO', CURRENT_TIMESTAMP),
('Churrasco', 'Parrilladas Los Andes', 23, 'ENTREGADO', CURRENT_TIMESTAMP),
('Churrasco', 'Parrilladas Los Andes', 23, 'ENTREGADO', CURRENT_TIMESTAMP),
('Chuletas a la plancha', 'Parrilladas Los Andes', 24, 'ENTREGADO', CURRENT_TIMESTAMP),
('Chuletas a la plancha', 'Parrilladas Los Andes', 24, 'ENTREGADO', CURRENT_TIMESTAMP),
('Costillas BBQ', 'Parrilladas Los Andes', 25, 'ENTREGADO', CURRENT_TIMESTAMP),

('Humitas', 'Dulces y Coladas La Morlaca', 26, 'ENTREGADO', CURRENT_TIMESTAMP),
('Humitas', 'Dulces y Coladas La Morlaca', 26, 'ENTREGADO', CURRENT_TIMESTAMP),
('Colada morada', 'Dulces y Coladas La Morlaca', 27, 'ENTREGADO', CURRENT_TIMESTAMP),
('Quimbolitos', 'Dulces y Coladas La Morlaca', 28, 'ENTREGADO', CURRENT_TIMESTAMP);

-- ============== PEDIDOS EN OTROS ESTADOS (tablero Kanban) ==============
INSERT INTO pedidos (descripcion, marca, plato_id, estado, creado_en) VALUES
('Hornado completo', 'El Hornado de la Rumiñahui', 1, 'RECIBIDO', CURRENT_TIMESTAMP),
('Ceviche de camarón', 'Cevichería Manabita El Marinero', 8, 'RECIBIDO', CURRENT_TIMESTAMP),
('Fritada cuencana', 'Fritada La Chola Cuencana', 13, 'RECIBIDO', CURRENT_TIMESTAMP),
('Seco de pollo', 'Encebollados El Puerto', 19, 'EN_PREPARACION', CURRENT_TIMESTAMP),
('Parrillada mixta', 'Parrilladas Los Andes', 22, 'EN_PREPARACION', CURRENT_TIMESTAMP),
('Guatita', 'Encebollados El Puerto', 18, 'LISTO', CURRENT_TIMESTAMP),
('Churrasco', 'Parrilladas Los Andes', 23, 'LISTO', CURRENT_TIMESTAMP);

-- ============== ALERTAS_STOCK (historial visible desde el primer arranque) ==============
-- Una alerta por cada insumo ya critico/agotado en la semilla, con fechas
-- escalonadas para poder probar el filtro por rango de fechas de inmediato.
INSERT INTO alertas_stock (insumo_id, insumo_nombre, stock_al_momento, timestamp) VALUES
(4, 'Cuy', 2.0, DATEADD('DAY', -5, CURRENT_TIMESTAMP)),
(7, 'Concha', 0.0, DATEADD('DAY', -3, CURRENT_TIMESTAMP)),
(15, 'Chochos', 0.0, DATEADD('DAY', -2, CURRENT_TIMESTAMP)),
(19, 'Cebolla paiteña', 3.0, DATEADD('DAY', -1, CURRENT_TIMESTAMP)),
(21, 'Cilantro', 1.0, DATEADD('HOUR', -3, CURRENT_TIMESTAMP)),
(28, 'Panela', 1.5, CURRENT_TIMESTAMP);
