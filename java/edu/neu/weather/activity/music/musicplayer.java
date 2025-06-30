package edu.neu.weather.activity.music;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.neu.weather.R;
import android.content.SharedPreferences;

public class musicplayer extends AppCompatActivity {
    private MediaPlayer mediaPlayer;
    
    // 本地音乐资源
    private List<Integer> localSongResources = Arrays.asList(
            R.raw.baby,
            R.raw.pinksoda,
            R.raw.yan,
            R.raw.wanan,
            R.raw.wetoo,
            R.raw.street,
            R.raw.celebrity,
            R.raw.far,
            R.raw.wild,
            R.raw.myname,
            R.raw.loveme
    );
    private List<String> localSongNames = Arrays.asList(
            "Baby",
            "粉色苏打",
            "艳",
            "晚安",
            "我们俩",
            "烟袋斜街",
            "Celebrity",
            "多远都要在一起",
            "Wild",
            "我的名字",
            "Love Me Like You Do"
    );
    private List<String> localSongArtists = Arrays.asList(
            "Justin Bieber",
            "木秦/安全着陆",
            "oner",
            "颜人中",
            "郭顶",
            "吻枪",
            "IU",
            "邓紫棋",
            "Yuki",
            "焦迈奇",
            "Lene"
    );
    private List<String> localSongAlbums = Arrays.asList(
            "My World 2.0",
            "夏日特辑",
            "镜像马戏团",
            "未知专辑",
            "未知专辑",
            "未知专辑",
            "未知专辑",
            "未知专辑",
            "未知专辑",
            "未知专辑",
            "未知专辑"
    );
    private List<String> localSongDurations = Arrays.asList(
            "03:36",
            "03:00",
            "03:33",
            "04:00",
            "03:30",
            "02:30",
            "03:15",
            "03:45",
            "03:20",
            "03:50",
            "05:30"
    );
    private List<String> localSongBitrates = Arrays.asList(
            "256 kbps",
            "320 kbps",
            "192 kbps",
            "320 kbps",
            "320 kbps",
            "320 kbps",
            "320 kbps",
            "320 kbps",
            "320 kbps",
            "320 kbps",
            "320 kbps"
    );
    private List<String> localSongFileSizes = Arrays.asList(
            "3.8 MB",
            "5.6 MB",
            "4.2 MB",
            "4.4 MB",
            "3.7 MB",
            "829 KB",
            "3.0 MB",
            "3.3 MB",
            "3.3 MB",
            "3.8 MB",
            "9.7 MB"
    );
    
    // 存储卡音乐列表
    private List<Song> storageSongs = new ArrayList<>();
    // 网络音乐列表
    private List<Song> networkSongs = new ArrayList<>();
    // 当前播放列表
    private List<Song> currentPlaylist = new ArrayList<>();
    
    private int currentSongIndex = 0;
    private boolean isPlaying = false;
    private Handler handler = new Handler();
    private musicplayerService mService;
    private boolean isBound = false;
    private int currentSource = 0;

    // UI Components
    private TextView tvSongTitle, tvArtist, tvAlbum, tvCurrentTime, tvTotalTime;
    private TextView tvDuration, tvBitrate, tvFileSize, tvSource;
    private ImageButton btnPlayPause, btnPrev, btnNext;
    private MaterialButton btnSwitchSource;
    private SeekBar seekBar;
    private RecyclerView recyclerViewSongs;
    private MaterialCardView playlistCard;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            musicplayerService.LocalBinder binder = (musicplayerService.LocalBinder) service;
            mService = binder.getService();
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            isBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        int themeMode = prefs.getInt("theme_mode", 0);
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
        setContentView(R.layout.musicplayer);

        initViews();
        setupButtonListeners();
        
        // 恢复用户上次选择的音乐源
        currentSource = prefs.getInt("music_source", 0);
        switch (currentSource) {
            case 0:
                loadLocalSongs();
                btnSwitchSource.setText("本地音乐");
                break;
            case 1:
                if (checkStoragePermission()) {
                    loadStorageSongs();
                    btnSwitchSource.setText("存储卡音乐");
                } else {
                    currentSource = 0;
                    loadLocalSongs();
                    btnSwitchSource.setText("本地音乐");
                }
                break;
            case 2:
                btnSwitchSource.setText("网络音乐");
                // 网络音乐需要用户主动搜索，这里不预加载
                break;
        }
        
