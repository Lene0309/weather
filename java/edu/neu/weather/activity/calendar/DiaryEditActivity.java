package edu.neu.weather.activity.calendar;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Date;
import edu.neu.weather.R;
import edu.neu.weather.activity.SQLite.DiaryDBHelper;

public class DiaryEditActivity extends AppCompatActivity {
    private EditText etContent;
    private Date currentDate;
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
        setContentView(R.layout.activity_diary_edit);

        dbHelper = new DiaryDBHelper(this);
        etContent = findViewById(R.id.etContent);

        long dateMillis = getIntent().getLongExtra("date", System.currentTimeMillis());
        currentDate = new Date(dateMillis);

        String existingContent = getIntent().getStringExtra("diaryContent");
        if (existingContent != null && !existingContent.equals("暂无日记")) {
            etContent.setText(existingContent);
        }

        Button btnSave = findViewById(R.id.btnSave);
        btnSave.setOnClickListener(v -> saveDiary());
    }

    private void saveDiary() {
        String content = etContent.getText().toString().trim();

        if (content.isEmpty()) {

            return;
        }

        long result = dbHelper.addOrUpdateDiary(currentDate, content);

        if (result != -1) {

            Intent resultIntent = new Intent();
            resultIntent.putExtra("diaryContent", content);
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
        } else {

        }
    }

    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }
}
