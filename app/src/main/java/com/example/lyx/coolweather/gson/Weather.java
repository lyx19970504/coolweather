package com.example.lyx.coolweather.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Weather {

    public String status;

    public Basic basic;

    @SerializedName("daily_forecast")
    public List<Forecast> forecasts;

    @SerializedName("lifestyle")
    public List<Lifestyle> lifestyles;

    public Now now;

    @SerializedName("update")
    public Update update;


}
