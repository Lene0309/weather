package edu.neu.weather.activity.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ReminderReceiver extends BroadcastReceiver {
    private static final String TAG = "ReminderReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            String title = intent.getStringExtra("title");
            String content = intent.getStringExtra("content");
            long id = intent.getLongExtra("id", -1);

            if (title == null || content == null) {
                Log.w(TAG, "收到提醒广播但缺少必要参数");
                return;
            }

            Log.d(TAG, "显示日程提醒: " + title);
            NotificationUtils.showNotification(context, title, content);
            
        } catch (Exception e) {
            Log.e(TAG, "处理提醒广播时出错", e);
        }
    }
}
