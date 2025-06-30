package edu.neu.weather.activity.calendar;

import java.util.Date;

public class ScheduleItem {
    private long id;
    private String title;
    private String content;
    private Date date;
    private boolean hasReminder;
    private Date reminderTime;

    public ScheduleItem(long id, String title, String content, Date date, boolean hasReminder, Date reminderTime) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.date = date;
        this.hasReminder = hasReminder;
        this.reminderTime = reminderTime;
    }

    // Getters and Setters
    public long getId() { return id; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public Date getDate() { return date; }
    public boolean hasReminder() { return hasReminder; }
    public Date getReminderTime() { return reminderTime; }

    public void setId(long id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setContent(String content) { this.content = content; }
    public void setDate(Date date) { this.date = date; }
    public void setHasReminder(boolean hasReminder) { this.hasReminder = hasReminder; }
    public void setReminderTime(Date reminderTime) { this.reminderTime = reminderTime; }
}
