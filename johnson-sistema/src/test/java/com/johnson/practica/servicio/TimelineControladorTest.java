package com.johnson.practica.servicio;
import com.johnson.practica.controlador.TimelineControlador;
import com.johnson.practica.dto.ReporteEstadoGlobal;
import com.johnson.practica.servicio.ChecklistReporteServicio;
import com.johnson.practica.repositorio.ProyectoRepositorio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TimelineControlador.class)
class TimelineControladorTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChecklistReporteServicio reporteServicio;

    @MockBean
    private ProyectoRepositorio proyectoRepositorio;

    @Test
    @WithMockUser(roles = "ADMIN") // Simula un usuario logueado
    void debeCargarPaginaTimelineCorrectamente() throws Exception {
        // Preparar datos falsos para que el controlador no truene
        when(reporteServicio.obtenerDatosTimeline()).thenReturn(new HashMap<>());
        when(proyectoRepositorio.findByEsHistoricoFalse()).thenReturn(List.of());
        when(reporteServicio.generarReporteEstadoGlobal()).thenReturn(new ReporteEstadoGlobal());

        // Simular la petición HTTP GET a /timeline
        mockMvc.perform(get("/timeline"))
               .andExpect(status().isOk()) // Esperamos un HTTP 200
               .andExpect(view().name("proyectos/timeline")) // Debe cargar la vista correcta
               .andExpect(model().attributeExists("timelineGroups"))
               .andExpect(model().attributeExists("timelineItems"))
               .andExpect(model().attributeExists("proyectos"));
    }
}