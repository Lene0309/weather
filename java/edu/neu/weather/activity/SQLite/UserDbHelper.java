package edu.neu.weather.activity.SQLite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class UserDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "UserProfile.db";
    private static final int DATABASE_VERSION = 1;


    private static final String TABLE_USER = "user";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_PASSWORD = "password";
    private static final String COLUMN_PROFILE_IMAGE = "profile_image";
    private static final String COLUMN_BACKGROUND_IMAGE = "background_image";
    private static final String COLUMN_THEME = "theme"; // 0=light, 1=dark, 2=system

    // 创建用户表的SQL语句
    private static final String CREATE_USER_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_USER + "(" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            COLUMN_NAME + " TEXT," +
            COLUMN_EMAIL + " TEXT," +
            COLUMN_PASSWORD + " TEXT," +
            COLUMN_PROFILE_IMAGE + " TEXT," +
            COLUMN_BACKGROUND_IMAGE + " TEXT," +
            COLUMN_THEME + " INTEGER DEFAULT 2" +
            ")";

    public UserDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_USER_TABLE);

        // 插入默认用户数据
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, "用户名");
        values.put(COLUMN_EMAIL, "user@example.com");
        values.put(COLUMN_PASSWORD, "123456");
        values.put(COLUMN_THEME, 2); // 默认系统主题
        db.insert(TABLE_USER, null, values);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 兼容老表，若无password字段则加
        try { db.execSQL("ALTER TABLE " + TABLE_USER + " ADD COLUMN " + COLUMN_PASSWORD + " TEXT"); } catch (Exception ignore) {}
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        onCreate(db);
    }

    // 获取用户数据
    public User getUser() {
        SQLiteDatabase db = this.getReadableDatabase();
        User user = null;

        Cursor cursor = db.query(TABLE_USER,
                new String[]{COLUMN_ID, COLUMN_NAME, COLUMN_EMAIL,
                        COLUMN_PROFILE_IMAGE, COLUMN_BACKGROUND_IMAGE, COLUMN_THEME},
                null, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            try {
                user = new User();
                int idIndex = cursor.getColumnIndex(COLUMN_ID);
                int nameIndex = cursor.getColumnIndex(COLUMN_NAME);
                int emailIndex = cursor.getColumnIndex(COLUMN_EMAIL);
                int profileImageIndex = cursor.getColumnIndex(COLUMN_PROFILE_IMAGE);
                int backgroundImageIndex = cursor.getColumnIndex(COLUMN_BACKGROUND_IMAGE);
                int themeIndex = cursor.getColumnIndex(COLUMN_THEME);

                // 检查每个列索引是否有效
                if (idIndex != -1) user.setId(cursor.getInt(idIndex));
                if (nameIndex != -1) user.setName(cursor.getString(nameIndex));
                if (emailIndex != -1) user.setEmail(cursor.getString(emailIndex));
                if (profileImageIndex != -1) user.setProfileImagePath(cursor.getString(profileImageIndex));
                if (backgroundImageIndex != -1) user.setBackgroundImagePath(cursor.getString(backgroundImageIndex));
                if (themeIndex != -1) user.setThemePreference(cursor.getInt(themeIndex));
            } finally {
                cursor.close();
            }
        }
        db.close();
        return user;
    }

    // 更新用户
    public int updateUserName(String newName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, newName);
        return db.update(TABLE_USER, values, null, null);
    }

    // 更新用户邮箱
    public int updateUserEmail(String newEmail) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_EMAIL, newEmail);
        return db.update(TABLE_USER, values, null, null);
    }

    // 更新用户头像路径
    public int updateProfileImage(String imagePath) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PROFILE_IMAGE, imagePath);
        return db.update(TABLE_USER, values, null, null);
    }

    // 更新背景图片路径
    public int updateBackgroundImage(String imagePath) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_BACKGROUND_IMAGE, imagePath);
        return db.update(TABLE_USER, values, null, null);
    }

    // 更新主题偏好
    public int updateThemePreference(int theme) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_THEME, theme);
        return db.update(TABLE_USER, values, null, null);
    }

    // 新增：获取所有用户
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USER,
                new String[]{COLUMN_ID, COLUMN_NAME, COLUMN_EMAIL, COLUMN_PROFILE_IMAGE, COLUMN_BACKGROUND_IMAGE, COLUMN_THEME},
                null, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                User user = new User();
                user.setId(cursor.getInt(0));
                user.setName(cursor.getString(1));
                user.setEmail(cursor.getString(2));
                user.setProfileImagePath(cursor.getString(3));
                user.setBackgroundImagePath(cursor.getString(4));
                user.setThemePreference(cursor.getInt(5));
                users.add(user);
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return users;
    }

    // 新增：添加新用户
    public long addUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, user.getName());
        values.put(COLUMN_EMAIL, user.getEmail());
        values.put(COLUMN_PASSWORD, user.getPassword());
        values.put(COLUMN_PROFILE_IMAGE, user.getProfileImagePath());
        values.put(COLUMN_BACKGROUND_IMAGE, user.getBackgroundImagePath());
        values.put(COLUMN_THEME, user.getThemePreference());
        long id = db.insert(TABLE_USER, null, values);
        db.close();
        return id;
    }

    // 新增：设置当前活跃用户（用SharedPreferences存储activeId
    public void setActiveUser(Context context, int userId) {
        context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                .edit().putInt("active_user_id", userId).apply();
    }

    // 新增：获取当前活跃用户
    public User getActiveUser(Context context) {
        int activeId = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                .getInt("active_user_id", 1); // 默认1
        SQLiteDatabase db = this.getReadableDatabase();
        User user = null;
        Cursor cursor = db.query(TABLE_USER,
                new String[]{COLUMN_ID, COLUMN_NAME, COLUMN_EMAIL, COLUMN_PROFILE_IMAGE, COLUMN_BACKGROUND_IMAGE, COLUMN_THEME},
                COLUMN_ID + "=?", new String[]{String.valueOf(activeId)}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            user = new User();
            user.setId(cursor.getInt(0));
            user.setName(cursor.getString(1));
            user.setEmail(cursor.getString(2));
            user.setProfileImagePath(cursor.getString(3));
            user.setBackgroundImagePath(cursor.getString(4));
            user.setThemePreference(cursor.getInt(5));
            cursor.close();
        }
        db.close();
        return user;
    }

    // 登录校验
    public User login(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        User user = null;
        Cursor cursor = db.query(TABLE_USER,
                new String[]{COLUMN_ID, COLUMN_NAME, COLUMN_EMAIL, COLUMN_PASSWORD, COLUMN_PROFILE_IMAGE, COLUMN_BACKGROUND_IMAGE, COLUMN_THEME},
                COLUMN_EMAIL + "=? AND " + COLUMN_PASSWORD + "=?",
                new String[]{email, password}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            user = new User();
            user.setId(cursor.getInt(0));
            user.setName(cursor.getString(1));
            user.setEmail(cursor.getString(2));
            user.setPassword(cursor.getString(3));
            user.setProfileImagePath(cursor.getString(4));
            user.setBackgroundImagePath(cursor.getString(5));
            user.setThemePreference(cursor.getInt(6));
            cursor.close();
        }
        db.close();
        return user;
    }

    // 用户名密码登录
    public User loginByName(String name, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        User user = null;
        Cursor cursor = db.query(TABLE_USER,
                new String[]{COLUMN_ID, COLUMN_NAME, COLUMN_EMAIL, COLUMN_PASSWORD, COLUMN_PROFILE_IMAGE, COLUMN_BACKGROUND_IMAGE, COLUMN_THEME},
                COLUMN_NAME + "=? AND " + COLUMN_PASSWORD + "=?",
                new String[]{name, password}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            user = new User();
            user.setId(cursor.getInt(0));
            user.setName(cursor.getString(1));
            user.setEmail(cursor.getString(2));
            user.setPassword(cursor.getString(3));
            user.setProfileImagePath(cursor.getString(4));
            user.setBackgroundImagePath(cursor.getString(5));
            user.setThemePreference(cursor.getInt(6));
            cursor.close();
        }
        db.close();
        return user;
    }

    // 检查用户名是否已存在
    public boolean isUserExists(String name) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USER,
                new String[]{COLUMN_ID},
                COLUMN_NAME + "=?",
                new String[]{name}, null, null, null);
        boolean exists = cursor != null && cursor.getCount() > 0;
        if (cursor != null) {
            cursor.close();
        }
        db.close();
        return exists;
    }
}
