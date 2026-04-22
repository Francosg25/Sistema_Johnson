-- Migration V5: Add Master Timeline Fields for Master Timeline Functional Requirement
ALTER TABLE proyectos ADD COLUMN bu VARCHAR(255);
ALTER TABLE proyectos ADD COLUMN planta VARCHAR(255);
ALTER TABLE proyectos ADD COLUMN fecha_line_arrival DATE;
ALTER TABLE proyectos ADD COLUMN fecha_pv_build DATE;
ALTER TABLE proyectos ADD COLUMN scope TEXT;
ALTER TABLE proyectos ADD COLUMN launch_engineer VARCHAR(255);
