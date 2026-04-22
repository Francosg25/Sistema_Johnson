ALTER TABLE proyectos 
    ADD COLUMN aplicacion VARCHAR(255),
    ADD COLUMN linea VARCHAR(255),
    ADD COLUMN producto VARCHAR(255),
    ADD COLUMN razon_revision VARCHAR(255),
    ADD COLUMN program_manager VARCHAR(255),
    ADD COLUMN observaciones TEXT,
    ADD COLUMN fecha_ppap DATE,
    ADD COLUMN fecha_termino_safe_launch DATE;