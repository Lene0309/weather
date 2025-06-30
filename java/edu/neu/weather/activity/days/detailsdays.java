package edu.neu.weather.activity.days;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import edu.neu.weather.R;

public class detailsdays extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detailsdays);

        moredays_weather.WeatherItem item = (moredays_weather.WeatherItem) getIntent().getSerializableExtra("weatherItem");
        if (item == null) {
            finish();
            return;
        }

        // 初始化视�?
        ImageView weatherIcon = findViewById(R.id.iv_weather_icon);
        TextView tempText = findViewById(R.id.tv_temp);
        TextView weatherText = findViewById(R.id.tv_weather_text);
        TextView timeText = findViewById(R.id.tv_time);
        TextView weekdayText = findViewById(R.id.weekday);
        TextView sunriseText = findViewById(R.id.tv_sunrise);
        TextView sunsetText = findViewById(R.id.tv_sunset);
        TextView windText = findViewById(R.id.tv_wind);
        TextView windSpeedText = findViewById(R.id.tv_wind_speed);
        TextView airQualityText = findViewById(R.id.airquality);
        TextView pressureText = findViewById(R.id.tv_pressure);
        TextView noticeText = findViewById(R.id.notice);
        TextView dewText = findViewById(R.id.tv_dew);

        // 设置数据
        weatherIcon.setImageResource(item.icon);
        tempText.setText(item.dayTemp);
        weatherText.setText(item.condition);
        timeText.setText(item.date);
        weekdayText.setText(item.day);
        sunriseText.setText(item.sunrise);
        sunsetText.setText(item.sunset);

        // 处理风力数据
        String[] windParts = item.wind.split("(?<=\\D)(?=\\d)");
        windText.setText(windParts.length > 0 ? windParts[0] : item.wind);
        windSpeedText.setText(windParts.length > 1 ? windParts[1] : "");

        airQualityText.setText("空气质量 " + item.aqi);
        pressureText.setText(item.aqi);
        noticeText.setText("notice");
        dewText.setText(item.notice);
    }
}
