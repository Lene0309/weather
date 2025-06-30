package edu.neu.weather.activity.days;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import edu.neu.weather.R;

public class moredays_weather extends AppCompatActivity {

    private static final String WEATHER_API = "http://t.weather.itboy.net/api/weather/city/%s";
    private static final String EXTRA_CITY_CODE = "city_code";

     public interface OnWeatherItemClickListener {
        void onItemClick(WeatherItem item);
    }

    private RecyclerView forecastRecyclerView;
    private RequestQueue requestQueue;
    private TextView titleText, locationText, currentTemp, currentCondition, currentDate;
    private ImageView currentWeatherIcon;
    private LineChart tempChart;
    private String cityCode = "101070102";

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
        setContentView(R.layout.moredays_weather);

        initViews();
        requestQueue = Volley.newRequestQueue(this);

        // 从Intent获取城市代码
        if (getIntent() != null && getIntent().hasExtra(EXTRA_CITY_CODE)) {
            cityCode = getIntent().getStringExtra(EXTRA_CITY_CODE);
        }

        fetchWeatherData();
    }

    private void initViews() {
        titleText = findViewById(R.id.titleText);
        locationText = findViewById(R.id.locationText);
        currentTemp = findViewById(R.id.currentTemp);
        currentCondition = findViewById(R.id.currentCondition);
        currentDate = findViewById(R.id.currentDate);
        currentWeatherIcon = findViewById(R.id.currentWeatherIcon);
        tempChart = findViewById(R.id.temputureChart);

        forecastRecyclerView = findViewById(R.id.forecastList);
        forecastRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        titleText.setText("多日天气预报");
        locationText.setText("沈阳"); // 初始城市名称，将在获取数据后更新
    }

    private void fetchWeatherData() {
        String apiUrl = String.format(WEATHER_API, cityCode);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, apiUrl, null,
                this::handleWeatherResponse,
                this::handleErrorResponse
        );

        requestQueue.add(jsonObjectRequest);
    }

    private void handleWeatherResponse(JSONObject response) {
        try {
            parseAndUpdateWeatherData(response);
        } catch (JSONException | ParseException e) {
            e.printStackTrace();
            // Toast������ɾ��
        }
    }

    private void handleErrorResponse(VolleyError error) {
        // Toast������ɾ��
        Log.e("WeatherAPI", "Error: " + error.getMessage());
    }

    private void parseAndUpdateWeatherData(JSONObject response) throws JSONException, ParseException {
        JSONObject cityInfo = response.getJSONObject("cityInfo");
        String cityName = cityInfo.getString("city");
        locationText.setText(cityName);

        JSONObject data = response.getJSONObject("data");
        JSONArray forecastArray = data.getJSONArray("forecast");
        JSONObject firstForecast = forecastArray.getJSONObject(0);

        currentTemp.setText(data.getString("wendu") + "°");
        currentCondition.setText(firstForecast.getString("type"));
        currentDate.setText(formatDate(firstForecast.getString("ymd")));
        setWeatherIcon(currentWeatherIcon, firstForecast.getString("type"));

        updateBackgroundBasedOnWeather(firstForecast.getString("type"));

        List<WeatherItem> forecastItems = new ArrayList<>();
        List<Entry> highTempEntries = new ArrayList<>();
        List<Entry> lowTempEntries = new ArrayList<>();

        for (int i = 0; i < forecastArray.length(); i++) {
            JSONObject forecast = forecastArray.getJSONObject(i);
            WeatherItem item = createWeatherItem(forecast);
            forecastItems.add(item);

            highTempEntries.add(new Entry(i, extractTemperature(forecast.getString("high"))));
            lowTempEntries.add(new Entry(i, extractTemperature(forecast.getString("low"))));
        }

        setupRecyclerView(forecastItems);
        setupTemperatureChart(highTempEntries, lowTempEntries);
    }

    private void updateBackgroundBasedOnWeather(String weatherType) {
        View rootView = findViewById(android.R.id.content).getRootView();
        int backgroundResId;

        if (weatherType.contains("雨") || weatherType.contains("�?") || weatherType.contains("阵雨")) {
            backgroundResId = R.drawable.background_yu;
        } else {
            backgroundResId = R.drawable.background;
        }

        rootView.setBackgroundResource(backgroundResId);
    }

    private WeatherItem createWeatherItem(JSONObject forecast) throws JSONException {
        String date = forecast.getString("date");
        String ymd = forecast.getString("ymd");
        String type = forecast.getString("type");
        String highTemp = extractTemperatureString(forecast.getString("high"));
        String lowTemp = extractTemperatureString(forecast.getString("low"));
        String sunrise = forecast.getString("sunrise");
        String sunset = forecast.getString("sunset");
        String wind = forecast.getString("fx") + forecast.getString("fl");
        String aqi = forecast.getString("aqi");
        String notice = forecast.getString("notice");

        return new WeatherItem(
                getWeekDay(date),
                ymd.substring(5),
                getWeatherIconRes(type),
                type,
                highTemp,
                lowTemp,
                sunrise,
                sunset,
                wind,
                aqi,
                notice
        );
    }

    private float extractTemperature(String tempStr) {
        return Float.parseFloat(tempStr.replaceAll("[^0-9]", ""));
    }

    private String extractTemperatureString(String tempStr) {
        return tempStr.replaceAll("[^0-9]", "") + "°";
    }

    private String formatDate(String ymd) throws ParseException {
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault());
        Date date = inputFormat.parse(ymd);
        return outputFormat.format(date);
    }

    private String getWeekDay(String date) {
        String[] weekDays = {"星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"};
        int dayOfWeek = Integer.parseInt(date) % 7;
        return weekDays[dayOfWeek];
    }

    private void setWeatherIcon(ImageView imageView, String weatherType) {
        int resId = getWeatherIconRes(weatherType);
        if (resId != 0) {
            imageView.setImageResource(resId);
        }
    }

    private int getWeatherIconRes(String weatherType) {
        switch (weatherType) {
            case "晴": return R.drawable.qing;
            case "多云": return R.drawable.duoyun;
            case "阴": return R.drawable.yin;
            case "雨":
            case "小雨":
            case"阵雨":
            case "中雨":
            case "大雨": return R.drawable.yu;
            case "雪": return R.drawable.xue;
            default: return R.drawable.sun;
        }
    }

    private void setupRecyclerView(List<WeatherItem> items) {
        WeatherAdapter adapter = new WeatherAdapter(items);

        adapter.setOnItemClickListener(new OnWeatherItemClickListener() {
            @Override
            public void onItemClick(WeatherItem item) {
                Intent intent = new Intent(moredays_weather.this, detailsdays.class);
                intent.putExtra("weatherItem", item);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        forecastRecyclerView.setAdapter(adapter);
    }

    private void setupTemperatureChart(List<Entry> highTemps, List<Entry> lowTemps) {
        LineDataSet highTempDataSet = createTemperatureDataSet(highTemps, "高温", R.color.red);
        LineDataSet lowTempDataSet = createTemperatureDataSet(lowTemps, "低温", R.color.blue);

        LineData lineData = new LineData(highTempDataSet, lowTempDataSet);
        tempChart.setData(lineData);

        tempChart.getDescription().setEnabled(false);
        tempChart.getLegend().setEnabled(true);
        tempChart.getAxisRight().setEnabled(false);
        tempChart.getXAxis().setDrawLabels(false);
        tempChart.invalidate();
    }

    private LineDataSet createTemperatureDataSet(List<Entry> entries, String label, int colorRes) {
        LineDataSet dataSet = new LineDataSet(entries, label);
        dataSet.setColor(getResources().getColor(colorRes));
        dataSet.setCircleColor(getResources().getColor(colorRes));
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(3f);
        dataSet.setDrawValues(false);
        return dataSet;
    }

    public static class WeatherItem implements java.io.Serializable {
        public final String day;
        public final String date;
        public final int icon;
        public final String condition;
        public final String dayTemp;
        public final String nightTemp;
        public final String sunrise;
        public final String sunset;
        public final String wind;
        public final String aqi;
        public final String notice;

        public WeatherItem(String day, String date, int icon, String condition, String dayTemp,
                           String nightTemp, String sunrise, String sunset, String wind,
                           String aqi, String notice) {
            this.day = day;
            this.date = date;
            this.icon = icon;
            this.condition = condition;
            this.dayTemp = dayTemp;
            this.nightTemp = nightTemp;
            this.sunrise = sunrise;
            this.sunset = sunset;
            this.wind = wind;
            this.aqi = aqi;
            this.notice = notice;
        }
    }

    private class WeatherAdapter extends RecyclerView.Adapter<WeatherAdapter.ViewHolder> {
        private final List<WeatherItem> dataList;
        private OnWeatherItemClickListener listener;

        WeatherAdapter(List<WeatherItem> dataList) {
            this.dataList = dataList;
        }

        public void setOnItemClickListener(OnWeatherItemClickListener listener) {
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_daily_forecast, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            WeatherItem item = dataList.get(position);
            holder.dayText.setText(item.day);
            holder.dateText.setText(item.date);
            holder.weatherIcon.setImageResource(item.icon);
            holder.dayTemp.setText(item.dayTemp);
            holder.nightTemp.setText(item.nightTemp);
            holder.conditionText.setText(item.condition);

            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(item);
                }
            });
        }

        @Override
        public int getItemCount() {
            return dataList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final TextView dayText, dateText, dayTemp, nightTemp, conditionText;
            final ImageView weatherIcon;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                dayText = itemView.findViewById(R.id.weekText);
                dateText = itemView.findViewById(R.id.dateText);
                weatherIcon = itemView.findViewById(R.id.weatherIcon);
                dayTemp = itemView.findViewById(R.id.highTemp);
                nightTemp = itemView.findViewById(R.id.lowTemp);
                conditionText = itemView.findViewById(R.id.conditionText);
            }
        }
    }
}
