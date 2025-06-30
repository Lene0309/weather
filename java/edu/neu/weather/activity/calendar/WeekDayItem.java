package edu.neu.weather.activity.calendar;

import java.util.Date;
import java.util.List;

import edu.neu.weather.activity.utils.LunarDate;

public class WeekDayItem {
    private Date date;
    private LunarDate lunarDate;
    private String holiday;
    private String solarTerm;
    private String diary;
    private List<ScheduleItem> schedules;

    public WeekDayItem(Date date, LunarDate lunarDate, String holiday, String solarTerm, String diary, List<ScheduleItem> schedules) {
        this.date = date;
        this.lunarDate = lunarDate;
        this.holiday = holiday;
        this.solarTerm = solarTerm;
        this.diary = diary;
        this.schedules = schedules;
    }

    // Getters
    public Date getDate() { return date; }
    public LunarDate getLunarDate() { return lunarDate; }
    public String getHoliday() { return holiday; }
    public String getSolarTerm() { return solarTerm; }
    public String getDiary() { return diary; }
    public List<ScheduleItem> getSchedules() { return schedules; }
}
