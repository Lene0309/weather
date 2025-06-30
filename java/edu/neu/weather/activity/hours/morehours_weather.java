package edu.neu.weather.activity.hours;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import edu.neu.weather.R;

public class morehours_weather extends AppCompatActivity {

    private static final String HOURLY_API = "https://n546gyyep2.re.qweatherapi.com/v7/weather/24h?location=%s&key=1f82971c596943058e7a8dc48901b4fd";
    private static final String EXTRA_CITY_CODE = "city_code";

    // 中文注释示例
    private RecyclerView hourlyForecastList;
    private HourlyForecastAdapter hourlyForecastAdapter;
    private TextView titleText, currentTemp, currentCondition, currentDate;
    private ImageView currentWeatherIcon;  // 当前天气图标
    private RequestQueue requestQueue;  // 网络请求队列
    private LineChart temperatureChart;  // 温度图表
    private String cityCode = "101070102";  // 默认城市代码（沈阳）

    public static class HourlyForecast implements Serializable {
        private String time;  // 时间
        private int weatherIcon;  // 天气图标
        private String temperature;  // 温度
        private String condition;  // 天气状况
        private String wind;  // 风向风力
        private String windSpeed;       private String precipitation;
        private String precipitationAmount;         private String pressure;
        private String humidity;  // 湿度
        private String cloudCover;  // 云量
        private String dewPoint;  // 露点温度

        public HourlyForecast(String time, int weatherIcon, String temperature,
                              String condition, String wind, String windSpeed,
                              String precipitation, String precipitationAmount,
                              String pressure, String humidity, String cloudCover,
                              String dewPoint) {
            this.time = time;
            this.weatherIcon = weatherIcon;
            this.temperature = temperature;
            this.condition = condition;
            this.wind = wind;
            this.windSpeed = windSpeed;
            this.precipitation = precipitation;
            this.precipitationAmount = precipitationAmount;
            this.pressure = pressure;
            this.humidity = humidity;
            this.cloudCover = cloudCover;
            this.dewPoint = dewPoint;
        }

        // Getter方法
        public String getTime() { return time; }
        public int getWeatherIcon() { return weatherIcon; }
        public String getTemperature() { return temperature; }
        public String getCondition() { return condition; }
        public String getWind() { return wind; }
        public String getWindSpeed() { return windSpeed; }
        public String getPrecipitation() { return precipitation; }
        public String getPrecipitationAmount() { return precipitationAmount; }
        public String getPressure() { return pressure; }
        public String getHumidity() { return humidity; }
        public String getCloudCover() { return cloudCover; }
        public String getDewPoint() { return dewPoint; }
    }

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
        setContentView(R.layout.morehours_weather);

        initViews();
        setupRecyclerView();
        requestQueue = Volley.newRequestQueue(this);

        // 从Intent获取城市代码
        if (getIntent() != null && getIntent().hasExtra(EXTRA_CITY_CODE)) {
            cityCode = getIntent().getStringExtra(EXTRA_CITY_CODE);
        }

