package com.johnson.practica.servicio;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;

@Service
public class BackupServicio {

    private static final Logger logger = Logger.getLogger(BackupServicio.class.getName());

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUser;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    // Ejecutar todos los días a las 2:00 AM
    @Scheduled(cron = "0 0 2 * * *")
    public void realizarBackup() {
        try {
            String backupDir = "backups";
            Files.createDirectories(Paths.get(backupDir));

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = backupDir + File.separator + "backup_" + timestamp + ".sql";

            // 1. Limpiar la URL de "jdbc:postgresql://"
            String cleanUrl = dbUrl.replace("jdbc:postgresql://", "");
            
            // 2. Separar Host/Puerto del Nombre de la BD
            String hostPort = cleanUrl.split("/")[0];
            String fullDbName = cleanUrl.split("/")[1];
            
            // 3. CORRECCIÓN CRÍTICA: Eliminar parámetros como "?sslmode=disable" del nombre de la BD
            String dbName = fullDbName.contains("?") ? fullDbName.split("\\?")[0] : fullDbName;
            
            // 4. Extraer Host y Puerto
            String host = hostPort.split(":")[0];
            String port = hostPort.contains(":") ? hostPort.split(":")[1] : "5432";

            ProcessBuilder pb = new ProcessBuilder(
                "pg_dump",
                "-h", host,
                "-p", port,
                "-U", dbUser,
                "-F", "p", // formato plain sql
                "-f", fileName,
                dbName
            );

            // Pasar la contraseña vía variable de entorno para evitar prompts en la consola
            pb.environment().put("PGPASSWORD", dbPassword);

            Process process = pb.start();
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                logger.info("Backup realizado con éxito: " + fileName);
            } else {
                logger.severe("Error al realizar el backup. Código de salida de pg_dump: " + exitCode);
            }

            // Limpieza opcional: borrar backups de más de 30 días
            limpiarBackupsAntiguos(backupDir, 30);

        } catch (IOException | InterruptedException e) {
            logger.severe("Excepción durante el backup: " + e.getMessage());
        }
    }

    private void limpiarBackupsAntiguos(String dir, int dias) {
        File directory = new File(dir);
        File[] files = directory.listFiles();
        if (files != null) {
            long threshold = System.currentTimeMillis() - ((long) dias * 24 * 60 * 60 * 1000);
            for (File file : files) {
                if (file.lastModified() < threshold) {
                    if (file.delete()) {
                        logger.info("Backup antiguo eliminado: " + file.getName());
                    }
                }
            }
        }
    }
}