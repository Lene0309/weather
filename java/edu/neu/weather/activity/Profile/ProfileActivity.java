package edu.neu.weather.activity.Profile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import edu.neu.weather.R;
import edu.neu.weather.activity.SQLite.User;
import edu.neu.weather.activity.SQLite.UserDbHelper;

public class ProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PICK_BACKGROUND_REQUEST = 2;

    private CircleImageView profileImage;
    private ImageView profileBackground;
    private TextView profileName, profileEmail;
    private LinearLayout accountSettingsLayout, themeSettingsLayout, notificationSettingsLayout;
    private LinearLayout aboutAppLayout, feedbackLayout, logoutLayout;

    private UserDbHelper dbHelper;
    private User currentUser;

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
        dbHelper = new UserDbHelper(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.personal);
        currentUser = dbHelper.getActiveUser(this);
        initializeViews();
        setupClickListeners();
        loadUserData();
    }

    private void applyTheme() {
        User user = dbHelper.getActiveUser(this);
        switch (user.getThemePreference()) {
            case 0:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case 1:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case 2:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }

    private void initializeViews() {
        profileImage = findViewById(R.id.profile_image);
        profileBackground = findViewById(R.id.profile_background);
        profileName = findViewById(R.id.profile_name);
        profileEmail = findViewById(R.id.profile_email);

        accountSettingsLayout = findViewById(R.id.account_settings_layout);
        themeSettingsLayout = findViewById(R.id.theme_settings_layout);
        notificationSettingsLayout = findViewById(R.id.notification_settings_layout);

        aboutAppLayout = findViewById(R.id.about_app_layout);
        feedbackLayout = findViewById(R.id.feedback_layout);
        logoutLayout = findViewById(R.id.logout_layout);
    }

    private void setupClickListeners() {
        // 头像点击事件
        profileImage.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "选择头像"), PICK_IMAGE_REQUEST);
        });

        // 背景点击事件
        profileBackground.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "选择背景图片"), PICK_BACKGROUND_REQUEST);
        });

        // 账号设置
        accountSettingsLayout.setOnClickListener(v -> showAccountSettingsDialog());

        // 主题设置
        themeSettingsLayout.setOnClickListener(v -> showThemeSelectionDialog());

        // 通知设置
        notificationSettingsLayout.setOnClickListener(v -> {
            // Toast������ɾ��
        });

        // 关于应用
        aboutAppLayout.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(ProfileActivity.this)
                    .setTitle("关于应用")
                    .setMessage("版本: 1.0.0\n开发团队： 您的团队")
                    .setPositiveButton("确定", null)
                    .show();
        });

        // 意见反馈
        feedbackLayout.setOnClickListener(v -> {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                    "mailto", "support@yourapp.com", null));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "应用反馈");
            startActivity(Intent.createChooser(emailIntent, "发送反馈"));
        });

        // 退出登录
        logoutLayout.setOnClickListener(v -> showLogoutConfirmationDialog());
    }

    private void loadUserData() {
        if (currentUser != null) {
            profileName.setText(currentUser.getName());
            profileEmail.setText(currentUser.getEmail());

            // 加载头像
            if (currentUser.getProfileImagePath() != null && !currentUser.getProfileImagePath().isEmpty()) {
                Bitmap profileBitmap = BitmapFactory.decodeFile(currentUser.getProfileImagePath());
                if (profileBitmap != null) {
                    profileImage.setImageBitmap(profileBitmap);
                }
            }

            // 加载背景
            if (currentUser.getBackgroundImagePath() != null && !currentUser.getBackgroundImagePath().isEmpty()) {
                Bitmap backgroundBitmap = BitmapFactory.decodeFile(currentUser.getBackgroundImagePath());
                if (backgroundBitmap != null) {
                    profileBackground.setImageBitmap(backgroundBitmap);
                }
            }
        }
    }

    private void showAccountSettingsDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_account_settings, null);
        TextView etName = dialogView.findViewById(R.id.et_name);
        TextView etEmail = dialogView.findViewById(R.id.et_email);

        etName.setText(currentUser.getName());
        etEmail.setText(currentUser.getEmail());

        // 新增：账号切换和新建账号按钮
        dialogView.findViewById(R.id.et_email).setOnLongClickListener(v -> {
            showAccountSwitchDialog();
            return true;
        });

        new MaterialAlertDialogBuilder(this)
                .setTitle("账号设置")
                .setView(dialogView)
                .setPositiveButton("保存", (dialog, which) -> {
                    String newName = etName.getText().toString();
                    String newEmail = etEmail.getText().toString();

                    if (!newName.isEmpty() && !newEmail.isEmpty()) {
                        dbHelper.updateUserName(newName);
                        dbHelper.updateUserEmail(newEmail);
                        currentUser.setName(newName);
                        currentUser.setEmail(newEmail);
                        profileName.setText(newName);
                        profileEmail.setText(newEmail);

                    } else {

                    }
                })
                .setNeutralButton("切换账号", (dialog, which) -> showAccountSwitchDialog())
                .setNegativeButton("取消", null)
                .show();
    }

