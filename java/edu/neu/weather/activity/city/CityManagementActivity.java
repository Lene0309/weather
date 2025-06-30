package edu.neu.weather.activity.city;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;
import edu.neu.weather.R;


public class CityManagementActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_ADD_CITY = 1001;
    private static final String PREFS_NAME = "city_prefs";
    private static final String KEY_CITY_LIST = "city_list";

    private ImageView backButton, addCityButton;
    private RecyclerView cityRecyclerView;
    private CityListAdapter cityListAdapter;
    private List<City> cityList = new ArrayList<>();
    private RequestQueue requestQueue;
    private Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String cityJson = prefs.getString(KEY_CITY_LIST, null);
        if (cityJson != null) {
            cityList = gson.fromJson(cityJson, new TypeToken<List<City>>(){}.getType());
        } else {
            cityList = new ArrayList<>();
        }
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
        setContentView(R.layout.activity_city_management);

        initViews();
        setupBackButton();
        setupAddCityButton();
        initCityRecyclerView();

        // Add default city if empty
        if (cityList.isEmpty()) {
            cityList.add(new City("沈阳", "101070102"));
            cityListAdapter.notifyDataSetChanged();
        }

        requestQueue = Volley.newRequestQueue(this);
    }

    private void initViews() {
        backButton = findViewById(R.id.back_button);
        addCityButton = findViewById(R.id.addbutton);
        cityRecyclerView = findViewById(R.id.cityRecyclerView);
    }

    private void setupBackButton() {
        backButton.setOnClickListener(v -> finish());
    }

    private void setupAddCityButton() {
        addCityButton.setOnClickListener(v -> {
            Intent intent = new Intent(CityManagementActivity.this, AddCityActivity.class);
            startActivityForResult(intent, REQUEST_CODE_ADD_CITY);
        });
    }

    private void initCityRecyclerView() {
        cityRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        cityListAdapter = new CityListAdapter(cityList, new CityListAdapter.OnCityActionListener(){
            @Override
            public void onCitySelected(City city) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("city_code", city.getCode());
                resultIntent.putExtra("city_name", city.getName());
                setResult(RESULT_OK, resultIntent);
                finish();
            }
            @Override
            public void onCityDeleted(City city) {
                cityList.remove(city);
                cityListAdapter.notifyDataSetChanged();
                saveCityList();
                Toast.makeText(CityManagementActivity.this, "已删�? " + city.getName(), Toast.LENGTH_SHORT).show();
                if (cityList.isEmpty()) {
                    cityList.add(new City("沈阳", "101070102"));
                    cityListAdapter.notifyDataSetChanged();
                    saveCityList();
                }
            }
        });
        cityRecyclerView.setAdapter(cityListAdapter);
        cityListAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                Log.d("CityManagement", "适配器数据已更新，项目数: " + cityListAdapter.getItemCount());
            }
        });
    }

    private void saveCityList() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putString(KEY_CITY_LIST, gson.toJson(cityList)).apply();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD_CITY && resultCode == RESULT_OK && data != null) {
            try {
                String cityName = data.getStringExtra("city_name");
                String cityCode = data.getStringExtra("city_code");
                if (cityName != null && cityCode != null) {
                    City newCity = new City(cityName, cityCode);
                    boolean exists = false;
                    for (City city : cityList) {
                        if (city.getCode().equals(newCity.getCode())) {
                            exists = true;
                            break;
                        }
                    }
                    if (exists) {
                        Toast.makeText(this, newCity.getName() + " 已在城市列表", Toast.LENGTH_SHORT).show();
                    } else {
                        cityList.add(newCity);
                        cityListAdapter.notifyDataSetChanged();
                        saveCityList();
                        Toast.makeText(this, "已添加城市 " + newCity.getName(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e("CityManagement", "获取新城市名或代码为null");
                    // Toast������ɾ��
                }
            } catch (Exception e) {
                Log.e("CityManagement", "解析城市对象失败", e);
                // Toast������ɾ��
            }
        }
    }

    public static class CityListAdapter extends RecyclerView.Adapter<CityListAdapter.CityViewHolder> {

        private List<City> cityList;
        private OnCityActionListener listener;

        public interface OnCityActionListener {
            void onCitySelected(City city);
            void onCityDeleted(City city);
        }

        public CityListAdapter(List<City> cityList, OnCityActionListener listener) {
            this.cityList = cityList;
            this.listener = listener;
        }

        @NonNull
        @Override
        public CityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_manage_city, parent, false);
            return new CityViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CityViewHolder holder, int position) {
            City city = cityList.get(position);
            holder.tvCityName.setText(city.getName());

            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCitySelected(city);
                }
            });

            holder.ivDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCityDeleted(city);
                }
            });
        }

        @Override
        public int getItemCount() {
            return cityList.size();
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
