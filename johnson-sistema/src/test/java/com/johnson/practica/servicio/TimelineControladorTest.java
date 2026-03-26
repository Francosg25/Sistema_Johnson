package com.johnson.practica.servicio;

import com.johnson.practica.controlador.TimelineControlador;
import com.johnson.practica.dto.ReporteEstadoGlobal;
import com.johnson.practica.repositorio.ProyectoRepositorio;
import com.johnson.practica.repositorio.UsuarioRepositorio; 
import com.johnson.practica.servicio.ChecklistReporteServicio;
import com.johnson.practica.servicio.NotificacionServicio;
import com.johnson.practica.servicio.UsuarioServicio;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(TimelineControlador.class)
class TimelineControladorTest {

    @Autowired
    private MockMvc mockMvc;

    // --- Mocks para el Controlador ---
    @MockBean
    private ChecklistReporteServicio reporteServicio;

    @MockBean
    private ProyectoRepositorio proyectoRepositorio;

    // --- Mocks para la Seguridad / GlobalAtributos ---
    @MockBean
    private UsuarioServicio usuarioServicio;

    @MockBean
    private NotificacionServicio notificacionServicio;

    @MockBean
    private UsuarioRepositorio usuarioRepositorio; 

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void debeCargarPaginaTimelineCorrectamente() throws Exception {
        // Simulamos datos mínimos
        when(reporteServicio.obtenerDatosTimeline()).thenReturn(new HashMap<>());
        when(proyectoRepositorio.findByEsHistoricoFalse()).thenReturn(List.of());
        when(reporteServicio.generarReporteEstadoGlobal()).thenReturn(new ReporteEstadoGlobal());

        mockMvc.perform(get("/timeline"))
               .andExpect(status().isOk())
               .andExpect(view().name("proyectos/timeline"));
    }
}