    if (currentPlaylist.isEmpty()) {
            loadLocalSongs();
            currentSource = 0;
            btnSwitchSource.setText("本地音乐");
        }
        
        updateSongList();

        Intent intent = new Intent(this, musicplayerService.class);
        bindService(intent, connection, BIND_AUTO_CREATE);
    }

    private void initViews() {
        tvSongTitle = findViewById(R.id.tvSongTitle);
        tvArtist = findViewById(R.id.tvArtist);
        tvAlbum = findViewById(R.id.tvAlbum);
        tvCurrentTime = findViewById(R.id.tvCurrentTime);
        tvTotalTime = findViewById(R.id.tvTotalTime);
        tvDuration = findViewById(R.id.tvDuration);
        tvBitrate = findViewById(R.id.tvBitrate);
        tvFileSize = findViewById(R.id.tvFileSize);
        tvSource = findViewById(R.id.tvSource);

        btnPlayPause = findViewById(R.id.fabPlayPause);
        btnPrev = findViewById(R.id.btnPrev);
        btnNext = findViewById(R.id.btnNext);
        btnSwitchSource = findViewById(R.id.btnSwitchSource);
        seekBar = findViewById(R.id.sliderProgress);
        recyclerViewSongs = findViewById(R.id.recyclerViewPlaylist);
        playlistCard = findViewById(R.id.playlistCard);

        // 添加调试信息
        if (tvSongTitle == null) Log.e("MusicPlayer", "tvSongTitle not found");
        if (tvArtist == null) Log.e("MusicPlayer", "tvArtist not found");
        if (tvAlbum == null) Log.e("MusicPlayer", "tvAlbum not found");
        if (btnPlayPause == null) Log.e("MusicPlayer", "btnPlayPause not found");
        if (btnPrev == null) Log.e("MusicPlayer", "btnPrev not found");
        if (btnNext == null) Log.e("MusicPlayer", "btnNext not found");
        if (btnSwitchSource == null) Log.e("MusicPlayer", "btnSwitchSource not found");
        if (seekBar == null) Log.e("MusicPlayer", "seekBar not found");
        if (recyclerViewSongs == null) Log.e("MusicPlayer", "recyclerViewSongs not found");
        if (playlistCard == null) Log.e("MusicPlayer", "playlistCard not found");

        recyclerViewSongs.setLayoutManager(new LinearLayoutManager(this));
        mediaPlayer = new MediaPlayer();
        
        // 确保按钮被正确初始化
        if (btnSwitchSource != null) {
            btnSwitchSource.setText("本地音乐");
        }
    }

    private void setupButtonListeners() {
        btnPlayPause.setOnClickListener(v -> togglePlayPause());
        btnPrev.setOnClickListener(v -> playPrevious());
        btnNext.setOnClickListener(v -> playNext());
        btnSwitchSource.setOnClickListener(v -> {

            showSourceSelectionDialog();
        });

        // 添加播放列表按钮点击事件
        MaterialButton btnPlaylist = findViewById(R.id.btnPlaylist);
        if (btnPlaylist != null) {
            btnPlaylist.setOnClickListener(v -> {
                togglePlaylist();
            });
        } else {
            Log.e("MusicPlayer", "btnPlaylist not found");
        }

        // 添加关闭播放列表按钮点击事件
        MaterialButton btnClosePlaylist = findViewById(R.id.btnClosePlaylist);
        if (btnClosePlaylist != null) {
            btnClosePlaylist.setOnClickListener(v -> {
                playlistCard.setVisibility(View.GONE);
            });
        } else {
            Log.e("MusicPlayer", "btnClosePlaylist not found");
        }

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mediaPlayer != null) {
                    mediaPlayer.seekTo(progress);
                    tvCurrentTime.setText(formatTime(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void togglePlaylist() {
        if (playlistCard.getVisibility() == View.VISIBLE) {
            playlistCard.setVisibility(View.GONE);
        } else {
            playlistCard.setVisibility(View.VISIBLE);
            updateSongList();
        }
    }

    private void showSourceSelectionDialog() {
        String[] sources = {"本地音乐", "存储卡音乐", "网络音乐"};
        String[] descriptions = {
            "内置音乐 (" + localSongNames.size() + ") ",
            "存储卡音乐(" + storageSongs.size() + ") ",
            "在线音乐 "
        };
        
        new AlertDialog.Builder(this)
                .setTitle("选择音乐")
                .setItems(descriptions, (dialog, which) -> {
                    currentSource = which;
                    // 保存用户偏好
                    SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
                    prefs.edit().putInt("music_source", which).apply();
                    
                    switch (which) {
                        case 0:
                            loadLocalSongs();
                            btnSwitchSource.setText("本地音乐");
                            break;
                        case 1:
                            if (checkStoragePermission()) {
                                loadStorageSongs();
                                btnSwitchSource.setText("存储卡音乐");
                            } else {
                                requestStoragePermission();
                            }
                            break;
                        case 2:
                            showNetworkSearchDialog();
                            btnSwitchSource.setText("网络音乐");
                            break;
                    }
                    updateSongList();
                })
                .show();
    }

    private void showNetworkSearchDialog() {
        String[] options = {"显示固定在线音乐", "搜索网络音乐"};
        
        new AlertDialog.Builder(this)
                .setTitle("选择在线音乐")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        // 显示固定在线音乐
                        searchNetworkMusic(""); // 空字符串表示显示固定音乐
                    } else {
                        // 搜索网络音乐
                        showNetworkSearchInputDialog();
                    }
                })
                .show();
    }

    private void showNetworkSearchInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("搜索网络音乐");

        final EditText input = new EditText(this);
        input.setHint("输入歌曲名或歌手");
        builder.setView(input);

        builder.setPositiveButton("搜索", (dialog, which) -> {
            String query = input.getText().toString();
            if (!query.isEmpty()) {
                searchNetworkMusic(query);
            } else {
                // 如果搜索词为空，显示固定音乐
                searchNetworkMusic("");
            }
        });
        builder.setNegativeButton("取消", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void searchNetworkMusic(String query) {
        // 模拟网络搜索延迟
        new Handler().postDelayed(() -> {
            networkSongs.clear();
            
            networkSongs.add(new Song("我们俩", "郭顶", "在线专辑", "03:10", "320 kbps", "3.1 MB", "withyou", Song.Source.NETWORK));
            networkSongs.add(new Song("Love Story", "Taylor Swift", "在线专辑", "03:36", "320 kbps", "3.6 MB", "lovestrory", Song.Source.NETWORK));
            networkSongs.add(new Song("就是爱你", "苡慧", "在线专辑", "03:36", "320 kbps", "3.6 MB", "loveyou", Song.Source.NETWORK));
            
            // 只有当搜索词不为空时，才添加搜索结果
            if (!query.isEmpty()) {
                if (query.toLowerCase().contains("baby")) {
                    networkSongs.add(new Song("Baby", "Justin Bieber", "My World 2.0", "03:36", "320 kbps", "5.2 MB", "baby", Song.Source.NETWORK));
                }
                if (query.toLowerCase().contains("love")) {
                    networkSongs.add(new Song("Love Story", "Taylor Swift", "Fearless", "03:55", "256 kbps", "6.1 MB", "lovestrory", Song.Source.NETWORK));
                }
                if (query.toLowerCase().contains("shape")) {
                    networkSongs.add(new Song("就是爱你", "苡慧", "÷", "03:53", "320 kbps", "7.2 MB", "loveyou", Song.Source.NETWORK));
                }
                
                // 添加通用搜索结果
                networkSongs.add(new Song(query + " - 热门版本", "热门歌手", "热门专辑", "03:45", "320 kbps", "5.5 MB", "https://example.com/popular_" + query.replace(" ", "_") + ".mp3", Song.Source.NETWORK));
                networkSongs.add(new Song(query + " - 经典版本", "经典歌手", "经典专辑", "04:12", "256 kbps", "6.8 MB", "https://example.com/classic_" + query.replace(" ", "_") + ".mp3", Song.Source.NETWORK));
                networkSongs.add(new Song(query + " - 翻唱版本", "翻唱歌手", "翻唱专辑", "03:28", "320 kbps", "5.1 MB", "https://example.com/cover_" + query.replace(" ", "_") + ".mp3", Song.Source.NETWORK));
            }
            
            currentPlaylist = networkSongs;
            updateSongList();
            
            if (networkSongs.isEmpty()) {
                Toast.makeText(musicplayer.this, "未找到相关歌曲", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(musicplayer.this, "找到 " + networkSongs.size() + " 首在线歌曲", Toast.LENGTH_SHORT).show();
            }
        }, 1000);
    }

    private void loadLocalSongs() {
        currentPlaylist.clear();
        for (int i = 0; i < localSongNames.size(); i++) {
            currentPlaylist.add(new Song(
                localSongNames.get(i),
                localSongArtists.get(i),
                localSongAlbums.get(i),
                localSongDurations.get(i),
                localSongBitrates.get(i),
                localSongFileSizes.get(i),
                String.valueOf(localSongResources.get(i)),
                Song.Source.LOCAL
            ));
        }
        Log.d("MusicPlayer", "本地音乐加载完成，共 " + currentPlaylist.size() + " 首歌曲");
        Toast.makeText(this, "本地音乐加载完成，共 " + currentPlaylist.size() + " 首歌曲", Toast.LENGTH_SHORT).show();
    }

    private void loadStorageSongs() {
        storageSongs.clear();
        if (checkStoragePermission()) {
            String[] projection = {
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.SIZE
            };

            String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
            String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";

            try (Cursor cursor = getContentResolver().query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    selection,
                    null,
                    sortOrder)) {

                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                        String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                        String album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
                        long duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                        String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                        long size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));

                        if (artist == null || artist.isEmpty()) {
                            artist = "未知歌手";
                        }
                        if (album == null || album.isEmpty()) {
                            album = "未知专辑";
                        }

                        String durationStr = formatTime((int) duration);
                        String sizeStr = formatFileSize(size);
                        String bitrate = "320 kbps"; // 默认比特率
                        storageSongs.add(new Song(title, artist, album, durationStr, bitrate, sizeStr, path, Song.Source.STORAGE));
                    } while (cursor.moveToNext());
                }
            } catch (Exception e) {
                Log.e("MusicPlayer", "加载存储卡音乐失败", e);
                Toast.makeText(this, "加载存储卡音乐失败 " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        currentPlaylist = storageSongs;
    }

    private boolean checkStoragePermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, 
            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 
            1001);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1001) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadStorageSongs();
                updateSongList();
            } else {
                Toast.makeText(this, "需要存储权限才能访问音乐文件", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateSongList() {
        Log.d("MusicPlayer", "更新歌曲列表，当前列表 " + currentPlaylist.size());
        
        recyclerViewSongs.setAdapter(new RecyclerView.Adapter() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_song, parent, false);
                return new SongViewHolder(view);
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                SongViewHolder songHolder = (SongViewHolder) holder;
                songHolder.bind(position);
            }

            @Override
            public int getItemCount() {
                Log.d("MusicPlayer", "Adapter getItemCount: " + currentPlaylist.size());
                return currentPlaylist.size();
            }

            class SongViewHolder extends RecyclerView.ViewHolder {
                private final TextView tvSongTitle, tvSongArtist, tvSongSource, tvSongDuration;

                SongViewHolder(View itemView) {
                    super(itemView);
                    tvSongTitle = itemView.findViewById(R.id.tvSongTitle);
                    tvSongArtist = itemView.findViewById(R.id.tvSongArtist);
                    tvSongSource = itemView.findViewById(R.id.tvSongSource);
                    tvSongDuration = itemView.findViewById(R.id.tvSongDuration);

                    itemView.setOnClickListener(v -> {
                        int adapterPosition = getAdapterPosition();
                        if (adapterPosition != RecyclerView.NO_POSITION) {
                            currentSongIndex = adapterPosition;
                            playSong(adapterPosition);
                            playlistCard.setVisibility(View.GONE);
                        }
                    });
                }

                void bind(int position) {
                    if (position < currentPlaylist.size()) {
                        Song song = currentPlaylist.get(position);
                        tvSongTitle.setText(song.getTitle());
                        tvSongArtist.setText(song.getArtist());
                        tvSongDuration.setText(song.getDuration());
                        
                        Log.d("MusicPlayer", "绑定歌曲: " + song.getTitle() + " - " + song.getArtist());
                        
                        switch (song.getSource()) {
                            case LOCAL:
                                tvSongSource.setText("本地音乐");
                                break;
                            case STORAGE:
                                tvSongSource.setText("存储卡");
                                break;
                            case NETWORK:
                                tvSongSource.setText("网络音乐");
                                break;
                        }
                    }
                }
            }
        });
        
        Toast.makeText(this, "歌曲列表已更新，" + currentPlaylist.size() + " 首歌", Toast.LENGTH_SHORT).show();
    }

    private void playSong(int position) {
        if (position < 0 || position >= currentPlaylist.size()) {
            return;
        }

        try {
            if (mediaPlayer != null) {
                mediaPlayer.reset();
                
                Song song = currentPlaylist.get(position);
                
                if (song.getSource() == Song.Source.LOCAL) {
                    // 播放本地资源
                    int resourceId = Integer.parseInt(song.getPath());
                    mediaPlayer = MediaPlayer.create(this, resourceId);
                } else if (song.getSource() == Song.Source.STORAGE) {
                    mediaPlayer.setDataSource(song.getPath());
                    mediaPlayer.prepare();
                } else if (song.getSource() == Song.Source.NETWORK) {
                    // 播放网络音乐
                    String path = song.getPath();
                    if (path.equals("withyou")) {
                        // 播放withyou.mp3
                        mediaPlayer = MediaPlayer.create(this, R.raw.withyou);
                    } else if (path.equals("lovestrory")) {
                        // 播放lovestrory.mp3
                        mediaPlayer = MediaPlayer.create(this, R.raw.lovestrory);
                    } else if (path.equals("loveyou")) {
                        // 播放loveyou.mp3
                        mediaPlayer = MediaPlayer.create(this, R.raw.loveyou);
                    } else if (path.equals("baby")) {
                        // 播放baby.mp3
                        mediaPlayer = MediaPlayer.create(this, R.raw.baby);
                    } else {
                        // 其他网络音乐
                        mediaPlayer.setDataSource(song.getPath());
                        mediaPlayer.prepareAsync();
                    }
                }

                if (mediaPlayer != null) {
                    updateSongInfo(song);

                    mediaPlayer.setOnPreparedListener(mp -> {
                        seekBar.setMax(mediaPlayer.getDuration());
                        tvTotalTime.setText(formatTime(mediaPlayer.getDuration()));
                        startPlayback();
                        if (isBound && mService != null) {
                            if (song.getSource() == Song.Source.LOCAL) {
                                mService.play(Integer.parseInt(song.getPath()));
                            }
                        }
                        if (song.getSource() == Song.Source.NETWORK) {

                        }
                    });

                    mediaPlayer.setOnCompletionListener(mp -> playNext());
                    
                    mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                        String errorMsg = "播放失败";
                        switch (what) {
                            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                                errorMsg = "服务器连接失败";
                                break;
                            case MediaPlayer.MEDIA_ERROR_IO:
                                errorMsg = "网络连接失败";
                                break;
                            case MediaPlayer.MEDIA_ERROR_MALFORMED:
                                errorMsg = "音频文件损坏";
                                break;
                            case MediaPlayer.MEDIA_ERROR_UNSUPPORTED:
                                errorMsg = "不支持的音频格式";
                                break;
                        }
                        Toast.makeText(musicplayer.this, errorMsg, Toast.LENGTH_SHORT).show();
                        return true;
                    });
                }
            }
        } catch (IllegalArgumentException e) {
            Log.e("MusicPlayer", "无效的音频源", e);
            Toast.makeText(this, "无效的音频源: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (SecurityException e) {
            Log.e("MusicPlayer", "权限不足", e);

        } catch (IOException e) {
            Log.e("MusicPlayer", "IO错误", e);
            Toast.makeText(this, "无法读取音频文件: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("MusicPlayer", "播放失败", e);
            Toast.makeText(this, "播放失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateSongInfo(Song song) {
        tvSongTitle.setText(song.getTitle());
        tvArtist.setText(song.getArtist());
        tvAlbum.setText(song.getAlbum());
        tvDuration.setText(song.getDuration());
        tvBitrate.setText(song.getBitrate());
        tvFileSize.setText(song.getFileSize());
        
        switch (song.getSource()) {
            case LOCAL:
                tvSource.setText("本地存储");
                break;
            case STORAGE:
                tvSource.setText("存储卡");
                break;
            case NETWORK:
                tvSource.setText("网络");
                break;
        }
    }

    private void updatePlaybackStatus() {
        View playingIndicator = findViewById(R.id.viewPlayingIndicator);
        if (playingIndicator != null) {
            if (isPlaying) {
                playingIndicator.setVisibility(View.VISIBLE);
            } else {
                playingIndicator.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void startPlayback() {
        if (mediaPlayer != null) {
            mediaPlayer.start();
            isPlaying = true;
            btnPlayPause.setImageResource(R.drawable.pause);
            updatePlaybackStatus();
            handler.postDelayed(updateSeekBar, 1000);
        }
    }

    private Runnable updateSeekBar = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer != null && isPlaying) {
                int currentPosition = mediaPlayer.getCurrentPosition();
                seekBar.setProgress(currentPosition);
                tvCurrentTime.setText(formatTime(currentPosition));
                handler.postDelayed(this, 1000);
            }
        }
    };

    private void togglePlayPause() {
        if (isPlaying) {
            pauseMusic();
        } else {
            if (mediaPlayer != null && !mediaPlayer.isPlaying() && mediaPlayer.getDuration() > 0) {
                startPlayback();
                if (isBound && mService != null && currentPlaylist.get(currentSongIndex).getSource() == Song.Source.LOCAL) {
                    mService.play(Integer.parseInt(currentPlaylist.get(currentSongIndex).getPath()));
                }
            } else if (currentPlaylist.isEmpty()) {
                Toast.makeText(this, "没有可播放的歌曲", Toast.LENGTH_SHORT).show();
            } else {
                playSong(currentSongIndex);
            }
        }
    }

    private void pauseMusic() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isPlaying = false;
            btnPlayPause.setImageResource(R.drawable.play);
            updatePlaybackStatus();
            handler.removeCallbacks(updateSeekBar);

            if (isBound && mService != null) {
                mService.pause();
            }
        }
    }

    private void playPrevious() {
        if (currentPlaylist.isEmpty()) return;
        currentSongIndex = (currentSongIndex - 1 + currentPlaylist.size()) % currentPlaylist.size();
        playSong(currentSongIndex);
    }

    private void playNext() {
        if (currentPlaylist.isEmpty()) return;
        currentSongIndex = (currentSongIndex + 1) % currentPlaylist.size();
        playSong(currentSongIndex);
    }

    private String formatTime(int milliseconds) {
        int seconds = (milliseconds / 1000) % 60;
        int minutes = (milliseconds / (1000 * 60)) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp-1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        handler.removeCallbacks(updateSeekBar);

        if (isBound) {
            unbindService(connection);
            isBound = false;
        }
    }

public static class Song {
        public enum Source { LOCAL, STORAGE, NETWORK }
        
        private String title;
        private String artist;
        private String album;
        private String duration;
        private String bitrate;
        private String fileSize;
        private String path;
        private Source source;

        public Song(String title, String artist, String album, String duration,
                   String bitrate, String fileSize, String path, Source source) {
            this.title = title;
            this.artist = artist;
            this.album = album;
            this.duration = duration;
            this.bitrate = bitrate;
            this.fileSize = fileSize;
            this.path = path;
            this.source = source;
        }

        // Getters
        public String getTitle() { return title; }
        public String getArtist() { return artist; }
        public String getAlbum() { return album; }
        public String getDuration() { return duration; }
        public String getBitrate() { return bitrate; }
        public String getFileSize() { return fileSize; }
        public String getPath() { return path; }
        public Source getSource() { return source; }
    }
}
