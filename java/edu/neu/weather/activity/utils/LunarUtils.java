package edu.neu.weather.activity.utils;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 农历日期工具类（支持1900-2100年）
 */
public class LunarUtils {

    private static final long[] lunarInfo = {
            0x04bd8, 0x04ae0, 0x0a570, 0x054d5, 0x0d260, 0x0d950, 0x16554, 0x056a0, 0x09ad0, 0x055d2,
            0x04ae0, 0x0a5b6, 0x0a4d0, 0x0d250, 0x1d255, 0x0b540, 0x0d6a0, 0x0ada2, 0x095b0, 0x14977,
            0x04970, 0x0a4b0, 0x0b4b5, 0x06a50, 0x06d40, 0x1ab54, 0x02b60, 0x09570, 0x052f2, 0x04970,
            0x06566, 0x0d4a0, 0x0ea50, 0x06e95, 0x05ad0, 0x02b60, 0x186e3, 0x092e0, 0x1c8d7, 0x0c950,
            0x0d4a0, 0x1d8a6, 0x0b550, 0x056a0, 0x1a5b4, 0x025d0, 0x092d0, 0x0d2b2, 0x0a950, 0x0b557,
            0x06ca0, 0x0b550, 0x15355, 0x04da0, 0x0a5d0, 0x14573, 0x052d0, 0x0a9a8, 0x0e950, 0x06aa0,
            0x0aea6, 0x0ab50, 0x04b60, 0x0aae4, 0x0a570, 0x05260, 0x0f263, 0x0d950, 0x05b57, 0x056a0,
            0x096d0, 0x04dd5, 0x04ad0, 0x0a4d0, 0x0d4d4, 0x0d250, 0x0d558, 0x0b540, 0x0b5a0, 0x195a6,
            0x095b0, 0x049b0, 0x0a974, 0x0a4b0, 0x0b27a, 0x06a50, 0x06d40, 0x0af46, 0x0ab60, 0x09570,
            0x04af5, 0x04970, 0x064b0, 0x074a3, 0x0ea50, 0x06b58, 0x055c0, 0x0ab60, 0x096d5, 0x092e0,
            0x0c960, 0x0d954, 0x0d4a0, 0x0da50, 0x07552, 0x056a0, 0x0abb7, 0x025d0, 0x092d0, 0x0cab5,
            0x0a950, 0x0b4a0, 0x0baa4, 0x0ad50, 0x055d9, 0x04ba0, 0x0a5b0, 0x15176, 0x052b0, 0x0a930,
            0x07954, 0x06aa0, 0x0ad50, 0x05b52, 0x04b60, 0x0a6e6, 0x0a4e0, 0x0d260, 0x0ea65, 0x0d530,
            0x05aa0, 0x076a3, 0x096d0, 0x04bd7, 0x04ad0, 0x0a4d0, 0x1d0b6, 0x0d250, 0x0d520, 0x0dd45,
            0x0b5a0, 0x056d0, 0x055b2, 0x049b0, 0x0a577, 0x0a4b0, 0x0aa50, 0x1b255, 0x06d20, 0x0ada0
    };

    // 24节气
    private static final String[] solarTerms = {
            "小寒", "大寒", "立春", "雨水", "惊蛰", "春分", "清明", "谷雨",
            "立夏", "小满", "芒种", "夏至", "小暑", "大暑", "立秋", "处暑",
            "白露", "秋分", "寒露", "霜降", "立冬", "小雪", "大雪", "冬至"
    };

    // 农历月份
    private static final String[] chineseMonths = {
            "正月", "二月", "三月", "四月", "五月", "六月", "七月", "八月", "九月", "十月", "冬月", "腊月"
    };

    // 农历日
    private static final String[] chineseDays = {
            "初一", "初二", "初三", "初四", "初五", "初六", "初七", "初八", "初九", "初十",
            "十一", "十二", "十三", "十四", "十五", "十六", "十七", "十八", "十九", "二十",
            "廿一", "廿二", "廿三", "廿四", "廿五", "廿六", "廿七", "廿八", "廿九", "三十"
    };

    // 天干
    private static final String[] heavenlyStems = {
            "甲", "乙", "丙", "丁", "戊", "己", "庚", "辛", "壬", "癸"
    };

    // 地支
    private static final String[] earthlyBranches = {
            "子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥"
    };

    // 生肖
    private static final String[] zodiacs = {
            "鼠", "牛", "虎", "兔", "龙", "蛇", "马", "羊", "猴", "鸡", "狗", "猪"
    };

    // 节日表（公历和农历分开存储）
    private static final Map<String, String> solarHolidays = new HashMap<>();
    private static final Map<String, String> lunarHolidays = new HashMap<>();

    static {
        // 公历节日
        solarHolidays.put("01-01", "元旦");
        solarHolidays.put("02-14", "情人节");
        solarHolidays.put("03-08", "妇女节");
        solarHolidays.put("05-01", "劳动节");
        solarHolidays.put("06-01", "儿童节");
        solarHolidays.put("10-01", "国庆节");
        solarHolidays.put("12-25", "圣诞节");

        // 农历节日（使用农历月份和日）
        lunarHolidays.put("01-01", "春节");
        lunarHolidays.put("01-15", "元宵节");
        lunarHolidays.put("05-05", "端午节");
        lunarHolidays.put("07-07", "七夕");
        lunarHolidays.put("08-15", "中秋节");
        lunarHolidays.put("09-09", "重阳节");
    }

