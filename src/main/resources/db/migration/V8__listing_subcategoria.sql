ALTER TABLE listings
    ADD COLUMN IF NOT EXISTS subcategoria_id UUID REFERENCES subcategorias(id) ON DELETE SET NULL;
