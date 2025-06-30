package edu.neu.weather.activity.calendar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import edu.neu.weather.R;
import edu.neu.weather.activity.SQLite.DiaryDBHelper;
import edu.neu.weather.activity.utils.LunarDate;
import edu.neu.weather.activity.utils.LunarUtils;

public class DayViewActivity extends AppCompatActivity {
    private TextView tvDate, tvLunarDate, tvDiaryContent, tvScheduleContent;
    private DiaryDBHelper dbHelper;
    private Date currentDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        int themeMode = prefs.getInt("theme_mode", 0); // 0: 跟随系统, 1: 浅色, 2: 深色
        switch (themeMode) {
            case 1:
                setTheme(R.style.Theme_Weather_Light);
                break;
            case 2:
                setTheme(R.style.Theme_Weather_Dark);
                break;
            default:
                setTheme(R.style.Theme_Weather_FollowSystem);
                break;
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_day_view);

        dbHelper = new DiaryDBHelper(this);
        initViews();
        setupDate();
    }

    private void initViews() {
        tvDate = findViewById(R.id.tvDate);
        tvLunarDate = findViewById(R.id.tvLunarDate);
        tvDiaryContent = findViewById(R.id.tvDiaryContent);
        tvScheduleContent = findViewById(R.id.tvScheduleContent);
    }

    private void setupDate() {
        long dateMillis = getIntent().getLongExtra("date", System.currentTimeMillis());
        currentDate = new Date(dateMillis);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);

        // Set Gregorian date
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年M月d�?EEEE", Locale.getDefault());
        tvDate.setText(sdf.format(currentDate));

        // Set Lunar date
        LunarDate lunarDate = LunarUtils.getLunarDate(calendar);
        tvLunarDate.setText(lunarDate.toString());

        // Load data
        loadData();
    }

    private void loadData() {
        // Get diary content
        String diaryContent = dbHelper.getDiaryForDate(currentDate);
        tvDiaryContent.setText(diaryContent != null ? diaryContent : "暂无日记");

        // Get schedules
        List<ScheduleItem> schedules = dbHelper.getSchedulesForDate(currentDate);
        StringBuilder scheduleText = new StringBuilder();

        for (ScheduleItem schedule : schedules) {
            if (scheduleText.length() > 0) {
                scheduleText.append("\n\n");
            }
            scheduleText.append(schedule.getTitle())
                    .append("\n")
                    .append(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(schedule.getDate()))
                    .append("\n")
                    .append(schedule.getContent());
        }

        tvScheduleContent.setText(scheduleText.length() > 0 ? scheduleText.toString() : "暂无日程");
    }

    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }
}
