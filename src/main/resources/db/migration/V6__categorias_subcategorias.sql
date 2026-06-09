-- Tabla de categorías gestionadas por el admin
CREATE TABLE categorias (
    id          TEXT PRIMARY KEY,
    nombre      TEXT NOT NULL,
    activo      BOOLEAN NOT NULL DEFAULT true,
    orden       INT NOT NULL DEFAULT 0,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Tabla de subcategorías anidadas bajo una categoría
CREATE TABLE subcategorias (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    categoria_id TEXT NOT NULL REFERENCES categorias(id) ON DELETE CASCADE,
    nombre       TEXT NOT NULL,
    activo       BOOLEAN NOT NULL DEFAULT true,
    orden        INT NOT NULL DEFAULT 0,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_subcategorias_categoria ON subcategorias(categoria_id);

-- Columna de subcategoría en tienda_productos (nullable, sin FK estricta para no romper datos existentes)
ALTER TABLE tienda_productos
    ADD COLUMN subcategoria_id UUID REFERENCES subcategorias(id) ON DELETE SET NULL;

-- Seed: categorías iniciales (equivalentes al frontend categorias.js + enum backend)
INSERT INTO categorias (id, nombre, activo, orden) VALUES
('autopartes',   'Autopartes',        true,  1),
('herramientas', 'Herramientas',      true,  2),
('hogar',        'Hogar',             true,  3),
('deportes',     'Deportes',          true,  4),
('indumentaria', 'Indumentaria',      true,  5),
('libros',       'Libros',            true,  6),
('juguetes',     'Juguetes',          true,  7),
('mascotas',     'Mascotas',          true,  8),
('belleza',      'Belleza',           true,  9),
('muebles',      'Muebles',           true, 10),
('accesorios',   'Accesorios',        true, 11),
('electronica',  'Electrónica',       true, 12),
('cosmeticos',   'Cosméticos',        true, 13),
('alimentos',    'Alimentos',         true, 14),
('otro',         'Otro',              true, 15);
