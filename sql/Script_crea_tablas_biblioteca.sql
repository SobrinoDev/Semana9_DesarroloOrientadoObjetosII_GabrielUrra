-- Sistema de Gestión de Biblioteca Escolar - creación de tablas
DROP DATABASE IF EXISTS biblioteca_escolar;
CREATE DATABASE biblioteca_escolar CHARACTER SET utf8mb4 COLLATE utf8mb4_spanish_ci;
USE biblioteca_escolar;

CREATE TABLE usuarios (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    nombre      VARCHAR(100) NOT NULL,
    rut         VARCHAR(12)  NOT NULL UNIQUE,
    correo      VARCHAR(100) NOT NULL UNIQUE,
    contrasena  VARCHAR(64)  NOT NULL,              -- hash SHA-256
    rol         VARCHAR(20)  NOT NULL               -- BIBLIOTECARIO | ESTUDIANTE
);

CREATE TABLE estudiantes (
    id      INT AUTO_INCREMENT PRIMARY KEY,
    nombre  VARCHAR(100) NOT NULL,
    rut     VARCHAR(12)  NOT NULL UNIQUE,
    curso   VARCHAR(20)  NOT NULL,
    correo  VARCHAR(100) NOT NULL
);

CREATE TABLE categorias (
    id      INT AUTO_INCREMENT PRIMARY KEY,
    nombre  VARCHAR(60) NOT NULL UNIQUE
);

CREATE TABLE libros (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    titulo        VARCHAR(150) NOT NULL,
    autor         VARCHAR(100) NOT NULL,
    isbn          VARCHAR(20)  NOT NULL UNIQUE,
    editorial     VARCHAR(100) NOT NULL,
    stock         INT NOT NULL DEFAULT 0 CHECK (stock >= 0),
    id_categoria  INT NOT NULL,
    FOREIGN KEY (id_categoria) REFERENCES categorias(id)
);

CREATE TABLE prestamos (
    id                INT AUTO_INCREMENT PRIMARY KEY,
    id_estudiante     INT  NOT NULL,
    id_libro          INT  NOT NULL,
    fecha_prestamo    DATE NOT NULL,
    fecha_devolucion  DATE NOT NULL,                -- fecha de vencimiento calculada
    devuelto          TINYINT(1) NOT NULL DEFAULT 0,
    fecha_entrega     DATE NULL,                    -- fecha real de devolución (constancia de atraso)
    FOREIGN KEY (id_estudiante) REFERENCES estudiantes(id),
    FOREIGN KEY (id_libro)      REFERENCES libros(id)
);
