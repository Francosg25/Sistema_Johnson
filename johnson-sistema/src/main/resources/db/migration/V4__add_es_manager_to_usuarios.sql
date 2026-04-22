ALTER TABLE usuarios ADD COLUMN es_manager BOOLEAN DEFAULT FALSE;
UPDATE usuarios SET es_manager = TRUE WHERE username = 'admin';
