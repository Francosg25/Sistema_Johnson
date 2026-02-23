package com.johnson.practica.dto;

public class TimelineItem {
    private Long id;
    private Long group;
    private String content;
    private String start;
    private String type;
    private String className; 

    public TimelineItem(Long id, Long group, String content, String start, String type, String className) {
        this.id = id;
        this.group = group;
        this.content = content;
        this.start = start;
        this.type = type;
        this.className = className; 
    }

    public Long getId() { return id; }
    public Long getGroup() { return group; }
    public String getContent() { return content; }
    public String getStart() { return start; }
    public String getType() { return type; }
    public String getClassName() { return className; }
}