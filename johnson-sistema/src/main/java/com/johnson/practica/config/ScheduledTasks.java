package com.johnson.practica.config;

import com.johnson.practica.servicio.ChecklistServicio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledTasks {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledTasks.class);

    @Autowired
    private ChecklistServicio checklistServicio;

    /**
     * Se ejecuta todos los días a las 00:01 AM para marcar como "LATE" los entregables vencidos.
     */
    @Scheduled(cron = "0 1 0 * * *")
    public void marcarEntregablesVencidos() {
        logger.info("Iniciando tarea programada: Marcando entregables vencidos (LATE)...");
        try {
            checklistServicio.actualizarEntregablesVencidos();
            logger.info("Tarea programada finalizada con éxito.");
        } catch (Exception e) {
            logger.error("Error al ejecutar la tarea programada de entregables vencidos: {}", e.getMessage());
        }
    }
}
