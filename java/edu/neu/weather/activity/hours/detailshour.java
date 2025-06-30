package edu.neu.weather.activity.hours;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import edu.neu.weather.R;

public class detailshour extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detailshours);


        // 从Intent获取传递过来的逐时预报数据
        morehours_weather.HourlyForecast forecast =
                (morehours_weather.HourlyForecast) getIntent().getSerializableExtra("hourly_forecast");

        String tempStr = forecast.getTemperature();
        Log.d("DetailshourDebug", "Temperature: " + tempStr);
        ((TextView) findViewById(R.id.tv_temp)).setText(tempStr);
        if (forecast != null) {
            // 初始化视图并设置数据
            ((TextView) findViewById(R.id.tv_temp)).setText(forecast.getTemperature());
            ((TextView) findViewById(R.id.tv_weather_text)).setText(forecast.getCondition());
            ((TextView) findViewById(R.id.tv_time)).setText(forecast.getTime());
            ((TextView) findViewById(R.id.tv_wind)).setText(forecast.getWind());
            ((TextView) findViewById(R.id.tv_wind_speed)).setText(forecast.getWindSpeed() + " km/h");
            ((TextView) findViewById(R.id.tv_humidity)).setText(forecast.getHumidity());
            ((TextView) findViewById(R.id.tv_precip_prob)).setText(forecast.getPrecipitation());
            ((TextView) findViewById(R.id.tv_precip)).setText(forecast.getPrecipitationAmount() + " mm");
            ((TextView) findViewById(R.id.tv_pressure)).setText(forecast.getPressure());
            ((TextView) findViewById(R.id.tv_cloud)).setText(forecast.getCloudCover() + "%");
            ((TextView) findViewById(R.id.tv_dew)).setText(forecast.getDewPoint() + "°C");
            ((ImageView) findViewById(R.id.iv_weather_icon)).setImageResource(forecast.getWeatherIcon());

            // 设置日出日落时间（可以从API获取或使用默认值）
            ((TextView) findViewById(R.id.tv_sunrise)).setText("06:00");
            ((TextView) findViewById(R.id.tv_sunset)).setText("18:00");
        }
    }
}
