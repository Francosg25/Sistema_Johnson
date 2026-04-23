-- Destruimos cualquier rastro de la tabla vieja
DROP TABLE IF EXISTS adjuntos CASCADE;

-- Creamos la tabla alineada 100% con Adjunto.java
CREATE TABLE adjuntos (
    id BIGSERIAL PRIMARY KEY,
    nombre_archivo VARCHAR(255),
    tipo_contenido VARCHAR(255),
    datos BYTEA NOT NULL,
    subido_en TIMESTAMP(6) WITHOUT TIME ZONE,
    subido_por_id BIGINT,
    proyecto_id BIGINT,
    elemento_checklist_id BIGINT,

    -- Restricciones de llaves foráneas basadas en tu V1
    CONSTRAINT fkbtyepfqum8rnqr07wxvvcr1ms FOREIGN KEY (elemento_checklist_id) REFERENCES elemento_checklist(id),
    CONSTRAINT fkdauso575t9y0tnd8ktxngtulh FOREIGN KEY (subido_por_id) REFERENCES usuarios(id),
    CONSTRAINT fkjn2epflqiggrxg7yeofkqor41 FOREIGN KEY (proyecto_id) REFERENCES proyectos(id)
);