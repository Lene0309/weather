package edu.neu.weather.activity.calendar;

import android.Manifest;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import edu.neu.weather.R;
import edu.neu.weather.activity.SQLite.DiaryDBHelper;
import edu.neu.weather.activity.utils.NotificationUtils;

public class AddEditActivity extends AppCompatActivity {
    private static final String TAG = "AddEditActivity";

    private EditText etTitle, etContent, etReminderTime;
    private CheckBox cbReminder;
    private Button btnSave;
    private DiaryDBHelper dbHelper;
    private long itemId = -1;
    private String itemType;
    private Calendar reminderCalendar;
    private Date selectedDate;

    // 权限请求启动器
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Log.d(TAG, "通知权限已授予");
                } else {
                    Log.w(TAG, "通知权限被拒绝");
                    Toast.makeText(this, "需要通知权限才能设置提醒", Toast.LENGTH_LONG).show();
                }
            });

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
        setContentView(R.layout.activity_add_edit);

        // 检查通知权限
        checkNotificationPermission();

        // 初始化数据库
        dbHelper = new DiaryDBHelper(this);

        initViews();
        setupToolbar();
        processIntentData();
        setupReminder();

        // 设置保存按钮点击事件
        btnSave.setOnClickListener(v -> saveItem());

        // 添加测试通知按钮（仅用于调试）
        Button btnTestNotification = findViewById(R.id.btnTestNotification);
        if (btnTestNotification != null) {
            btnTestNotification.setVisibility(View.VISIBLE); // 显示测试按钮
            btnTestNotification.setOnClickListener(v -> testNotification());
        }

        // 如果是编辑模式，加载现有数据
        if (itemId != -1) {
            loadItemData();
        } else if ("schedule".equals(itemType)) {
            // 新建日程时默认开启提醒
            cbReminder.setChecked(true);
            updateReminderTimeDisplay();
        }
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    private void initViews() {
        etTitle = findViewById(R.id.etTitle);
        etContent = findViewById(R.id.etContent);
        cbReminder = findViewById(R.id.cbReminder);
        etReminderTime = findViewById(R.id.etReminderTime);
        btnSave = findViewById(R.id.btnSave);

        // 设置提醒复选框监听
        cbReminder.setOnCheckedChangeListener((buttonView, isChecked) -> {
            findViewById(R.id.reminderLayout).setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        // 设置提醒时间点击事件
        etReminderTime.setOnClickListener(v -> showTimePicker());
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // 设置返回按钮
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void processIntentData() {
        Intent intent = getIntent();
        itemId = intent.getLongExtra("id", -1);
        itemType = intent.getStringExtra("type");

        // 参数验证
        if (itemType == null) {
            Toast.makeText(this, "参数错误", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        long dateMillis = intent.getLongExtra("date", -1);
        if (dateMillis == -1) {
            Toast.makeText(this, "日期参数错误", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        selectedDate = new Date(dateMillis);

        // 根据模式设置标题
        updateActionBarTitle();
    }

    private void updateActionBarTitle() {
        if (getSupportActionBar() != null) {
            String title = (itemId == -1 ? "添加" : "编辑") +
                    ("diary".equals(itemType) ? "日记" : "日程");
            getSupportActionBar().setTitle(title);
        }
    }

    private void setupReminder() {
        reminderCalendar = Calendar.getInstance();
        reminderCalendar.setTime(selectedDate);
        reminderCalendar.set(Calendar.HOUR_OF_DAY, 8);
        reminderCalendar.set(Calendar.MINUTE, 0);
    }

    private void showTimePicker() {
        new TimePickerDialog(this, this::onTimeSet,
                reminderCalendar.get(Calendar.HOUR_OF_DAY),
                reminderCalendar.get(Calendar.MINUTE), true)
                .show();
    }

    private void onTimeSet(TimePicker view, int hour, int minute) {
        reminderCalendar.set(Calendar.HOUR_OF_DAY, hour);
        reminderCalendar.set(Calendar.MINUTE, minute);
        updateReminderTimeDisplay();
    }

    private void updateReminderTimeDisplay() {
        etReminderTime.setText(new SimpleDateFormat("HH:mm", Locale.getDefault())
                .format(reminderCalendar.getTime()));
    }

    private void loadItemData() {
        try {
            if ("diary".equals(itemType)) {
                DiaryItem diary = dbHelper.getDiary(itemId);
                if (diary != null) {
                    etTitle.setText(diary.getTitle());
                    etContent.setText(diary.getContent());
                }
            } else {
                ScheduleItem schedule = dbHelper.getSchedule(itemId);
                if (schedule != null) {
                    etTitle.setText(schedule.getTitle());
                    etContent.setText(schedule.getContent());
                    cbReminder.setChecked(schedule.hasReminder());
                    if (schedule.hasReminder() && schedule.getReminderTime() != null) {
                        reminderCalendar.setTime(schedule.getReminderTime());
                        updateReminderTimeDisplay();
                    }
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "加载数据失败", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "加载数据错误", e);
        }
    }

    private void saveItem() {
        // 验证输入
        String title = etTitle.getText().toString().trim();
        if (title.isEmpty()) {
            etTitle.setError("请输入标题");
            etTitle.requestFocus();
            return;
        }

        String content = etContent.getText().toString().trim();
        if (content.isEmpty()) {
            etContent.setError("请输入内容");
            etContent.requestFocus();
            return;
        }

        try {
            if ("diary".equals(itemType)) {
                saveDiary(title, content);
            } else {
                saveSchedule(title, content);
            }

            setResult(RESULT_OK);
            finish();
        } catch (Exception e) {
            Toast.makeText(this, "保存失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "保存错误", e);
        }
    }

    private void saveDiary(String title, String content) {
        DiaryItem diary = new DiaryItem(itemId, title, content, selectedDate);

        if (itemId == -1) {
            long newId = dbHelper.addDiary(diary);
            if (newId != -1) {
                Toast.makeText(this, "日记保存成功", Toast.LENGTH_SHORT).show();
            } else {
                throw new RuntimeException("添加日记失败");
            }
        } else {
            if (dbHelper.updateDiary(diary) > 0) {
                Toast.makeText(this, "日记更新成功", Toast.LENGTH_SHORT).show();
            } else {
                throw new RuntimeException("更新日记失败");
            }
        }
    }

    private void saveSchedule(String title, String content) {
        boolean hasReminder = cbReminder.isChecked();
        Date reminderTime = hasReminder ? reminderCalendar.getTime() : null;
        ScheduleItem schedule = new ScheduleItem(itemId, title, content, selectedDate, hasReminder, reminderTime);

        long newId = -1;
        if (itemId == -1) {
            // 新增日程
            newId = dbHelper.addSchedule(schedule);
            if (newId != -1) {
                schedule.setId(newId);
                // 设置提醒
                if (hasReminder && reminderTime != null) {
                    NotificationUtils.setReminder(this, schedule);
                    Log.d(TAG, "设置日程提醒: " + title + " 时间: " + reminderTime);
                }
                Toast.makeText(this, "日程保存成功", Toast.LENGTH_SHORT).show();
            } else {
                throw new RuntimeException("添加日程失败");
            }
        } else {
            // 编辑日程 - 先取消旧提醒
            if (itemId != -1) {
                NotificationUtils.cancelReminder(this, itemId);
                Log.d(TAG, "取消旧提醒: " + itemId);
            }
            
            if (dbHelper.updateSchedule(schedule) > 0) {
                // 设置新提醒
                if (hasReminder && reminderTime != null) {
                    NotificationUtils.setReminder(this, schedule);
                    Log.d(TAG, "更新日程提醒: " + title + " 时间: " + reminderTime);
                }
                Toast.makeText(this, "日程更新成功", Toast.LENGTH_SHORT).show();
            } else {
                throw new RuntimeException("更新日程失败");
            }
        }
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }

    // 测试通知功能的方法（仅用于调试）
    private void testNotification() {
        try {
            // 创建一个测试日程，5秒后提醒
            Calendar testCalendar = Calendar.getInstance();
            testCalendar.add(Calendar.SECOND, 5);
            
            ScheduleItem testSchedule = new ScheduleItem(
                999, // 测试ID
                "测试提醒",
                "这是一个测试提醒，5秒后应该弹出通知",
                new Date(),
                true,
                testCalendar.getTime()
            );
            
            NotificationUtils.setReminder(this, testSchedule);
            Toast.makeText(this, "测试提醒已设置，5秒后查看通知", Toast.LENGTH_LONG).show();
            Log.d(TAG, "测试提醒设置成功，时间: " + testCalendar.getTime());
            
        } catch (Exception e) {
            Log.e(TAG, "测试通知失败", e);
            Toast.makeText(this, "测试通知失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
