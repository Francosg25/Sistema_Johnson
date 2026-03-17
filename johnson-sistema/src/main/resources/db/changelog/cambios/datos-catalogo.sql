-- liquibase formatted sql

-- changeset franco:2
-- =====================================================================
-- PROGRAMA APQP (STAGE 1, 2, 3, 4, 5)
-- =====================================================================
INSERT INTO catalogo_elementos (champion, codigo, etapa_visual, fase, grupo, nombre, requerido, tipo_input) VALUES 
('QE', '1.1', 'STAGE 1', '0. Program', 'Master Plan', 'CFT Planning and Definition', true, 'HITO'),
('DE', '1.2', 'STAGE 1', '0. Program', 'Master Plan', 'Project Scope Definition', true, 'HITO'),
('DE', '1.3', 'STAGE 1', '0. Program', 'Master Plan', 'Establish ''Team to Team'' meetings', true, 'HITO'),
('ALL', '1.4', 'STAGE 1', '0. Program', 'Master Plan', 'Training (Identify training needs)', true, 'HITO'),
('DE', '1.5', 'STAGE 1', '0. Program', 'Master Plan', 'Concerns/Issues Resolution', true, 'HITO'),
('DE', '1.6', 'STAGE 1', '0. Program', 'Master Plan', 'Timeline / Gantt', true, 'HITO'),

('PROJ', 'P-01', 'STAGE 2', '0. Program', 'Master Plan', 'Cross-Functional Team / CFT (JE Global and internal):', true, 'HITO'),
('DE', 'P-02', 'STAGE 2', '0. Program', 'Master Plan', 'DFMEA:', true, 'HITO'),
('DE', 'P-03', 'STAGE 2', '0. Program', 'Master Plan', 'Preliminary BOM:', true, 'HITO'),
('DE', 'P-04', 'STAGE 2', '0. Program', 'Master Plan', 'Drawings:', true, 'HITO'),
('QE/PE', 'P-05', 'STAGE 2', '0. Program', 'Master Plan', 'List of equipment, fixtures, tools, spare parts, gauges, and facilities needed', true, 'HITO'),
('PROJ', 'P-06', 'STAGE 2', '0. Program', 'Master Plan', 'Team Feasibility Commitment:', true, 'HITO'),
('ALL', 'P-07', 'STAGE 2', '0. Program', 'Master Plan', 'Lessons learned.', true, 'HITO'),
('PROJ', 'P-08', 'STAGE 2', '0. Program', 'Master Plan', 'Customer Supplier Manual', true, 'HITO'),
('DE', 'P-09', 'STAGE 2', '0. Program', 'Master Plan', 'Design Validation Report / DV Report', true, 'HITO'),
('QE', 'P-10', 'STAGE 2', '0. Program', 'Master Plan', 'Preliminary Customer Characteristics List:', true, 'HITO'),

('QE', 'P-11', 'STAGE 3', '0. Program', 'Master Plan', '01.- Packaging Specifications', true, 'HITO'),
('PE', 'P-12', 'STAGE 3', '0. Program', 'Master Plan', '01.- Packaging Specifications', true, 'HITO'),
('QE', 'P-13', 'STAGE 3', '0. Program', 'Master Plan', '02.- QMS Changes for product manufacturing', true, 'HITO'),
('PE', 'P-14', 'STAGE 3', '0. Program', 'Master Plan', '03.- Process Flow Chart', true, 'HITO'),
('PE', 'P-15', 'STAGE 3', '0. Program', 'Master Plan', '04.- Floor plan layout', true, 'HITO'),
('QE/PE', 'P-16', 'STAGE 3', '0. Program', 'Master Plan', '05.- Characteristic Matrix', true, 'HITO'),
('PE', 'P-17', 'STAGE 3', '0. Program', 'Master Plan', '0.6- PFMEA', true, 'HITO'),
('QE', 'P-18', 'STAGE 3', '0. Program', 'Master Plan', '0.7- Control Plan Pre-launch', true, 'HITO'),
('PE', 'P-19', 'STAGE 3', '0. Program', 'Master Plan', '0.8- WI', true, 'HITO'),
('QE', 'P-20', 'STAGE 3', '0. Program', 'Master Plan', '0.9- MSA Plan', true, 'HITO'),
('QE/PE', 'P-21', 'STAGE 3', '0. Program', 'Master Plan', '10.- SPC Plan', true, 'HITO'),
('PROJ', 'P-22', 'STAGE 3', '0. Program', 'Master Plan', '11.- Meeting Minutes', true, 'HITO'),
('QE', 'P-23', 'STAGE 3', '0. Program', 'Master Plan', '11.- Meeting Minutes', true, 'HITO'),
('PROJ', 'P-24', 'STAGE 3', '0. Program', 'Master Plan', 'Stage Revision (line in Mexico):', true, 'HITO'),

