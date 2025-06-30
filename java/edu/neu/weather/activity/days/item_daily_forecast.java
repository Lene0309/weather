package edu.neu.weather.activity.days;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import edu.neu.weather.R;
import edu.neu.weather.activity.hours.morehours_weather;

public class item_daily_forecast extends AppCompatActivity {
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
        setContentView(R.layout.item_daily_forecast);     }

    public static class HourlyForecastAdapter extends RecyclerView.Adapter<HourlyForecastAdapter.ViewHolder> {
        private List<morehours_weather.HourlyForecast> forecastList;
        private OnItemClickListener listener;

        public HourlyForecastAdapter(List<morehours_weather.HourlyForecast> forecastList) {
            this.forecastList = forecastList;
        }

        public void updateData(List<morehours_weather.HourlyForecast> newData) {
            this.forecastList = newData;
            notifyDataSetChanged();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_hour_forecast, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            morehours_weather.HourlyForecast forecast = forecastList.get(position);

            holder.timeText.setText(forecast.getTime());
            holder.weatherIcon.setImageResource(forecast.getWeatherIcon());
            holder.tempText.setText(forecast.getTemperature());
            holder.conditionText.setText(forecast.getCondition());

            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(forecast);
                }
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

        public interface OnItemClickListener {
            void onItemClick(morehours_weather.HourlyForecast forecast);
        }

        public void setOnItemClickListener(OnItemClickListener listener) {
            this.listener = listener;
        }
    }
}
