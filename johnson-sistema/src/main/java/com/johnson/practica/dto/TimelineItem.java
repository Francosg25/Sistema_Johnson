package com.johnson.practica.dto;

public class TimelineItem {
    private Long id;
    private Long group;
    private String content;
    private String start;
    private String type;

    public TimelineItem(Long id, Long group, String content, String start, String type) {
        this.id = id;
        this.group = group;
        this.content = content;
        this.start = start;
        this.type = type; 
    }

    public Long getId() { return id; }
    public Long getGroup() { return group; }
    public String getContent() { return content; }
    public String getStart() { return start; }
    public String getType() { return type; }
}