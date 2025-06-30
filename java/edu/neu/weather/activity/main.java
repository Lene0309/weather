package edu.neu.weather.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import edu.neu.weather.R;
import edu.neu.weather.activity.Profile.ProfileActivity;
import edu.neu.weather.activity.calendar.calendarActivity;
import edu.neu.weather.activity.city.CityManagementActivity;
import edu.neu.weather.activity.days.moredays_weather;
import edu.neu.weather.activity.hours.morehours_weather;
import edu.neu.weather.activity.music.musicplayer;

public class main extends AppCompatActivity {

    private static final String WEATHER_API = "http://t.weather.itboy.net/api/weather/city/";
    private static final int REQUEST_CODE_CITY_MANAGEMENT = 100;
    private LinearLayout weeklyForecastContainer;
    private String hourlyWeatherJsonString;
    private String currentCityCode = "101070102";
    private RequestQueue requestQueue;

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
        setContentView(R.layout.main);

     currentCityCode = loadCityCode();
        weeklyForecastContainer = findViewById(R.id.weeklyForecastContainer);
        requestQueue = Volley.newRequestQueue(this);

    ImageView etcButton = findViewById(R.id.etc);
        etcButton.setOnClickListener(this::showPopupMenu);

    setCurrentDate();
        loadWeatherData(currentCityCode);
        loadHourlyWeatherData(currentCityCode);
        setupMoreButtons();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 读取最新城市名和城市码
        SharedPreferences prefs = getSharedPreferences("weather_prefs", MODE_PRIVATE);
        String cityCode = prefs.getString("current_city_code", "101070102");
        String cityName = prefs.getString("current_city_name", "浑南");
    TextView cityNameText = findViewById(R.id.Addcityname); // 修正为实际id
        if (cityNameText != null) {
            cityNameText.setText(cityName);
        }
        // 刷新天气数据
        loadWeatherData(cityCode);
        loadHourlyWeatherData(cityCode);
    }

    /**
     * 显示弹出菜单
     */
    private void showPopupMenu(View view) {
        Log.d("ETC_BUTTON", "进入showPopupMenu方法");
        try {
            // 创建PopupMenu实例
            PopupMenu popupMenu = new PopupMenu(this, view, Gravity.END);
            popupMenu.getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu());

            // 强制显示图标（兼容方案）
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                popupMenu.setForceShowIcon(true);
            } else {
                   try {
                    Field field = popupMenu.getClass().getDeclaredField("mPopup");
                    field.setAccessible(true);
                    Object menuPopupHelper = field.get(popupMenu);
                    Field mForceShowIcon = menuPopupHelper.getClass().getDeclaredField("mForceShowIcon");
                    mForceShowIcon.setAccessible(true);
                    mForceShowIcon.setBoolean(menuPopupHelper, true);
                } catch (Exception e) {
                    Log.e("PopupMenu", "Force show icon failed", e);
                }
            }

          popupMenu.setOnMenuItemClickListener(item -> {
                Log.d("MENU_ITEM", "点击菜单" + item.getTitle());
                handleMenuItemClick(item);
                return true;
            });

            // 显示菜单
            popupMenu.show();
            Log.d("ETC_BUTTON", "菜单已显示");
        } catch (Exception e) {
            Log.e("PopupMenu", "Error showing menu", e);
            Toast.makeText(this, "菜单加载失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    private void handleMenuItemClick(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.menu_music) {
            startActivityWithAnimation(new Intent(this, musicplayer.class));
        } else if (itemId == R.id.menu_diary) {
            startActivityWithAnimation(new Intent(this, calendarActivity.class));
        } else if (itemId == R.id.menu_profile) {
            startActivityWithAnimation(new Intent(this, ProfileActivity.class));
        }
    }


    private void startActivityWithAnimation(Intent intent) {
        try {
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        } catch (Exception e) {
            Log.e("Navigation", "Failed to start activity", e);
            Toast.makeText(this, "无法打开页面: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // 以下是原有的天气功能代码（保持完整）
    private void setCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年M月d日EEEE", Locale.CHINA);
        String currentDate = sdf.format(new Date());
        ((TextView) findViewById(R.id.currentDate)).setText(currentDate);
    }

    private void saveCityCode(String cityCode) {
        SharedPreferences prefs = getSharedPreferences("weather_prefs", MODE_PRIVATE);
        prefs.edit().putString("current_city_code", cityCode).apply();
    }

    private String loadCityCode() {
        SharedPreferences prefs = getSharedPreferences("weather_prefs", MODE_PRIVATE);
        return prefs.getString("current_city_code", "101070102");
    }

    private void loadWeatherData(String cityCode) {
        StringRequest request = new StringRequest(Request.Method.GET, WEATHER_API + cityCode,
                response -> parseWeatherData(response),
                error -> {
                    showError("获取天气失败，请检查网络");
                    new Handler().postDelayed(() -> loadWeatherData(cityCode), 3000);
                });
        requestQueue.add(request);
    }

    private void loadHourlyWeatherData(String cityCode) {
        String HOURLY_API = "https://n546gyyep2.re.qweatherapi.com/v7/weather/24h?location=" + cityCode + "&key=1f82971c596943058e7a8dc48901b4fd";
        StringRequest request = new StringRequest(Request.Method.GET, HOURLY_API,
                response -> {
                    hourlyWeatherJsonString = response;
                    parseHourlyWeatherData(response);
                },
                error -> showError("获取小时天气失败"));
        requestQueue.add(request);
    }

    private void parseHourlyWeatherData(String response) {
        try {
            JSONObject json = new JSONObject(response);
            JSONArray hourlyArray = json.getJSONArray("hourly");

            HorizontalScrollView scrollView = findViewById(R.id.hourlyScrollView);
            LinearLayout hourlyContainer = findViewById(R.id.hourlyContainer);
            LinearLayout lookMoreContainer = findViewById(R.id.look_more_container);

            hourlyContainer.removeAllViews();
            hourlyContainer.addView(lookMoreContainer);

            for (int i = 0; i < 12 && i < hourlyArray.length(); i++) {
                JSONObject hourData = hourlyArray.getJSONObject(i);
                addHourlyForecastItem(hourlyContainer, hourData);
            }

            hourlyContainer.addView(lookMoreContainer);
            scrollView.post(() -> scrollView.fullScroll(HorizontalScrollView.FOCUS_LEFT));
        } catch (Exception e) {
            showError(" ");
        }
    }

    private void addHourlyForecastItem(LinearLayout container, JSONObject hourData) throws Exception {
        View itemView = LayoutInflater.from(this).inflate(R.layout.hour_forecast, container, false);
        String fullTime = hourData.getString("fxTime");
        String hour = fullTime.split("T")[1].substring(0, 5);
        ((TextView) itemView.findViewById(R.id.hour)).setText(hour);

        String weatherType = hourData.getString("text");
        ImageView icon = itemView.findViewById(R.id.hourimage);
        setHourlyWeatherIcon(icon, weatherType);

        String temp = hourData.getString("temp") + "°";
        ((TextView) itemView.findViewById(R.id.hourtem)).setText(temp);

        container.addView(itemView, container.getChildCount() - 1);
    }

    private void setHourlyWeatherIcon(ImageView imageView, String weatherType) {
        int resId;
        switch (weatherType) {
            case "晴": resId = R.drawable.qing; break;
            case "阴": resId = R.drawable.yin; break;
            case "雨":
            case "小雨":
            case "中雨":
            case "大雨": resId = R.drawable.yu; break;
            case "雷阵雨": resId = R.drawable.lei; break;
            case "多云": resId = R.drawable.duoyun; break;
            case "夜间":
            case "夜晚":
            case "晚上": resId = R.drawable.moon; break;
            default: resId = R.drawable.duoyun; break;
        }
        imageView.setImageResource(resId);
    }

    private void parseWeatherData(String response) {
        try {
            JSONObject json = new JSONObject(response);
            JSONObject data = json.getJSONObject("data");
            JSONArray forecast = data.getJSONArray("forecast");
            JSONObject today = forecast.getJSONObject(0);

            updateBackgroundBasedOnWeather(today.getString("type"));

            LinearLayout weeklyForecastContainer = findViewById(R.id.weeklyForecastContainer);
            LinearLayout lookmorecontainer = findViewById(R.id.lookmorecontainer);

            weeklyForecastContainer.removeAllViews();

            int daysToShow = Math.min(forecast.length(), 7);
            for (int i = 1; i <= daysToShow; i++) {
                JSONObject day = forecast.getJSONObject(i);
                addForecastItem(day);
            }

            weeklyForecastContainer.addView(lookmorecontainer);
            updateCurrentWeather(data, today);
            updateWindInfo(today);
            updateAirQualityInfo(data);
            updateSunriseSunsetInfo(today);

        } catch (Exception e) {
            showError("解析天气数据失败");
        }
    }

    private void updateAirQualityInfo(JSONObject data) throws Exception {
        String quality = data.optString("quality", "未知");
        int pm25 = data.optInt("pm25", 0);
        int pm10 = data.optInt("pm10", 0);
        String humidity = data.optString("shidu", "--");
        String ganmao = data.optString("ganmao", "请关注天气变化，注意身体健康");

        setTextViewText(R.id.qualityTextView, "空气质量: " + quality);
        setTextViewText(R.id.humidity, humidity);
        setTextViewText(R.id.pm2_5, String.valueOf(pm25));
        setTextViewText(R.id.pm10, String.valueOf(pm10));
        setTextViewText(R.id.healthTips, " ");
    }

    private void updateWindInfo(JSONObject today) throws Exception {
        String windDirection = today.has("fx") ? today.getString("fx") : "未知风向";
        String windPower = today.has("fl") ? today.getString("fl").replace("", "") :"未知风力";

        setTextViewText(R.id.wind, windDirection);
        setTextViewText(R.id.windforce, windPower);
    }

    private void updateSunriseSunsetInfo(JSONObject today) throws Exception {
        String sunrise = today.has("sunrise") ? today.getString("sunrise") : "--:--";
        String sunset = today.has("sunset") ? today.getString("sunset") : "--:--";

        setTextViewText(R.id.sunrisetime, sunrise);
        setTextViewText(R.id.sunsettime, sunset);

        calculateAndDisplayDaytimeDuration(sunrise, sunset);
    }

    private void calculateAndDisplayDaytimeDuration(String sunrise, String sunset) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.CHINA);
            Date sunriseTime = sdf.parse(sunrise);
            Date sunsetTime = sdf.parse(sunset);

            long diff = sunsetTime.getTime() - sunriseTime.getTime();
            long hours = TimeUnit.MILLISECONDS.toHours(diff);
            long minutes = TimeUnit.MILLISECONDS.toMinutes(diff) % 60;

            String durationText = String.format(Locale.CHINA, "白天时长: %d小时%02d分钟", hours, minutes);
            setTextViewText(R.id.dayhours, durationText);
        } catch (Exception e) {
            setTextViewText(R.id.dayhours, "白天时长: --");
        }
    }

    private void addForecastItem(JSONObject dayData) throws Exception {
        View itemView = LayoutInflater.from(this)
                .inflate(R.layout.item_forecast, weeklyForecastContainer, false);

        String dateStr = formatDate(dayData.getString("ymd"), dayData.getString("week"));
        ((TextView) itemView.findViewById(R.id.futuredate)).setText(dateStr);

        String weatherType = dayData.getString("type");
        setWeatherIcon((ImageView) itemView.findViewById(R.id.imagefuture), weatherType);

        ((TextView) itemView.findViewById(R.id.futureweather)).setText(weatherType);

        String highTemp = dayData.getString("high").replaceAll("[^0-9]", "");
        String lowTemp = dayData.getString("low").replaceAll("[^0-9]", "");
        ((TextView) itemView.findViewById(R.id.futuretem)).setText(highTemp + "°/" + lowTemp + "°");

        weeklyForecastContainer.addView(itemView);
    }

    private void updateCurrentWeather(JSONObject data, JSONObject today) throws Exception {
        setTextViewText(R.id.currentTemperature, data.getString("wendu") + "°");

        String highTemp = today.getString("high").replaceAll("[^0-9]", "");
        String lowTemp = today.getString("low").replaceAll("[^0-9]", "");
        setTextViewText(R.id.textView, highTemp + "°/" + lowTemp + "°");

        setTextViewText(R.id.weatherCondition, today.getString("type") + " " + data.getString("quality"));

        ImageView weatherIcon = findViewById(R.id.currentWeatherIcon);
        if (weatherIcon != null) {
            setWeatherIcon(weatherIcon, today.getString("type"));
        }

        updateBackgroundBasedOnWeather(today.getString("type"));
        setTextViewText(R.id.notice, today.optString("notice", "请关注天气变化，注意身体健康"));
    }

    private void updateBackgroundBasedOnWeather(String weatherType) {
        int backgroundResId;
        if (weatherType.contains("") || weatherType.contains(") ")) {
            backgroundResId = R.drawable.background_yu;
        } else if (weatherType.contains("") || weatherType.contains("阴") ) {
            backgroundResId = R.drawable.background_yin;
        } else {
            backgroundResId = R.drawable.background;
        }
        findViewById(android.R.id.content).getRootView().setBackgroundResource(backgroundResId);
    }

    private String formatDate(String ymd, String week) throws Exception {
        SimpleDateFormat inFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        SimpleDateFormat outFormat = new SimpleDateFormat("M/d", Locale.CHINA);
        Date date = inFormat.parse(ymd);
        return outFormat.format(date) + " " + week.replace("星期", "");
    }

    private void setWeatherIcon(ImageView imageView, String weatherType) {
        int resId;
        switch (weatherType) {
            case "晴": resId = R.drawable.qing; break;
            case "阴": resId = R.drawable.yin; break;
            case "雨":
            case "小雨":
            case "中雨":
            case "阵雨":
            case "大雨": resId = R.drawable.yu; break;
            case "雷阵雨": resId = R.drawable.lei; break;
            case "多云":
            case "晴转多云":
            case "多云转晴": resId = R.drawable.duoyun; break;
            default: resId = R.drawable.duoyun; break;
        }
        imageView.setImageResource(resId);
    }

    private void showError(String message) {

    }

    private void setTextViewText(int viewId, String text) {
        TextView textView = findViewById(viewId);
        if (textView != null) {
            textView.setText(text);
        }
    }

    private void setupMoreButtons() {
       ImageView btnMoreWeather = findViewById(R.id.btnMoreWeather);
        btnMoreWeather.setOnClickListener(v -> {
            Intent intent = new Intent(this, moredays_weather.class);
            intent.putExtra("city_code", currentCityCode);
            startActivityWithAnimation(intent);
        });

        // 小时天气预报按钮
        ImageView lookMore1 = findViewById(R.id.look_more1);
        lookMore1.setOnClickListener(v -> {
            if (hourlyWeatherJsonString != null) {
                Intent intent = new Intent(this, morehours_weather.class);
                intent.putExtra("city_code", currentCityCode);
                intent.putExtra("hourly_data", hourlyWeatherJsonString);
                startActivityWithAnimation(intent);
            } else {
                showError("天气数据未加载完");
            }
        });

        // 城市管理按钮
        ImageView settingButton = findViewById(R.id.imagesetting);
        settingButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, CityManagementActivity.class);
            startActivityForResult(intent, REQUEST_CODE_CITY_MANAGEMENT);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CITY_MANAGEMENT && resultCode == RESULT_OK && data != null) {
            String newCode = data.getStringExtra("city_code");
            String newName = data.getStringExtra("city_name");

            if (newCode != null && !newCode.equals(currentCityCode)) {
                currentCityCode = newCode;
                saveCityCode(currentCityCode);
                // 保存新城市名
                SharedPreferences prefs = getSharedPreferences("weather_prefs", MODE_PRIVATE);
                prefs.edit().putString("current_city_name", newName).apply();
                // 刷新主页面城市名
                setTextViewText(R.id.Addcityname, newName);
                setTextViewText(R.id.locationText, newName);
                loadWeatherData(currentCityCode);
                loadHourlyWeatherData(currentCityCode);

            }
        }
    }
}