private void showAccountSwitchDialog() {
        List<User> users = dbHelper.getAllUsers();
        String[] userNames = new String[users.size() + 1];
        for (int i = 0; i < users.size(); i++) {
            userNames[i] = users.get(i).getName() + " (" + users.get(i).getEmail() + ")";
        }
        userNames[users.size()] = "新建账号";
        new MaterialAlertDialogBuilder(this)
                .setTitle("切换账号")
                .setItems(userNames, (dialog, which) -> {
                    if (which == users.size()) {
                        showAddAccountDialog();
                    } else {
                        dbHelper.setActiveUser(this, users.get(which).getId());
                        currentUser = dbHelper.getActiveUser(this);
                        loadUserData();
                        applyTheme();
                        recreate();
                        Toast.makeText(this, "账号切换成功", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

 private void showAddAccountDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_account_settings, null);
        TextView etName = dialogView.findViewById(R.id.et_name);
        TextView etEmail = dialogView.findViewById(R.id.et_email);
        new MaterialAlertDialogBuilder(this)
                .setTitle("新建账号")
                .setView(dialogView)
                .setPositiveButton("创建", (dialog, which) -> {
                    String name = etName.getText().toString();
                    String email = etEmail.getText().toString();
                    if (!name.isEmpty() && !email.isEmpty()) {
                        User newUser = new User();
                        newUser.setName(name);
                        newUser.setEmail(email);
                        newUser.setThemePreference(2); // 默认跟随系统
                        long newId = dbHelper.addUser(newUser);
                        dbHelper.setActiveUser(this, (int)newId);
                        currentUser = dbHelper.getActiveUser(this);
                        loadUserData();
                        applyTheme();
                        recreate();
                        Toast.makeText(this, "账号创建成功", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "请填写完整信息", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void showThemeSelectionDialog() {
        String[] themes = {"深色", "浅色", "跟随系统"};
        int checkedItem = currentUser.getThemePreference();

        new MaterialAlertDialogBuilder(this)
                .setTitle("选择主题")
                .setSingleChoiceItems(themes, checkedItem, (dialog, which) -> {
                    dbHelper.updateThemePreference(which);
                    currentUser.setThemePreference(which);
                    SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
                    prefs.edit().putInt("theme_mode", which).apply();
                    Intent intent = new Intent(ProfileActivity.this, edu.neu.weather.activity.main.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                })
                .setPositiveButton("确定", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showLogoutConfirmationDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("退出登录")
                .setMessage("确定要退出当前账号吗？")
                .setPositiveButton("退出", (dialog, which) -> {
                    // 清除当前活跃用户
                    SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
                    prefs.edit().remove("active_user_id").apply();
                    
                    // 跳转到登录页面
                    Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                    
                    Toast.makeText(this, "已退出登录", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

  String imagePath = saveImageToInternalStorage(bitmap,
                        requestCode == PICK_IMAGE_REQUEST ? "profile.jpg" : "background.jpg");

                if (requestCode == PICK_IMAGE_REQUEST) {
                    profileImage.setImageBitmap(bitmap);
                    dbHelper.updateProfileImage(imagePath);
                    currentUser.setProfileImagePath(imagePath);

                } else if (requestCode == PICK_BACKGROUND_REQUEST) {
                    profileBackground.setImageBitmap(bitmap);
                    dbHelper.updateBackgroundImage(imagePath);

                }

            } catch (IOException e) {
                e.printStackTrace();

            }
        }
    }

    private String saveImageToInternalStorage(Bitmap bitmap, String filename) {
        File directory = getFilesDir();
        File imageFile = new File(directory, filename);

        try (FileOutputStream fos = new FileOutputStream(imageFile)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            return imageFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }
}