('PROJ', 'P-25', 'STAGE 4', '0. Program', 'Master Plan', '12.- Pilot Run', true, 'HITO'),
('QE', 'P-26', 'STAGE 4', '0. Program', 'Master Plan', '13.- MSA', true, 'HITO'),
('QE/PE', 'P-27', 'STAGE 4', '0. Program', 'Master Plan', '14.- Preliminary SPC', true, 'HITO'),
('QE', 'P-28', 'STAGE 4', '0. Program', 'Master Plan', '15.- PPAP', true, 'HITO'),
('QE', 'P-29', 'STAGE 4', '0. Program', 'Master Plan', '16.- Production Validation Testing', true, 'HITO'),
('PE', 'P-30', 'STAGE 4', '0. Program', 'Master Plan', '17.- Packaging evaluation', true, 'HITO'),
('QE', 'P-31', 'STAGE 4', '0. Program', 'Master Plan', '18.- Production Control Plan', true, 'HITO'),
('PE', 'P-32', 'STAGE 4', '0. Program', 'Master Plan', '04.- Floor plan layout', true, 'HITO'),
('PROJ', 'P-33', 'STAGE 4', '0. Program', 'Master Plan', '19.- Sign-OFF', true, 'HITO'),

('QE/PE', 'P-34', 'STAGE 5', '0. Program', 'Master Plan', '20.- Reduced Variation', true, 'HITO'),
('QE', 'P-35', 'STAGE 5', '0. Program', 'Master Plan', '21.- Improve customer satisfaction', true, 'HITO'),
('QE', 'P-36', 'STAGE 5', '0. Program', 'Master Plan', '22.- Improved delivery and service', true, 'HITO'),
('QE/PE', 'P-37', 'STAGE 5', '0. Program', 'Master Plan', '23.- Effective use of Lesson Learned/Best practice', true, 'HITO'),
('PROJ', 'P-38', 'STAGE 5', '0. Program', 'Master Plan', '25.- Formal project delivery to production:', true, 'HITO');

-- =====================================================================
-- STAGE 2 PREGUNTAS
-- =====================================================================
INSERT INTO catalogo_elementos (champion, codigo, etapa_visual, fase, grupo, nombre, requerido, tipo_input) VALUES 
('Project Engineer', 'S2-01', null, '2. Stage 2', 'Preliminary information', 'Is a completed CFT available?', true, 'PREGUNTA'),
('Design Engineer', 'S2-02', null, '2. Stage 2', 'Preliminary information', 'Is the complete DFMEA available?', true, 'PREGUNTA'),
('Design Engineer', 'S2-03', null, '2. Stage 2', 'Preliminary information', 'Is a Preliminary BOM available?', true, 'PREGUNTA'),
('Design Engineer', 'S2-04', null, '2. Stage 2', 'Preliminary information', 'Are the drawings available?', true, 'PREGUNTA'),
('QE/PE', 'S2-05', null, '2. Stage 2', 'Preliminary information', 'Are equipment lists available?', true, 'PREGUNTA'),
('Project Engineer', 'S2-06', null, '2. Stage 2', 'Preliminary information', 'Is the Team Feasibility Commitment signed?', true, 'PREGUNTA'),
('QE/PE', 'S2-07', null, '2. Stage 2', 'Preliminary information', 'Are lessons learned documented?', true, 'PREGUNTA'),
('Project Engineer', 'S2-08', null, '2. Stage 2', 'Preliminary information', 'Is the Supplier Manual available?', true, 'PREGUNTA'),
('Project Engineer', 'S2-09', null, '2. Stage 2', 'Preliminary information', 'Was the DV Report provided?', true, 'PREGUNTA'),
('Design Engineer', 'S2-10', null, '2. Stage 2', 'Preliminary information', 'Design Validation Plan (DVP)?', true, 'PREGUNTA'),
('Quality Engineer', 'S2-11', null, '2. Stage 2', 'Engineering Drawings', 'Process Validation Plan (PVP)?', true, 'PREGUNTA'),
('Quality Engineer', 'S2-12', null, '2. Stage 2', 'Engineering Drawings', 'Preliminary Control Plan?', true, 'PREGUNTA'),
('Process Engineer', 'S2-13', null, '2. Stage 2', 'Engineering Drawings', 'Preliminary Process Flow Diagram?', true, 'PREGUNTA'),
('Process Engineer', 'S2-14', null, '2. Stage 2', 'Engineering Drawings', 'Preliminary Layout?', true, 'PREGUNTA'),
('Process Engineer', 'S2-15', null, '2. Stage 2', 'New components', 'Preliminary Packaging Plan?', true, 'PREGUNTA'),
('SCS Procurement', 'S2-16', null, '2. Stage 2', 'New components', 'Is the list aligned with the RFQ tracker?', true, 'PREGUNTA'),
('SCS Procurement', 'S2-17', null, '2. Stage 2', 'New components', 'Are the suppliers of new materials known?', true, 'PREGUNTA'),
('Project Engineer', 'S2-18', null, '2. Stage 2', 'New components', 'Are special characteristics identified?', true, 'PREGUNTA'),
('SCS Procurement', 'S2-19', null, '2. Stage 2', 'New components', 'Was the lead time considered?', true, 'PREGUNTA'),
('SCS Procurement', 'S2-20', null, '2. Stage 2', 'New components', 'Are QRs for new components available?', true, 'PREGUNTA'),
('Finance Rep', 'S2-21', null, '2. Stage 2', 'New components', 'Are TP for new components available?', true, 'PREGUNTA'),
('Design Engineer', 'S2-22', null, '2. Stage 2', 'Preliminary Customer Characteristics List:', 'Preliminary list of customer characteristics?', true, 'PREGUNTA'),
('Design Engineer', 'S2-23', null, '2. Stage 2', 'Preliminary Customer Characteristics List:', 'Is the list endorsed by the customer''s signature?', true, 'PREGUNTA');

