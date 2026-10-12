-- Datos de prueba
USE biblioteca_escolar;

-- Contraseñas: bibliotecarios = admin123, estudiantes = est123
INSERT INTO usuarios (nombre, rut, correo, contrasena, rol) VALUES
('Carmen Rojas',     '11111111-1', 'admin@biblioteca.cl',   SHA2('admin123', 256), 'BIBLIOTECARIO'),
('Pedro Soto',       '22222222-2', 'psoto@biblioteca.cl',   SHA2('admin123', 256), 'BIBLIOTECARIO'),
('Valentina Muñoz',  '20111222-3', 'vmunoz@colegio.cl',     SHA2('est123', 256),   'ESTUDIANTE'),
('Benjamín Pérez',   '20333444-5', 'bperez@colegio.cl',     SHA2('est123', 256),   'ESTUDIANTE'),
('Sofía González',   '20555666-7', 'sgonzalez@colegio.cl',  SHA2('est123', 256),   'ESTUDIANTE');

INSERT INTO estudiantes (nombre, rut, curso, correo) VALUES
('Valentina Muñoz',  '20111222-3', '3° Medio A', 'vmunoz@colegio.cl'),
('Benjamín Pérez',   '20333444-5', '2° Medio B', 'bperez@colegio.cl'),
('Sofía González',   '20555666-7', '1° Medio A', 'sgonzalez@colegio.cl'),
('Matías Fernández', '20777888-9', '4° Medio C', 'mfernandez@colegio.cl'),
('Isidora Díaz',     '20999000-K', '8° Básico',  'idiaz@colegio.cl');

INSERT INTO categorias (nombre) VALUES
('Novela'), ('Ciencia'), ('Historia'), ('Poesía'), ('Matemáticas');

INSERT INTO libros (titulo, autor, isbn, editorial, stock, id_categoria) VALUES
('Cien años de soledad',          'Gabriel García Márquez', '9780307474728', 'Sudamericana',  3, 1),
('La casa de los espíritus',      'Isabel Allende',         '9788401242267', 'Plaza & Janés', 2, 1),
('El principito',                 'Antoine de Saint-Exupéry','9788478887194','Salamandra',    4, 1),
('Breve historia del tiempo',     'Stephen Hawking',        '9788498921960', 'Crítica',       2, 2),
('Cosmos',                        'Carl Sagan',             '9788408053521', 'Planeta',       1, 2),
('Historia de Chile',             'Francisco Encina',       '9789561123456', 'Zig-Zag',       2, 3),
('Veinte poemas de amor',         'Pablo Neruda',           '9789562390000', 'Nascimento',    3, 4),
('Desolación',                    'Gabriela Mistral',       '9789561100001', 'Universitaria', 2, 4),
('Álgebra',                       'Aurelio Baldor',         '9786074380000', 'Patria',        5, 5),
('El hombre que calculaba',       'Malba Tahan',            '9788496051123', 'RBA',           1, 5);

-- Préstamos: activos, uno atrasado y algunos devueltos (para los reportes)
INSERT INTO prestamos (id_estudiante, id_libro, fecha_prestamo, fecha_devolucion, devuelto, fecha_entrega) VALUES
(1, 1, CURDATE() - INTERVAL 3 DAY,  CURDATE() + INTERVAL 4 DAY, 0, NULL),
(2, 4, CURDATE() - INTERVAL 12 DAY, CURDATE() - INTERVAL 5 DAY, 0, NULL),
(3, 3, CURDATE() - INTERVAL 20 DAY, CURDATE() - INTERVAL 13 DAY, 1, CURDATE() - INTERVAL 14 DAY),
(1, 3, CURDATE() - INTERVAL 30 DAY, CURDATE() - INTERVAL 23 DAY, 1, CURDATE() - INTERVAL 20 DAY),
(4, 1, CURDATE() - INTERVAL 40 DAY, CURDATE() - INTERVAL 33 DAY, 1, CURDATE() - INTERVAL 33 DAY),
(5, 7, CURDATE() - INTERVAL 2 DAY,  CURDATE() + INTERVAL 5 DAY, 0, NULL);

-- El stock refleja lo disponible: se descuentan los préstamos activos
UPDATE libros SET stock = stock - 1 WHERE id IN (1, 4, 7);
