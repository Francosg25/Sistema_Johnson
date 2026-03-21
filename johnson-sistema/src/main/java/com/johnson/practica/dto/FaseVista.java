package com.johnson.practica.dto;

import com.johnson.practica.modelo.ElementoChecklist;
import com.johnson.practica.modelo.FirmaEtapa;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FaseVista {
    private String id;
    private String nombre;
    private List<ElementoChecklist> items;
    private Map<String, FirmaEtapa> firmas;
}
