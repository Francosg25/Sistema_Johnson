-- Añadir columnas para rangos de fechas en proyectos
ALTER TABLE proyectos ADD COLUMN fecha_line_arrival_fin DATE;
ALTER TABLE proyectos ADD COLUMN fecha_pv_build_fin DATE;