-- =====================================================================
-- GATE REVIEWS (STAGE 3, 4, 5)
-- =====================================================================
INSERT INTO catalogo_elementos (champion, codigo, etapa_visual, fase, grupo, nombre, requerido, tipo_input) VALUES 
('N/A', 'GATE-01', null, '3. Stage 3', 'Validation', 'Are all APQP Checklist items closed?', true, 'GATE'),
('N/A', 'GATE-02', null, '3. Stage 3', 'Validation', 'Are deliverables validated and audited by the team?', true, 'GATE'),
('N/A', 'GATE-03', null, '3. Stage 3', 'Validation', 'Were deliverables completed on time?', true, 'GATE'),
('N/A', 'CONC-01', null, '3. Stage 3', 'Conclusion', 'CLOSE: The project can be closed.', true, 'GATE'),
('N/A', 'CONC-02', null, '3. Stage 3', 'Conclusion', 'DEVIATION: Minor open situations.', true, 'GATE'),
('N/A', 'CONC-03', null, '3. Stage 3', 'Conclusion', 'OPEN: Insufficient evidence.', true, 'GATE'),

('N/A', 'GATE-01', null, '4. Stage 4', 'Validation', 'Are all APQP Checklist items closed?', true, 'GATE'),
('N/A', 'GATE-02', null, '4. Stage 4', 'Validation', 'Are deliverables validated and audited by the team?', true, 'GATE'),
('N/A', 'GATE-03', null, '4. Stage 4', 'Validation', 'Were deliverables completed on time?', true, 'GATE'),
('N/A', 'CONC-01', null, '4. Stage 4', 'Conclusion', 'CLOSE: The project can be closed.', true, 'GATE'),
('N/A', 'CONC-02', null, '4. Stage 4', 'Conclusion', 'DEVIATION: Minor open situations.', true, 'GATE'),
('N/A', 'CONC-03', null, '4. Stage 4', 'Conclusion', 'OPEN: Insufficient evidence.', true, 'GATE'),

('N/A', 'GATE-01', null, '5. Stage 5', 'Validation', 'Are all APQP Checklist items closed?', true, 'GATE'),
('N/A', 'GATE-02', null, '5. Stage 5', 'Validation', 'Are deliverables validated and audited by the team?', true, 'GATE'),
('N/A', 'GATE-03', null, '5. Stage 5', 'Validation', 'Were deliverables completed on time?', true, 'GATE'),
('N/A', 'CONC-01', null, '5. Stage 5', 'Conclusion', 'CLOSE: The project can be closed.', true, 'GATE'),
('N/A', 'CONC-02', null, '5. Stage 5', 'Conclusion', 'DEVIATION: Minor open situations.', true, 'GATE'),
('N/A', 'CONC-03', null, '5. Stage 5', 'Conclusion', 'OPEN: Insufficient evidence.', true, 'GATE');