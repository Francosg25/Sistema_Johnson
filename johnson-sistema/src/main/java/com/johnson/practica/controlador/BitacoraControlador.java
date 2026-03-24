package com.johnson.practica.controlador;

import com.johnson.practica.servicio.BitacoraServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/bitacora")
public class BitacoraControlador {

    @Autowired
    private BitacoraServicio bitacoraServicio;

    @GetMapping
    public String verBitacora(Model model) {
        model.addAttribute("movimientos", bitacoraServicio.obtenerUltimosMovimientos());
        return "proyectos/bitacora";
    }
}
