package com.johnson.practica.servicio;


import com.johnson.practica.modelo.Proyecto;
import com.johnson.practica.repositorio.ProyectoRepositorio;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProyectoRepositorioTest {

    @Autowired
    private ProyectoRepositorio proyectoRepositorio;

    @Test
    void debeTraerSoloProyectosActivos() {
        // 1. Guardar un proyecto activo
        Proyecto pActivo = new Proyecto();
        pActivo.setNombre("Proyecto Activo");
        pActivo.setEsHistorico(false);
        proyectoRepositorio.save(pActivo);

        // 2. Guardar un proyecto archivado/histórico
        Proyecto pHistorico = new Proyecto();
        pHistorico.setNombre("Proyecto Viejo");
        pHistorico.setEsHistorico(true);
        proyectoRepositorio.save(pHistorico);

        // 3. Probar tu método personalizado
        List<Proyecto> activos = proyectoRepositorio.findByEsHistoricoFalse();

        // 4. Validar que el filtro funciona
        assertFalse(activos.isEmpty());
        boolean contieneHistorico = activos.stream().anyMatch(Proyecto::getEsHistorico);
        assertFalse(contieneHistorico, "La consulta trajo un proyecto histórico por error");
    }
}