    /**
     * 获取农历日期信息
     */
    public static LunarDate getLunarDate(Calendar calendar) {
        int year = calendar.get(Calendar.YEAR);
        if (year < 1900 || year >= 2100) {
            return new LunarDate(year, "无效年份", "无效日期", "");
        }

        try {
            int[] lunar = solarToLunar(
                    year,
                    calendar.get(Calendar.MONTH) + 1,
                    calendar.get(Calendar.DAY_OF_MONTH)
            );

            int lunarYear = lunar[0];
            int lunarMonth = lunar[1];
            int lunarDay = lunar[2];
            boolean isLeap = lunar[3] == 1;

            // 检查月份和日是否有效
            if (lunarMonth < 1 || lunarMonth > 12 || lunarDay < 1 || lunarDay > 30) {
                return new LunarDate(lunarYear, "无效日期", "无效日期", "");
            }

            String monthName = chineseMonths[lunarMonth - 1];
            if (isLeap) {
                monthName = "闰" + monthName;
            }

            String dayName = chineseDays[lunarDay - 1];

            // 计算干支年
            int stemIndex = (lunarYear - 4) % 10;
            int branchIndex = (lunarYear - 4) % 12;
            String stem = heavenlyStems[(stemIndex + 10) % 10];
            String branch = earthlyBranches[(branchIndex + 12) % 12];
            String zodiac = zodiacs[(lunarYear - 4) % 12];
            String yearName = stem + branch + zodiac + "年";

            return new LunarDate(lunarYear, monthName, dayName, yearName);
        } catch (Exception e) {
            return new LunarDate(year, "转换错误", "无效日期", "");
        }
    }

    /**
     * 获取节气
     */
    public static String getSolarTerm(Calendar calendar) {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // 简化版节气计算（精确算法需使用天文公式）
        int[] termDays = {
                5,20, 4,18, 5,20, 4,19, 5,20, 5,21,  // 上半年
                7,22, 7,23, 7,23, 8,23, 7,22, 7,21   // 下半年
        };

        int index = month * 2;
        if (day >= termDays[index]) {
            index++;
        }
        if (index >= 24) index = 0;

        return solarTerms[index];
    }

    /**
     * 获取节日（自动判断公历农历）
     */
    public static String getHoliday(Calendar calendar) {
        // 先检查公历节日
        String solarKey = String.format("%02d-%02d",
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH));
        String holiday = solarHolidays.get(solarKey);
        if (holiday != null) return holiday;

        // 检查农历节日
        LunarDate lunarDate = getLunarDate(calendar);
        String lunarKey = String.format("%02d-%02d",
                lunarDate.getMonthIndex(),
                lunarDate.getDayIndex());
        return lunarHolidays.getOrDefault(lunarKey, "");
    }

    /**
     * 公历转农历
     */
    private static int[] solarToLunar(int year, int month, int day) {
        int[] lunarDate = new int[4];
        int i, leap = 0, temp = 0;

        // 基准日期：1900年1月31日（公历）
        Calendar baseCalendar = Calendar.getInstance();
        baseCalendar.set(1900, 0, 31);
        long baseMillis = baseCalendar.getTimeInMillis();

        // 目标日期
        Calendar objCalendar = Calendar.getInstance();
        objCalendar.set(year, month - 1, day);
        long objMillis = objCalendar.getTimeInMillis();

        // 计算天数差
        long offset = (objMillis - baseMillis) / (1000 * 60 * 60 * 24);

        // 计算农历年
        for (i = 1900; i < 2100 && offset > 0; i++) {
            temp = daysInLunarYear(i);
            offset -= temp;
        }
        if (offset < 0) {
            offset += temp;
            i--;
        }
        lunarDate[0] = i;

        // 计算农历月
        leap = leapMonth(i);
        boolean isLeap = false;
        for (i = 1; i < 13 && offset > 0; i++) {
            if (leap > 0 && i == (leap + 1) && !isLeap) {
                --i;
                isLeap = true;
                temp = daysInLeapMonth(lunarDate[0]);
            } else {
                temp = daysInLunarMonth(lunarDate[0], i);
            }

            if (isLeap && i == (leap + 1)) isLeap = false;
            offset -= temp;
        }

        if (offset == 0 && leap > 0 && i == leap + 1) {
            if (isLeap) {
                isLeap = false;
            } else {
                isLeap = true;
                --i;
            }
        }
        if (offset < 0) {
            offset += temp;
            --i;
        }

        lunarDate[1] = i;
        lunarDate[2] = (int)(offset + 1);
        lunarDate[3] = isLeap ? 1 : 0;

        return lunarDate;
    }

    // 以下为辅助方法
    private static int leapMonth(int year) {
        return (int)(lunarInfo[year - 1900] & 0xf);
    }

    private static int daysInLunarYear(int year) {
        int sum = 348; // 12个小月的总天数（12*29）
        for (int i = 0x8000; i > 0x8; i >>= 1) {
            sum += ((lunarInfo[year - 1900] & i) != 0 ? 1 : 0);
        }
        return sum + daysInLeapMonth(year);
    }

    private static int daysInLunarMonth(int year, int month) {
        return ((lunarInfo[year - 1900] & (0x10000 >> month)) == 0 ? 29 : 30);
    }

    private static int daysInLeapMonth(int year) {
        if (leapMonth(year) != 0) {
            return (((lunarInfo[year - 1900] & 0x10000) != 0) ? 30 : 29);
        }
        return 0;
    }
}