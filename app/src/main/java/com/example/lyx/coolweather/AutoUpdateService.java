package com.example.lyx.coolweather;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import com.example.lyx.coolweather.gson.AQI;
import com.example.lyx.coolweather.gson.Weather;
import com.example.lyx.coolweather.util.HttpUtil;
import com.example.lyx.coolweather.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AutoUpdateService extends Service {
    public AutoUpdateService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        updateWeatherAndAir();
        updateBingPic();
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int eight_Hour = 1000*60*60;
        long triggerAtTime = SystemClock.elapsedRealtime() + eight_Hour;
        Intent i = new Intent(this,AutoUpdateService.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,i,0);
        manager.cancel(pendingIntent);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtTime,pendingIntent);
        return super.onStartCommand(intent, flags, startId);
    }

    public void updateWeatherAndAir(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        final String weather_info = preferences.getString("weather",null);
        if(weather_info != null){
            Weather weather = Utility.handleWeatherResponse(weather_info);
            String weatherId = weather.basic.weatherId;
            String weatherUrl = "https://free-api.heweather.net/s6/weather?location="+weatherId+"&key=a9e489cf72bb4d5981def08c9258a267";
            String airUrl = "https://free-api.heweather.net/s6/air/now?location="+weatherId+"&key=a9e489cf72bb4d5981def08c9258a267";
            HttpUtil.sendOkhttpRequest(weatherUrl, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseText = response.body().string();
                    Weather weather = Utility.handleWeatherResponse(responseText);
                    if(weather != null && "ok".equals(weather.status)){
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                        editor.putString("weather",responseText);
                        editor.apply();
                    }
                }
            });
            HttpUtil.sendOkhttpRequest(airUrl, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseText = response.body().string();
                    AQI aqi = Utility.handleAirResponse(responseText);
                    if(aqi != null && "ok".equals(aqi.status)){
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                        editor.putString("air",responseText);
                        editor.apply();
                    }
                }
            });
        }
    }

    public void updateBingPic(){
        String picUrl = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkhttpRequest(picUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                if(responseText!=null){
                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                    editor.putString("bing_pic",null);
                    editor.apply();
                }
            }
        });
    }


}
