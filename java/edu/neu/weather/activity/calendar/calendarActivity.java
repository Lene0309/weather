package edu.neu.weather.activity.calendar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import edu.neu.weather.R;
import edu.neu.weather.activity.SQLite.DiaryDBHelper;
import edu.neu.weather.activity.utils.LunarDate;
import edu.neu.weather.activity.utils.LunarUtils;

public class calendarActivity extends AppCompatActivity {
    private static final int REQUEST_ADD = 1001;
    private GridView calendarGrid;
    private TextView tvMonthYear, tvLunarInfo;
    private CalendarAdapter calendarAdapter;
    private Calendar currentCalendar = Calendar.getInstance();
    private Calendar today = Calendar.getInstance();
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
        setContentView(R.layout.activity_calender);

        dbHelper = new DiaryDBHelper(this);
        initViews();
        setupCalendar();
        setupListeners();
    }

    private void initViews() {
        calendarGrid = findViewById(R.id.calendarGrid);
        tvMonthYear = findViewById(R.id.tvMonthYear);
        tvLunarInfo = findViewById(R.id.tvLunarInfo);
    }

    private void setupCalendar() {
        updateMonthYearTitle();
        calendarAdapter = new CalendarAdapter(this, currentCalendar, today);
        calendarGrid.setAdapter(calendarAdapter);
        updateLunarInfo();
    }

    private void updateMonthYearTitle() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年M月", Locale.getDefault());
        tvMonthYear.setText(sdf.format(currentCalendar.getTime()));
    }

    private void updateLunarInfo() {
        LunarDate lunarToday = LunarUtils.getLunarDate(today);
        String solarTerm = LunarUtils.getSolarTerm(today);
        String info = String.format("今天是农历 | %s月| %s",
                lunarToday.getDisplayName(), lunarToday.getYearName(), solarTerm);
        tvLunarInfo.setText(info);
    }

    private void setupListeners() {
        // 月份切换
        findViewById(R.id.btnPrevMonth).setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, -1);
            updateCalendar();
        });

        findViewById(R.id.btnNextMonth).setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, 1);
            updateCalendar();
        });

        // 日期点击
        calendarGrid.setOnItemClickListener((parent, view, position, id) -> {
            Calendar day = calendarAdapter.getDayAtPosition(position);
            if (day != null) {
                Intent intent = new Intent(this, DayDetailActivity.class);
                intent.putExtra("date", day.getTimeInMillis());
                startActivity(intent);
            }
        });

        // 视图切换
        findViewById(R.id.btnDayView).setOnClickListener(v -> {
            Intent intent = new Intent(this, DayDetailActivity.class);
            intent.putExtra("date", currentCalendar.getTimeInMillis());
            startActivity(intent);
        });

        findViewById(R.id.btnWeekView).setOnClickListener(v -> {
            Intent intent = new Intent(this, WeekViewActivity.class);
            intent.putExtra("date", currentCalendar.getTimeInMillis());
            startActivity(intent);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ADD && resultCode == RESULT_OK) {
            calendarAdapter.updateCalendar(currentCalendar); // 刷新日历
        }
    }

    private void updateCalendar() {
        updateMonthYearTitle();
        calendarAdapter.updateCalendar(currentCalendar);
        updateLunarInfo();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (calendarAdapter != null) {
            calendarAdapter.updateCalendar(currentCalendar);
        }
    }

    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }
}
