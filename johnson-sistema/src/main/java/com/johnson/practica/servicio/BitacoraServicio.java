package com.johnson.practica.servicio;

import com.johnson.practica.modelo.Bitacora;
import com.johnson.practica.repositorio.BitacoraRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BitacoraServicio {

    @Autowired
    private BitacoraRepositorio repositorio;

    @Transactional
    public void registrarAccion(String usuario, String accion, String detalle) {
        Bitacora registro = new Bitacora(usuario, accion, detalle);
        repositorio.save(registro);
    }

    @Transactional(readOnly = true)
    public List<Bitacora> obtenerUltimosMovimientos() {
        return repositorio.findTop50ByOrderByFechaDesc();
    }
    
}