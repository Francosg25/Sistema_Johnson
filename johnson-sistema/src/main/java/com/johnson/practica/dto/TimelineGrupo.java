package com.johnson.practica.dto;

public class TimelineGrupo {
    private Long id;
    private String content;

    public TimelineGrupo(Long id, String content) {
        this.id = id;
        this.content = content;
    }

    public Long getId() { return id; }
    public String getContent() { return content; }
}