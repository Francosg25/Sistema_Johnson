-- Añadir columnas para rangos de fechas en SOP y PPAP
ALTER TABLE proyectos ADD COLUMN sop_fin DATE;
ALTER TABLE proyectos ADD COLUMN fecha_ppap_fin DATE;