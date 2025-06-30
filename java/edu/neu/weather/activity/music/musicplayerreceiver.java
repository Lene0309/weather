package edu.neu.weather.activity.music;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;

public class musicplayerreceiver extends BroadcastReceiver {
    private static final String TAG = "MusicPlayerReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "收到广播: " + action);

        if (action == null) return;

        Intent serviceIntent = new Intent(context, musicplayerreceiver.class);

        switch (action) {
            case "com.example.weather.MUSIC_ACTION_PLAY":
                serviceIntent.putExtra("action", "play");
                context.startService(serviceIntent);
                break;

            case "com.example.weather.MUSIC_ACTION_PAUSE":
                serviceIntent.putExtra("action", "pause");
                context.startService(serviceIntent);
                break;

            case "com.example.weather.MUSIC_ACTION_NEXT":
                serviceIntent.putExtra("action", "next");
                context.startService(serviceIntent);
                break;

            case "com.example.weather.MUSIC_ACTION_PREV":
                serviceIntent.putExtra("action", "prev");
                context.startService(serviceIntent);
                break;

            case "com.example.weather.MUSIC_ACTION_STOP":
                serviceIntent.putExtra("action", "stop");
                context.startService(serviceIntent);
                break;

            case "com.example.weather.MUSIC_ACTION_TOGGLE":
                serviceIntent.putExtra("action", "toggle");
                context.startService(serviceIntent);
                break;

            case Intent.ACTION_MEDIA_BUTTON:
                // 处理耳机按钮事件
                KeyEvent keyEvent = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
                if (keyEvent != null && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (keyEvent.getKeyCode()) {
                        case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                            serviceIntent.putExtra("action", "toggle");
                            context.startService(serviceIntent);
                            break;
                        case KeyEvent.KEYCODE_MEDIA_NEXT:
                            serviceIntent.putExtra("action", "next");
                            context.startService(serviceIntent);
                            break;
                        case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                            serviceIntent.putExtra("action", "prev");
                            context.startService(serviceIntent);
                            break;
                    }
                }
                break;
        }
    }
}
