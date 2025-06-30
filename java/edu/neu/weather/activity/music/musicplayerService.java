package edu.neu.weather.activity.music;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.media.session.MediaButtonReceiver;

import edu.neu.weather.R;

public class musicplayerService extends Service {
    private static final String TAG = "MusicPlayerService";
    private static final String CHANNEL_ID = "music_channel";
    private static final int NOTIFICATION_ID = 101;
    public static final String ACTION_PLAY = "edu.neu.weather.ACTION_PLAY";
    public static final String ACTION_PAUSE = "edu.neu.weather.ACTION_PAUSE";
    public static final String ACTION_NEXT = "edu.neu.weather.ACTION_NEXT";
    public static final String ACTION_PREV = "edu.neu.weather.ACTION_PREV";
    public static final String ACTION_STOP = "edu.neu.weather.ACTION_STOP";

    private MediaPlayer mediaPlayer;
    private final IBinder binder = new LocalBinder();
    private MediaSessionCompat mediaSession;
    private int currentSongResource = -1;
    private String currentSongName = "";

    public class LocalBinder extends Binder {
        public musicplayerService getService() {
            return musicplayerService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service onCreate");

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnPreparedListener(mp -> {
            if (mp != null) {
                mp.start();
                updateNotification(currentSongName, "正在播放");
            }
        });

        mediaPlayer.setOnCompletionListener(mp -> {
            // Auto play next song when current completes
            sendBroadcast(new Intent(ACTION_NEXT));
        });

        mediaSession = new MediaSessionCompat(this, "MusicService");
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mediaSession.setMediaButtonReceiver(null);

        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            handleAction(intent.getAction());
        }
        return START_STICKY;
    }

    private void handleAction(String action) {
        switch (action) {
            case ACTION_PLAY:
                if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
                    if (currentSongResource != -1) {
                        play(currentSongResource);
                    } else {
                        mediaPlayer.start();
                    }
                    updateNotification(currentSongName, "正在播放");
                }
                break;
            case ACTION_PAUSE:
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    updateNotification(currentSongName, "已暂停");
                }
                break;
            case ACTION_NEXT:
                // Delegate next song logic to activity
                sendBroadcast(new Intent("MUSIC_NEXT"));
                break;
            case ACTION_PREV:
                // Delegate previous song logic to activity
                sendBroadcast(new Intent("MUSIC_PREV"));
                break;
            case ACTION_STOP:
                stopSelf();
                break;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void play(int songResource) {
        play(songResource, "歌曲");
    }

    public void play(int songResource, String songName) {
        try {
            currentSongResource = songResource;
            currentSongName = songName;

            if (mediaPlayer == null) {
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mediaPlayer.setOnPreparedListener(mp -> {
                    if (mp != null) {
                        mp.start();
                        updateNotification(currentSongName, "正在播放");
                    }
                });
            }

            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.reset();
            mediaPlayer = MediaPlayer.create(this, songResource);
            mediaPlayer.setOnCompletionListener(mp -> {
                sendBroadcast(new Intent(ACTION_NEXT));
            });
            mediaPlayer.start();
            updateNotification(currentSongName, "正在播放");
        } catch (Exception e) {
            Log.e(TAG, "播放失败", e);
        }
    }

    public void pause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            updateNotification(currentSongName, "已暂停");
        }
    }

    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.reset();
            stopForeground(true);
        }
    }

    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    public int getCurrentPosition() {
        return mediaPlayer != null ? mediaPlayer.getCurrentPosition() : 0;
    }

    public int getDuration() {
        return mediaPlayer != null ? mediaPlayer.getDuration() : 0;
    }

    public void seekTo(int position) {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(position);
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "音乐播放通知",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("音乐播放控制通知");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private void updateNotification(String title, String text) {
        Intent notificationIntent = new Intent(this, musicplayer.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0);

        Intent playIntent = new Intent(this, musicplayerService.class).setAction(ACTION_PLAY);
        PendingIntent playPendingIntent = PendingIntent.getService(this, 1, playIntent,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0);

        Intent pauseIntent = new Intent(this, musicplayerService.class).setAction(ACTION_PAUSE);
        PendingIntent pausePendingIntent = PendingIntent.getService(this, 2, pauseIntent,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0);

        Intent nextIntent = new Intent(this, musicplayerService.class).setAction(ACTION_NEXT);
        PendingIntent nextPendingIntent = PendingIntent.getService(this, 3, nextIntent,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0);

        Intent prevIntent = new Intent(this, musicplayerService.class).setAction(ACTION_PREV);
        PendingIntent prevPendingIntent = PendingIntent.getService(this, 4, prevIntent,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0);

        Intent stopIntent = new Intent(this, musicplayerService.class).setAction(ACTION_STOP);
        PendingIntent stopPendingIntent = PendingIntent.getService(this, 5, stopIntent,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher_foreground))
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOngoing(true)
                .addAction(R.drawable.prev, "上一首", prevPendingIntent)
                .addAction(isPlaying() ? R.drawable.pause : R.drawable.play,
                        isPlaying() ? "暂停" : "播放",
                        isPlaying() ? pausePendingIntent : playPendingIntent)
                .addAction(R.drawable.next, "下一首", nextPendingIntent)
                .addAction(R.drawable.stop, "停止", stopPendingIntent)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSession.getSessionToken())
                        .setShowActionsInCompactView(0, 1, 2));

        Notification notification = builder.build();
        startForeground(NOTIFICATION_ID, notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (mediaSession != null) {
            mediaSession.release();
            mediaSession = null;
        }
        stopForeground(true);
    }
}
