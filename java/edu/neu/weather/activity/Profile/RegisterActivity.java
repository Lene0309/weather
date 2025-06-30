package edu.neu.weather.activity.Profile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import edu.neu.weather.R;
import edu.neu.weather.activity.SQLite.User;
import edu.neu.weather.activity.SQLite.UserDbHelper;

public class RegisterActivity extends AppCompatActivity {
    private EditText etName, etPassword, etEmail;
    private Button btnRegister, btnBack;
    private UserDbHelper dbHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
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
        setContentView(R.layout.activity_register);
        
        dbHelper = new UserDbHelper(this);
        
        etName = findViewById(R.id.et_register_name);
        etPassword = findViewById(R.id.et_register_password);
        etEmail = findViewById(R.id.et_register_email);
        btnRegister = findViewById(R.id.btn_register);
        btnBack = findViewById(R.id.btn_back_to_login);
        
        btnRegister.setOnClickListener(v -> register());
        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void register() {
        String name = etName.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        
        if (name.isEmpty() || password.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "请填写所有信息", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (password.length() < 6) {
            Toast.makeText(this, "密码至少6位", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 检查用户名是否已存在
        if (dbHelper.isUserExists(name)) {
            Toast.makeText(this, "用户名已存在", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 创建新用户
        User newUser = new User();
        newUser.setName(name);
        newUser.setPassword(password);
        newUser.setEmail(email);
        
        long userId = dbHelper.addUser(newUser);
        if (userId > 0) {
            // 设置为当前活跃用户
            dbHelper.setActiveUser(this, (int) userId);
            
            Toast.makeText(this, "注册成功", Toast.LENGTH_SHORT).show();
            
            // 跳转到主页面
            startActivity(new Intent(this, edu.neu.weather.activity.main.class));
            finish();
        } else {
            Toast.makeText(this, "注册失败，请重试", Toast.LENGTH_SHORT).show();
        }
    }
} 
