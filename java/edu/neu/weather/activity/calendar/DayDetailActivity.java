package edu.neu.weather.activity.calendar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import edu.neu.weather.R;
import edu.neu.weather.activity.SQLite.DiaryDBHelper;
import edu.neu.weather.activity.utils.LunarDate;
import edu.neu.weather.activity.utils.LunarUtils;

public class DayDetailActivity extends AppCompatActivity {
    private static final int REQUEST_EDIT = 2001;

    private TextView tvDateTitle, tvLunarInfo;
    private ListView lvItems;
    private Button btnAdd;
    private DiaryDBHelper dbHelper;
    private Calendar selectedDay;
    private DayDetailAdapter adapter;
    private List<Object> items = new ArrayList<>();

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
        setContentView(R.layout.activity_day_detail);

        // 初始化工具栏
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // 初始化数据库
        dbHelper = new DiaryDBHelper(this);
        initViews();

   long dateMillis = getIntent().getLongExtra("date", -1);
        if (dateMillis == -1) {

            finish();
            return;
        }

        selectedDay = Calendar.getInstance();
        selectedDay.setTimeInMillis(dateMillis);

        setupDateInfo();
        loadItems();
        setupListeners();
    }

    private void initViews() {
        tvDateTitle = findViewById(R.id.tvDateTitle);
        tvLunarInfo = findViewById(R.id.tvLunarInfo);
        lvItems = findViewById(R.id.lvItems);
        btnAdd = findViewById(R.id.btnAdd);
    }

    private void setupDateInfo() {
        // 公历日期
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年M月d日EEEE", Locale.getDefault());
        tvDateTitle.setText(dateFormat.format(selectedDay.getTime()));

        // 农历信息
        LunarDate lunarDate = LunarUtils.getLunarDate(selectedDay);
        String holiday = LunarUtils.getHoliday(selectedDay);
        String solarTerm = LunarUtils.getSolarTerm(selectedDay);

        String lunarInfo = "农历" + lunarDate.getDisplayName() + " | " + lunarDate.getYearName();
        if (!holiday.isEmpty()) lunarInfo += " | " + holiday;
        if (!solarTerm.isEmpty()) lunarInfo += " | " + solarTerm;

        tvLunarInfo.setText(lunarInfo);
    }

    private void loadItems() {
        items.clear();

       items.addAll(dbHelper.getDiariesForDate(selectedDay.getTime()));

       items.addAll(dbHelper.getSchedulesForDate(selectedDay.getTime()));

          if (adapter == null) {
            adapter = new DayDetailAdapter(this, items);
            lvItems.setAdapter(adapter);
        } else {
            adapter.updateData(items);
        }
    }

    private void setupListeners() {
        // 添加按钮（关键修复：添加类型选择对话框）
        btnAdd.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle("添加内容")
                .setItems(new String[]{"日记", "日程"}, (dialog, which) -> {
                    Intent intent = new Intent(this, AddEditActivity.class);
                    intent.putExtra("date", selectedDay.getTimeInMillis());
                    intent.putExtra("type", which == 0 ? "diary" : "schedule");
                    startActivityForResult(intent, REQUEST_EDIT);
                })
                .show());

     lvItems.setOnItemClickListener((parent, view, position, id) -> {
            Object item = items.get(position);
            Intent intent = new Intent(this, AddEditActivity.class);

            if (item instanceof DiaryItem) {
                intent.putExtra("id", ((DiaryItem)item).getId());
                intent.putExtra("type", "diary");
            } else if (item instanceof ScheduleItem) {
                intent.putExtra("id", ((ScheduleItem)item).getId());
                intent.putExtra("type", "schedule");
            }

            intent.putExtra("date", selectedDay.getTimeInMillis());
            startActivityForResult(intent, REQUEST_EDIT);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_EDIT && resultCode == RESULT_OK) {
            loadItems(); // 刷新数据
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_day_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }
}
