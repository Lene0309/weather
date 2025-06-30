package edu.neu.weather.activity.calendar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.GridView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import edu.neu.weather.R;
import edu.neu.weather.activity.SQLite.DiaryDBHelper;
import edu.neu.weather.activity.utils.LunarDate;
import edu.neu.weather.activity.utils.LunarUtils;

public class WeekViewActivity extends AppCompatActivity {
    private GridView weekGrid;
    private TextView tvWeekRange;
    private WeekDayAdapter weekAdapter;
    private Calendar currentWeek;
    private DiaryDBHelper dbHelper;

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
        setContentView(R.layout.activity_week_view);

        dbHelper = new DiaryDBHelper(this);
        initViews();
        setupWeek();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateWeek();
    }

    private void initViews() {
        weekGrid = findViewById(R.id.weekGrid);
        tvWeekRange = findViewById(R.id.tvWeekRange);

        findViewById(R.id.btnPrevWeek).setOnClickListener(v -> {
            currentWeek.add(Calendar.WEEK_OF_YEAR, -1);
            updateWeek();
        });

        findViewById(R.id.btnNextWeek).setOnClickListener(v -> {
            currentWeek.add(Calendar.WEEK_OF_YEAR, 1);
            updateWeek();
        });
    }

    private void setupWeek() {
        long dateMillis = getIntent().getLongExtra("date", System.currentTimeMillis());
        currentWeek = Calendar.getInstance();
        currentWeek.setTimeInMillis(dateMillis);
        currentWeek.set(Calendar.DAY_OF_WEEK, currentWeek.getFirstDayOfWeek());

        updateWeek();
    }

    private void updateWeek() {
        SimpleDateFormat sdf = new SimpleDateFormat("M月d日", Locale.getDefault());
        Calendar endOfWeek = (Calendar) currentWeek.clone();
        endOfWeek.add(Calendar.DAY_OF_WEEK, 6);

        String range = sdf.format(currentWeek.getTime()) + " - " + sdf.format(endOfWeek.getTime());
        tvWeekRange.setText(range);

        // Get days for the week
        List<WeekDayItem> weekDays = new ArrayList<>();
        Calendar day = (Calendar) currentWeek.clone();

        for (int i = 0; i < 7; i++) {
            Date date = day.getTime();
            LunarDate lunarDate = LunarUtils.getLunarDate(day);
            String holiday = LunarUtils.getHoliday(day);
            String solarTerm = LunarUtils.getSolarTerm(day);

            String diary = dbHelper.getDiaryForDate(date);
            List<ScheduleItem> schedules = dbHelper.getSchedulesForDate(date);

            weekDays.add(new WeekDayItem(date, lunarDate, holiday, solarTerm, diary, schedules));
            day.add(Calendar.DAY_OF_MONTH, 1);
        }

        weekAdapter = new WeekDayAdapter(this, weekDays);
        weekGrid.setAdapter(weekAdapter);

        weekGrid.setOnItemClickListener((parent, view, position, id) -> {
            Calendar selectedDay = Calendar.getInstance();
            selectedDay.setTime(weekDays.get(position).getDate());
            showDayDetails(selectedDay);
        });
    }

    private void showDayDetails(Calendar day) {
        Intent intent = new Intent(this, DayDetailActivity.class);
        intent.putExtra("date", day.getTimeInMillis());
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }
}
