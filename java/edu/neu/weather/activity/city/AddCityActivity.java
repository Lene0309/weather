package edu.neu.weather.activity.city;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;
import java.util.List;

import edu.neu.weather.R;

public class AddCityActivity extends AppCompatActivity {

    private RecyclerView cityRecyclerView;
    private CityAdapter cityAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_city);

        cityRecyclerView = findViewById(R.id.cityRecyclerView);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        cityRecyclerView.setLayoutManager(layoutManager);

        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.grid_spacing);
        cityRecyclerView.addItemDecoration(new GridSpacingItemDecoration(3, spacingInPixels, true));

        List<City> recommendedCities = Arrays.asList(
                new City("北京", "101010100"),
                new City("上海", "101020100"),
                new City("广州", "101280101"),
                new City("深圳", "101280601"),
                new City("成都", "101270101"),
                new City("杭州", "101210101"),
                new City("武汉", "101200101"),
                new City("重庆", "101040100"),
                new City("西安", "101110101"),
                new City("苏州", "101190401"),
                new City("南京", "101190101"),
                new City("天津", "101030100")
        );

        cityAdapter = new CityAdapter(recommendedCities, new CityAdapter.OnCityClickListener() {
            @Override
            public void onCityClick(City city) {
                // Return the selected city to CityManagementActivity
                Intent resultIntent = new Intent();
                resultIntent.putExtra("city_name", city.getName());
                resultIntent.putExtra("city_code", city.getCode());
                setResult(RESULT_OK, resultIntent);
                finish();
            }

            @Override
            public void onDeleteClick(City city) {
                // Not needed in add city activity
            }
        });
        cityRecyclerView.setAdapter(cityAdapter);
    }

    public static class CityAdapter extends RecyclerView.Adapter<CityAdapter.CityViewHolder> {

        private List<City> cityList;
        private OnCityClickListener listener;

        public interface OnCityClickListener {
            void onCityClick(City city);
            void onDeleteClick(City city);
        }

        public CityAdapter(List<City> cityList, OnCityClickListener listener) {
            this.cityList = cityList;
            this.listener = listener;
        }

        @NonNull
        @Override
        public CityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_city, parent, false);
            return new CityViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CityViewHolder holder, int position) {
            City city = cityList.get(position);
            holder.tvCityName.setText(city.getName());

            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCityClick(city);
                }
            });

            // Hide delete button in add city activity
            holder.ivDelete.setVisibility(View.GONE);
        }

        @Override
        public int getItemCount() {
            return cityList != null ? cityList.size() : 0;
        }

        static class CityViewHolder extends RecyclerView.ViewHolder {
            TextView tvCityName;
            ImageView ivDelete;

            public CityViewHolder(@NonNull View itemView) {
                super(itemView);
                tvCityName = itemView.findViewById(R.id.Addcityname);
                ivDelete = itemView.findViewById(R.id.delete);
            }
        }
    }
}
