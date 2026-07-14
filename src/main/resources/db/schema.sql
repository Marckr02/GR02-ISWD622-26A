-- Esquema de la Dark Kitchen (migracion de almacen en memoria a BD real).
-- Sin FOREIGN KEY estrictas: se prioriza simplicidad sobre integridad
-- referencial estricta, consistente con el nivel del resto del proyecto.

CREATE TABLE IF NOT EXISTS restaurantes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(255),
    color VARCHAR(7)
);
-- CREATE TABLE IF NOT EXISTS no altera una tabla que ya existia sin esta columna;
-- este ADD COLUMN mantiene al dia cualquier BD de archivo previa.
ALTER TABLE restaurantes ADD COLUMN IF NOT EXISTS color VARCHAR(7);

CREATE TABLE IF NOT EXISTS insumos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    unidad VARCHAR(20) NOT NULL,
    stock DOUBLE NOT NULL,
    costo_unitario DOUBLE NOT NULL,
    stock_minimo DOUBLE NOT NULL
);

CREATE TABLE IF NOT EXISTS platos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    restaurante_id INT NOT NULL
);

CREATE TABLE IF NOT EXISTS plato_ingredientes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    plato_id INT NOT NULL,
    insumo_id INT NOT NULL,
    cantidad DOUBLE NOT NULL,
    unidad_receta VARCHAR(20)
);

CREATE TABLE IF NOT EXISTS pedidos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    descripcion VARCHAR(255),
    marca VARCHAR(100),
    plato_id INT,
    estado VARCHAR(20) NOT NULL,
    creado_en TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS proveedores (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    telefono VARCHAR(30),
    correo VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS proveedor_insumo (
    insumo_id INT PRIMARY KEY,
    proveedor_id INT NOT NULL
);

CREATE TABLE IF NOT EXISTS alertas_stock (
    id INT AUTO_INCREMENT PRIMARY KEY,
    insumo_id INT NOT NULL,
    insumo_nombre VARCHAR(100) NOT NULL,
    stock_al_momento DOUBLE NOT NULL,
    timestamp TIMESTAMP NOT NULL
);
