package com.example.lyx.coolweather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.lyx.coolweather.gson.AQI;
import com.example.lyx.coolweather.gson.Forecast;
import com.example.lyx.coolweather.gson.Lifestyle;
import com.example.lyx.coolweather.gson.Weather;
import com.example.lyx.coolweather.util.HttpUtil;
import com.example.lyx.coolweather.util.Utility;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    private static final String TAG = "WeatherActivity";

    private ScrollView weatherLayout;
    private TextView titleCity;
    private TextView titleUpdateTime;
    private TextView degreeText;
    private TextView weatherInfoText;
    private LinearLayout forecastLayout;
    private TextView air_quality;
    private TextView pm25Text;
    private TextView pm10Text;
    private TextView no2Text;
    private TextView so2Text;
    private TextView o3Text;
    private TextView coText;
    private TextView comfortText;
    private TextView dressText;
    private TextView fluText;
    private TextView sportText;
    private TextView travelText;
    private TextView uvText;
    private TextView carWashText;
    private TextView airText;

    public static final String WEATHER_ID = "weather_id";

    private ImageView bingPicImg;
    public SwipeRefreshLayout swipeRefresh;
    public DrawerLayout drawerLayout;
    private Button nav_button;
    public String weatherId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);


        //setting the StatusBar hidden
        if(Build.VERSION.SDK_INT >= 21){
            View view = getWindow().getDecorView();
            view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        //loading bing image from the Internet or Shared preferences
        bingPicImg = (ImageView) findViewById(R.id.bing_pic_img);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String bing_pic = preferences.getString("bing_pic",null);
        if(bing_pic != null){
            Glide.with(this).load(bing_pic).into(bingPicImg);
        }else{
            loadBing_pic();
        }

        weatherLayout = (ScrollView) findViewById(R.id.weather_layout);
        titleCity = (TextView) findViewById(R.id.title_city);
        titleUpdateTime = (TextView) findViewById(R.id.title_update_time);
        degreeText = (TextView) findViewById(R.id.degree_text);
        weatherInfoText = (TextView) findViewById(R.id.weather_info_text);
        forecastLayout = (LinearLayout) findViewById(R.id.forecast_layout);
        air_quality = (TextView) findViewById(R.id.air_quality);
        pm25Text = (TextView) findViewById(R.id.pm25_text);
        pm10Text = (TextView) findViewById(R.id.pm10_text);
        no2Text = (TextView) findViewById(R.id.no2_text);
        so2Text = (TextView) findViewById(R.id.so2_text);
        o3Text = (TextView) findViewById(R.id.o3_text);
        coText = (TextView) findViewById(R.id.co_text);
        comfortText = (TextView) findViewById(R.id.comfort_text);
        dressText = (TextView) findViewById(R.id.dress_text);
        fluText = (TextView) findViewById(R.id.flu_text);
        sportText = (TextView) findViewById(R.id.sport_text);
        travelText = (TextView) findViewById(R.id.travel_text);
        uvText = (TextView) findViewById(R.id.uv_text);
        carWashText = (TextView) findViewById(R.id.car_wash_text);
        airText = (TextView) findViewById(R.id.air_text);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather",null);
        String airString = prefs.getString("air",null);
        if(weatherString != null && airString != null){
            Weather weather = Utility.handleWeatherResponse(weatherString);
            weatherId = weather.basic.weatherId;
            AQI aqi = Utility.handleAirResponse(airString);
            showWeatherInfo(weather);
            showAirInfo(aqi);
        }else {
            weatherId = getIntent().getStringExtra(WEATHER_ID);
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(weatherId);
        }

        //swipe to update the data
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(weatherId);
            }
        });

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        nav_button = (Button) findViewById(R.id.nav_button);
        nav_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
    }


    public void requestWeather(final String weatherId){
        String weatherUrl = "https://free-api.heweather.net/s6/weather?location="+weatherId+"&key=a9e489cf72bb4d5981def08c9258a267";
        HttpUtil.sendOkhttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this,"获取天气信息失败",Toast.LENGTH_SHORT).show();
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(weather != null && weather.status.equals("ok")){
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather",responseText);
                            editor.apply();
                            showWeatherInfo(weather);
                        }else{
                            Toast.makeText(WeatherActivity.this,"获取天气失败",Toast.LENGTH_SHORT).show();
                        }
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        });
        requestAir(weatherId);
    }

    public void requestAir(String weatherId){
        String airUrl = "https://free-api.heweather.net/s6/air/now?location="+weatherId+"&key=a9e489cf72bb4d5981def08c9258a267";
        HttpUtil.sendOkhttpRequest(airUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(WeatherActivity.this,"无法获取环境数据",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final AQI aqi = Utility.handleAirResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(aqi != null && aqi.status.equals("ok")){
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("air",responseText);
                            editor.apply();
                            showAirInfo(aqi);
                        }else {
                            Toast.makeText(WeatherActivity.this,"获取环境数据失败",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    public void showWeatherInfo(Weather weather){
        String cityName = weather.basic.cityName;
        String updateTime = weather.update.updateTime;
        String degree = weather.now.temperature + "℃";
        String weatherInfo = weather.now.info;
        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        forecastLayout.removeAllViews();
        for(Forecast forecast :weather.forecasts){
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item,forecastLayout,false);
            TextView dateText = (TextView) view.findViewById(R.id.date_text);
            TextView infoText = (TextView) view.findViewById(R.id.info_text);
            TextView maxText = (TextView) view.findViewById(R.id.max_text);
            TextView minText = (TextView) view.findViewById(R.id.min_text);
            dateText.setText(forecast.date);
            infoText.setText("白天: "+forecast.dayConf+",夜晚: "+forecast.nightConf);
            maxText.setText(forecast.tmp_max);
            minText.setText(forecast.tmp_min);
            forecastLayout.addView(view);
        }

        comfortText.setText("舒适度: " + weather.lifestyles.get(0).feeling+ "," +weather.lifestyles.get(0).info);
        dressText.setText("穿衣指数: "+weather.lifestyles.get(1).feeling+","+weather.lifestyles.get(1).info);
        fluText.setText("感冒指数: "+weather.lifestyles.get(2).feeling+","+weather.lifestyles.get(2).info);
        sportText.setText("运动指数: "+weather.lifestyles.get(3).feeling+","+weather.lifestyles.get(3).info);
        travelText.setText("旅行指数: "+weather.lifestyles.get(4).feeling+","+weather.lifestyles.get(4).info);
        uvText.setText("紫外线指数: "+weather.lifestyles.get(5).feeling+","+weather.lifestyles.get(5).info);
        carWashText.setText("洗车指数: "+weather.lifestyles.get(6).feeling+","+weather.lifestyles.get(6).info);
        airText.setText("空气指数: "+weather.lifestyles.get(7).feeling+","+weather.lifestyles.get(7).info);
        weatherLayout.setVisibility(View.VISIBLE);
        Intent intent = new Intent(this,AutoUpdateService.class);
        startService(intent);//start auto updated service
    }

    public void showAirInfo(AQI aqi){
        air_quality.setText("空气质量: "+aqi.air_now_city.air_quality + "                 " + "AQI指数:"+aqi.air_now_city.aqi);
        pm25Text.setText(aqi.air_now_city.pm25);
        pm10Text.setText(aqi.air_now_city.pm10);
        so2Text.setText(aqi.air_now_city.so2);
        no2Text.setText(aqi.air_now_city.no2);
        coText.setText(aqi.air_now_city.co);
        o3Text.setText(aqi.air_now_city.o3);
    }

    public void loadBing_pic(){
        String downloadUrl = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkhttpRequest(downloadUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(WeatherActivity.this,"cannot get the image",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic",bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
                    }
                });
            }
        });
    }
}
