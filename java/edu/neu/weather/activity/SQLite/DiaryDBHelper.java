package edu.neu.weather.activity.SQLite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import edu.neu.weather.activity.calendar.DiaryItem;
import edu.neu.weather.activity.calendar.ScheduleItem;

public class DiaryDBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "diary.db";
    private static final int DATABASE_VERSION = 1;

    private static final String COLUMN_DIARY_ID = "_id";
    private static final String COLUMN_DIARY_TITLE = "title";
    private static final String COLUMN_DIARY_CONTENT = "content";
    private static final String COLUMN_DIARY_DATE = "date";


    private static final String COLUMN_SCHEDULE_ID = "_id";
    private static final String COLUMN_SCHEDULE_TITLE = "title";
    private static final String COLUMN_SCHEDULE_CONTENT = "content";
    private static final String COLUMN_SCHEDULE_DATE = "date";
    private static final String COLUMN_SCHEDULE_HAS_REMINDER = "has_reminder";
    private static final String COLUMN_SCHEDULE_REMINDER_TIME = "reminder_time";
    private static final String TABLE_DIARY = "diary";
    private static final String TABLE_SCHEDULE = "schedule";

    public DiaryDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
     String CREATE_DIARY_TABLE = "CREATE TABLE " + TABLE_DIARY + "("
                + COLUMN_DIARY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_DIARY_TITLE + " TEXT,"
                + COLUMN_DIARY_CONTENT + " TEXT,"
                + COLUMN_DIARY_DATE + " INTEGER)";
        db.execSQL(CREATE_DIARY_TABLE);

    String CREATE_SCHEDULE_TABLE = "CREATE TABLE " + TABLE_SCHEDULE + "("
                + COLUMN_SCHEDULE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_SCHEDULE_TITLE + " TEXT,"
                + COLUMN_SCHEDULE_CONTENT + " TEXT,"
                + COLUMN_SCHEDULE_DATE + " INTEGER,"
                + COLUMN_SCHEDULE_HAS_REMINDER + " INTEGER,"
                + COLUMN_SCHEDULE_REMINDER_TIME + " INTEGER)";
        db.execSQL(CREATE_SCHEDULE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DIARY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SCHEDULE);
        onCreate(db);
    }

  public Date getZeroTimeDate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    // 日记相关操作
    public long addDiary(DiaryItem diary) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_DIARY_TITLE, diary.getTitle());
        values.put(COLUMN_DIARY_CONTENT, diary.getContent());

        values.put(COLUMN_DIARY_DATE, getZeroTimeDate(diary.getDate()).getTime());
        long id = db.insert(TABLE_DIARY, null, values);
        db.close();
        return id;
    }

    // 在DiaryDBHelper类中添加这个方法
    public DiaryItem getDiaryForDateAsItem(Date date) {
        SQLiteDatabase db = this.getReadableDatabase();
        Date zeroDate = getZeroTimeDate(date);
        Cursor cursor = db.query(TABLE_DIARY,
                new String[]{COLUMN_DIARY_ID, COLUMN_DIARY_TITLE, COLUMN_DIARY_CONTENT, COLUMN_DIARY_DATE},
                COLUMN_DIARY_DATE + "=?",
                new String[]{String.valueOf(zeroDate.getTime())}, null, null, null, "1");

        if (cursor != null && cursor.moveToFirst()) {
            DiaryItem diary = new DiaryItem(
                    cursor.getLong(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    new Date(cursor.getLong(3)));
            cursor.close();
            return diary;
        }
        return null;
    }
    public long addOrUpdateDiary(Date date, String content) {
        SQLiteDatabase db = this.getWritableDatabase();
        Date zeroDate = getZeroTimeDate(date);
        // First check if a diary exists for this date
        String existingDiary = getDiaryForDate(zeroDate);
        ContentValues values = new ContentValues();
        values.put(COLUMN_DIARY_TITLE, "日记"); // Default title
        values.put(COLUMN_DIARY_CONTENT, content);
        values.put(COLUMN_DIARY_DATE, zeroDate.getTime());
        if (existingDiary != null) {
            // Update existing diary
            return db.update(TABLE_DIARY, values,
                    COLUMN_DIARY_DATE + "=?",
                    new String[]{String.valueOf(zeroDate.getTime())});
        } else {
            // Insert new diary
            return db.insert(TABLE_DIARY, null, values);
        }
    }
    public DiaryItem getDiary(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_DIARY,
                new String[]{COLUMN_DIARY_ID, COLUMN_DIARY_TITLE, COLUMN_DIARY_CONTENT, COLUMN_DIARY_DATE},
                COLUMN_DIARY_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);

        if (cursor != null)
            cursor.moveToFirst();

        DiaryItem diary = new DiaryItem(
                cursor.getLong(0),
                cursor.getString(1),
                cursor.getString(2),
                new Date(cursor.getLong(3)));

        cursor.close();
        return diary;
    }

    public String getDiaryForDate(Date date) {
        SQLiteDatabase db = this.getReadableDatabase();
        Date zeroDate = getZeroTimeDate(date);
        Cursor cursor = db.query(TABLE_DIARY,
                new String[]{COLUMN_DIARY_CONTENT},
                COLUMN_DIARY_DATE + "=?",
                new String[]{String.valueOf(zeroDate.getTime())}, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            String content = cursor.getString(0);
            cursor.close();
            return content;
        }
        return null;
    }

    public int updateDiary(DiaryItem diary) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_DIARY_TITLE, diary.getTitle());
        values.put(COLUMN_DIARY_CONTENT, diary.getContent());
        values.put(COLUMN_DIARY_DATE, diary.getDate().getTime());

        return db.update(TABLE_DIARY, values, COLUMN_DIARY_ID + " = ?",
                new String[]{String.valueOf(diary.getId())});
    }

    public void deleteDiary(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_DIARY, COLUMN_DIARY_ID + " = ?",
                new String[]{String.valueOf(id)});
        db.close();
    }

    // 日程相关操作
    public long addSchedule(ScheduleItem schedule) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_SCHEDULE_TITLE, schedule.getTitle());
        values.put(COLUMN_SCHEDULE_CONTENT, schedule.getContent());

        values.put(COLUMN_SCHEDULE_DATE, getZeroTimeDate(schedule.getDate()).getTime());
        values.put(COLUMN_SCHEDULE_HAS_REMINDER, schedule.hasReminder() ? 1 : 0);
        if (schedule.getReminderTime() != null) {
            values.put(COLUMN_SCHEDULE_REMINDER_TIME, schedule.getReminderTime().getTime());
        }
        long id = db.insert(TABLE_SCHEDULE, null, values);
        db.close();
        return id;
    }

    public ScheduleItem getSchedule(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_SCHEDULE,
                new String[]{COLUMN_SCHEDULE_ID, COLUMN_SCHEDULE_TITLE, COLUMN_SCHEDULE_CONTENT,
                        COLUMN_SCHEDULE_DATE, COLUMN_SCHEDULE_HAS_REMINDER, COLUMN_SCHEDULE_REMINDER_TIME},
                COLUMN_SCHEDULE_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);

        if (cursor != null)
            cursor.moveToFirst();

        ScheduleItem schedule = new ScheduleItem(
                cursor.getLong(0),
                cursor.getString(1),
                cursor.getString(2),
                new Date(cursor.getLong(3)),
                cursor.getInt(4) == 1,
                cursor.isNull(5) ? null : new Date(cursor.getLong(5)));

        cursor.close();
        return schedule;
    }

    public List<ScheduleItem> getSchedulesForDate(Date date) {
        List<ScheduleItem> schedules = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Date zeroDate = getZeroTimeDate(date);
        Cursor cursor = db.query(TABLE_SCHEDULE,
                new String[]{COLUMN_SCHEDULE_ID, COLUMN_SCHEDULE_TITLE, COLUMN_SCHEDULE_CONTENT,
                        COLUMN_SCHEDULE_HAS_REMINDER, COLUMN_SCHEDULE_REMINDER_TIME},
                COLUMN_SCHEDULE_DATE + "=?",
                new String[]{String.valueOf(zeroDate.getTime())}, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                ScheduleItem schedule = new ScheduleItem(
                        cursor.getLong(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        zeroDate,
                        cursor.getInt(3) == 1,
                        cursor.isNull(4) ? null : new Date(cursor.getLong(4)));
                schedules.add(schedule);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return schedules;
    }

    public int updateSchedule(ScheduleItem schedule) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_SCHEDULE_TITLE, schedule.getTitle());
        values.put(COLUMN_SCHEDULE_CONTENT, schedule.getContent());
        // 存储�?点时间戳
        values.put(COLUMN_SCHEDULE_DATE, getZeroTimeDate(schedule.getDate()).getTime());
        values.put(COLUMN_SCHEDULE_HAS_REMINDER, schedule.hasReminder() ? 1 : 0);
        if (schedule.getReminderTime() != null) {
            values.put(COLUMN_SCHEDULE_REMINDER_TIME, schedule.getReminderTime().getTime());
        }
        return db.update(TABLE_SCHEDULE, values, COLUMN_SCHEDULE_ID + " = ?",
                new String[]{String.valueOf(schedule.getId())});
    }

    public void deleteSchedule(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_SCHEDULE, COLUMN_SCHEDULE_ID + " = ?",
                new String[]{String.valueOf(id)});
        db.close();
    }

   public List<DiaryItem> getDiariesForDate(Date date) {
        List<DiaryItem> diaries = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Date zeroDate = getZeroTimeDate(date);
        Cursor cursor = db.query(TABLE_DIARY,
                new String[]{COLUMN_DIARY_ID, COLUMN_DIARY_TITLE, COLUMN_DIARY_CONTENT, COLUMN_DIARY_DATE},
                COLUMN_DIARY_DATE + "=?",
                new String[]{String.valueOf(zeroDate.getTime())}, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                DiaryItem diary = new DiaryItem(
                        cursor.getLong(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        new Date(cursor.getLong(3)));
                diaries.add(diary);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return diaries;
    }
}
