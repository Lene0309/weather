package edu.neu.weather.activity.calendar;

import java.util.Date;

public class DiaryItem {
    private long id;
    private String title;
    private String content;
    private Date date;

    public DiaryItem(long id, String title, String content, Date date) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.date = date;
    }

    // Getters and Setters
    public long getId() { return id; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public Date getDate() { return date; }

    public void setId(long id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setContent(String content) { this.content = content; }
    public void setDate(Date date) { this.date = date; }
}
