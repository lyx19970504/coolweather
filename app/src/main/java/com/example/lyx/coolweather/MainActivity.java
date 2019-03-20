package com.example.lyx.coolweather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.example.lyx.coolweather.gson.Forecast;
import com.example.lyx.coolweather.gson.Weather;
import com.example.lyx.coolweather.util.HttpUtil;
import com.example.lyx.coolweather.util.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if(preferences.getString("weather",null) != null){
            Intent intent = new Intent(this,WeatherActivity.class);
            startActivity(intent);
            finish();
        }

    }
}