        fetchHourlyWeatherData();
    }

   public static String forceUtf8(String input) {
        try {
            byte[] bytes = input.getBytes("ISO-8859-1");
            return new String(bytes, "UTF-8");
        } catch (Exception e) {
            return input;
        }
    }
    private void fetchHourlyWeatherData() {
        String apiUrl = String.format(HOURLY_API, cityCode);

        StringRequest request = new StringRequest(Request.Method.GET, apiUrl,
                response -> {
                    try {
                        // 直接使用原始响应，不进行额外编码转换
                        JSONObject json = new JSONObject(response);
                        JSONArray hourlyArray = json.getJSONArray("hourly");
                        List<HourlyForecast> forecasts = parseHourlyData(hourlyArray);

                        runOnUiThread(() -> {
                            hourlyForecastAdapter.updateData(forecasts);
                            updateTemperatureChart(forecasts);
                            if (!forecasts.isEmpty()) {
                                updateCurrentWeather(forecasts.get(0));
                            }
                        });
                    } catch (Exception e) {
                        Log.e("API Error", "处理API响应时出现", e);
                        showError("数据解析错误");
                    }
                },
                error -> {
                    Log.e("API Error", "网络请求失败", error);
                    showError("网络请求失败");
                }) {
            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                try {
                    // 关键修改：先尝试UTF-8，失败后尝试GBK
                    String parsed;
                    try {
                        parsed = new String(response.data, "UTF-8");
                    } catch (Exception e) {
                        parsed = new String(response.data, "GBK");
                    }
                    return Response.success(parsed, HttpHeaderParser.parseCacheHeaders(response));
                } catch (UnsupportedEncodingException e) {
                    return Response.error(new ParseError(e));
                }
            }
        };

        requestQueue.add(request);
    }


    private void updateBackgroundBasedOnWeather(String weatherType) {
        View rootView = findViewById(android.R.id.content).getRootView();
        int backgroundResId;

        if (weatherType.contains("雨") ) {
            backgroundResId = R.drawable.background_yu;
        } else if (weatherType.contains("阴") ) {
            backgroundResId = R.drawable.background_yin;
        } else {
            backgroundResId = R.drawable.background;
        }

        rootView.setBackgroundResource(backgroundResId);
    }

    private void showError(String message) {

        Log.e("HourlyWeather", message);
        hourlyForecastAdapter.updateData(getHourlyForecastData());
    }

    private void updateCurrentWeather(HourlyForecast current) {
        currentTemp.setText(current.getTemperature());
        currentCondition.setText(current.getCondition());
        currentWeatherIcon.setImageResource(current.getWeatherIcon());
        currentDate.setText(new SimpleDateFormat("yyyy年M月d日", Locale.CHINA).format(new Date()));
        updateBackgroundBasedOnWeather(current.getCondition());
    }

    private List<HourlyForecast> parseHourlyData(JSONArray hourlyArray) throws Exception {
        List<HourlyForecast> forecasts = new ArrayList<>();

        for (int i = 0; i < hourlyArray.length(); i++) {
            JSONObject hourData = hourlyArray.getJSONObject(i);

            String fullTime = hourData.getString("fxTime");
            String time = fullTime.split("T")[1].substring(0, 5);

            String temp = hourData.getString("temp") + "°";
            String condition = hourData.getString("text");
            String windDirection = hourData.getString("windDir");
            String windScale = hourData.getString("windScale");
            String wind = windDirection + " " + windScale ;
            String windSpeed = hourData.optString("windSpeed", "0");
            String precipitation = hourData.optString("pop", "0") + "%";
            String precipitationAmount = hourData.optString("precip", "0.0");
            String pressure = hourData.optString("pressure", "0") + "hPa";
            String humidity = hourData.optString("humidity", "0") + "%";
            String cloudCover = hourData.optString("cloud", "0");
            String dewPoint = hourData.optString("dew", "0");

            int iconRes = getIconResource(condition);
            forecasts.add(new HourlyForecast(time, iconRes, temp, condition, wind,
                    windSpeed, precipitation, precipitationAmount, pressure,
                    humidity, cloudCover, dewPoint));
        }
        return forecasts;
    }

    private int getIconResource(String condition) {
        switch (condition) {
            case "晴": return R.drawable.qing;
            case "阴": return R.drawable.yin;
            case "雨":
            case "小雨":
            case "中雨":
            case "阵雨":
            case "大雨": return R.drawable.yu;
            case "雷阵雨": return R.drawable.lei;
            case "多云": return R.drawable.duoyun;
            case "雾":
            case "雾霾": return R.drawable.wu;
            case "小雪":
            case "中雪":
            case "大雪": return R.drawable.xue;
            default: return R.drawable.duoyun;
        }
    }

    private void initViews() {
        titleText = findViewById(R.id.titleText);
        currentTemp = findViewById(R.id.currentTemp);
        currentCondition = findViewById(R.id.currentCondition);
        currentDate = findViewById(R.id.currentDate);
        currentWeatherIcon = findViewById(R.id.currentWeatherIcon);
        hourlyForecastList = findViewById(R.id.hourlyForecastList);
        temperatureChart = findViewById(R.id.temputureChart);
        setupTemperatureChart();

        titleText.setText("逐时天气预报");
    }

    private void setupTemperatureChart() {
        temperatureChart.getDescription().setEnabled(false);
        temperatureChart.setTouchEnabled(true);
        temperatureChart.setDragEnabled(true);
        temperatureChart.setScaleEnabled(true);
        temperatureChart.setPinchZoom(true);
        temperatureChart.setDrawGridBackground(false);

        XAxis xAxis = temperatureChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(6);

        YAxis leftAxis = temperatureChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        leftAxis.setAxisMaximum(40f);
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(Color.LTGRAY);

        temperatureChart.getAxisRight().setEnabled(false);
    }

    private void updateTemperatureChart(List<HourlyForecast> forecasts) {
        if (forecasts == null || forecasts.isEmpty()) return;

        List<Entry> entries = new ArrayList<>();
        List<String> timeLabels = new ArrayList<>();

        for (int i = 0; i < forecasts.size(); i++) {
            HourlyForecast forecast = forecasts.get(i);
            try {
                float temp = Float.parseFloat(forecast.getTemperature().replace("°", ""));
                entries.add(new Entry(i, temp));
                timeLabels.add(forecast.getTime());
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        XAxis xAxis = temperatureChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(timeLabels));

        LineDataSet dataSet = new LineDataSet(entries, "温度 (°C)");
        dataSet.setColor(Color.RED);
        dataSet.setCircleColor(Color.RED);
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawValues(true);

        LineData lineData = new LineData(dataSet);
        temperatureChart.setData(lineData);
        temperatureChart.invalidate();
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(
                this,
                LinearLayoutManager.HORIZONTAL,
                false);
        hourlyForecastList.setLayoutManager(layoutManager);
        hourlyForecastAdapter = new HourlyForecastAdapter(new ArrayList<>(), this);
        hourlyForecastList.setAdapter(hourlyForecastAdapter);
    }

    private List<HourlyForecast> getHourlyForecastData() {
        List<HourlyForecast> forecasts = new ArrayList<>();

        forecasts.add(new HourlyForecast("现在", R.drawable.qing, "26°", "",
                "南风1级", "11 km/h", "0%", "0.0 mm",
                "1012 hPa", "45%", "10%", "15°C"));

        forecasts.add(new HourlyForecast("14:00", R.drawable.qing, "28°", "",
                "南风2级", "15 km/h", "0%", "0.0 mm",
                "1011 hPa", "42%", "10%", "16°C"));

        return forecasts;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (requestQueue != null) {
            requestQueue.cancelAll(this);
        }
    }

    public class HourlyForecastAdapter extends RecyclerView.Adapter<HourlyForecastAdapter.ViewHolder> {
        private List<HourlyForecast> forecastList;
        private Context context;

        public HourlyForecastAdapter(List<HourlyForecast> forecastList, Context context) {
            this.forecastList = forecastList;
            this.context = context;
        }

        public void updateData(List<HourlyForecast> newData) {
            this.forecastList = newData;
            notifyDataSetChanged();
        }

        @Override
        public ViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            android.view.View view = android.view.LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_hour_forecast, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            HourlyForecast forecast = forecastList.get(position);

            holder.timeText.setText(forecast.getTime());
            holder.weatherIcon.setImageResource(forecast.getWeatherIcon());
            holder.tempText.setText(forecast.getTemperature());
            holder.conditionText.setText(forecast.getCondition());

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, detailshour.class);
                intent.putExtra("hourly_forecast", forecast);
                context.startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return forecastList != null ? forecastList.size() : 0;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView timeText;
            ImageView weatherIcon;
            TextView tempText;
            TextView conditionText;

            public ViewHolder(View itemView) {
                super(itemView);
                timeText = itemView.findViewById(R.id.hour);
                weatherIcon = itemView.findViewById(R.id.weatherIcon);
                tempText = itemView.findViewById(R.id.temperature);
                conditionText = itemView.findViewById(R.id.weatherCondition);
            }
        }
    }
}
