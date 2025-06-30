package edu.neu.weather.activity.utils;

public class LunarDate {
    private int year;
    private String month;
    private String day;
    private String yearName;

    public LunarDate(int year, String month, String day, String yearName) {
        this.year = year;
        this.month = month;
        this.day = day;
        this.yearName = yearName;
    }

    public int getYear() {
        return year;
    }

    public String getMonth() {
        return month;
    }

    public String getDay() {
        return day;
    }

    public String getYearName() {
        return yearName;
    }

    public String getDisplayName() {
        return month + day;
    }

    public String getLunarDayName() {
        return day;
    }

    /**
     * 获取农历月份的数字索引（1-12�?
     */
    public int getMonthIndex() {

        String monthStr = month.replace("", "");
        String[] months = {"正月", "二月", "三月", "四月", "五月", "六月",
                "七月", "八月", "九月", "十月", "冬月", "腊月"};
        for (int i = 0; i < months.length; i++) {
            if (months[i].equals(monthStr)) {
                return i + 1; // 返回1-12
            }
        }
        return 1; // 默认返回正月
    }



    public int getDayIndex() {
        String[] days = {"初一", "初二", "初三", "初四", "初五", "初六", "初七", "初八", "初九", "初十",
                "十一", "十二", "十三", "十四", "十五", "十六", "十七", "十八", "十九", "二十",
                "廿一", "廿二", "廿三", "廿四", "廿五", "廿六", "廿七", "廿八", "廿九", "三十"};
        for (int i = 0; i < days.length; i++) {
            if (days[i].equals(day)) {
                return i + 1; // 返回1-30
            }
        }
        return 1; // 默认返回初一
    }
}
