package com.johnson.practica.servicio;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.util.stream.Stream;

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
            
            // 1. Respaldar Base de Datos (.sql)
            realizarBackupBD(backupDir, timestamp);
            
            // 2. Respaldar Evidencias (.zip)
            respaldarEvidencias(backupDir, timestamp);

            // 3. Limpieza opcional: borrar archivos de más de 30 días
            limpiarBackupsAntiguos(backupDir, 30);

        } catch (IOException e) {
            logger.severe("Excepción durante el proceso de backup: " + e.getMessage());
        }
    }

    private void realizarBackupBD(String backupDir, String timestamp) {
        String fileName = backupDir + File.separator + "backup_" + timestamp + ".sql";
        try {
            // 1. Limpiar la URL de "jdbc:postgresql://"
            String cleanUrl = dbUrl.replace("jdbc:postgresql://", "");
            
            // 2. Separar Host/Puerto del Nombre de la BD
            String hostPort = cleanUrl.split("/")[0];
            String fullDbName = cleanUrl.split("/")[1];
            
            // 3. Eliminar parámetros adicionales
            String dbName = fullDbName.contains("?") ? fullDbName.split("\\?")[0] : fullDbName;
            
            // 4. Extraer Host y Puerto
            String host = hostPort.split(":")[0];
            String port = hostPort.contains(":") ? hostPort.split(":")[1] : "5432";

            ProcessBuilder pb = new ProcessBuilder(
                "pg_dump",
                "-h", host,
                "-p", port,
                "-U", dbUser,
                "-F", "p", 
                "-f", fileName,
                dbName
            );

            pb.environment().put("PGPASSWORD", dbPassword);

            Process process = pb.start();
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                logger.info("Backup de BD realizado con éxito: " + fileName);
            } else {
                logger.severe("Error al realizar el backup de BD. Código: " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            logger.severe("Error en pg_dump: " + e.getMessage());
        }
    }

    private void respaldarEvidencias(String backupDir, String timestamp) {
        String sourceDir = "evidencias";
        String zipFileName = backupDir + File.separator + "evidencias_" + timestamp + ".zip";
        Path sourcePath = Paths.get(sourceDir);

        if (!Files.exists(sourcePath)) {
            logger.warning("La carpeta de evidencias no existe: " + sourceDir);
            return;
        }

        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFileName));
             Stream<Path> paths = Files.walk(sourcePath)) {
            
            paths.filter(path -> !Files.isDirectory(path))
                 .forEach(path -> {
                     ZipEntry zipEntry = new ZipEntry(sourcePath.relativize(path).toString());
                     try {
                         zos.putNextEntry(zipEntry);
                         Files.copy(path, zos);
                         zos.closeEntry();
                     } catch (IOException e) {
                         logger.severe("Error al añadir archivo al zip: " + path + " - " + e.getMessage());
                     }
                 });
            logger.info("Respaldo de evidencias (ZIP) completado: " + zipFileName);
        } catch (IOException e) {
            logger.severe("Error al crear el archivo ZIP de evidencias: " + e.getMessage());
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
                        logger.info("Archivo antiguo eliminado: " + file.getName());
                    }
                }
            }
        }
    }
}
