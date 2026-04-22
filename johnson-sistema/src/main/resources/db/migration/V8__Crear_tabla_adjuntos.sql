CREATE TABLE adjuntos (
    id BIGSERIAL PRIMARY KEY,
    nombre_archivo VARCHAR(255) NOT NULL,
    tipo_contenido VARCHAR(100) NOT NULL,
    ruta_archivo VARCHAR(500) NOT NULL,
    tamano_bytes BIGINT,
    fecha_subida TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    proyecto_id BIGINT,
    CONSTRAINT fk_adjunto_proyecto FOREIGN KEY (proyecto_id) REFERENCES proyectos(id) ON DELETE CASCADE
);

CREATE INDEX idx_adjuntos_proyecto ON adjuntos(proyecto_id);