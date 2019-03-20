package com.example.lyx.coolweather.gson;

import com.google.gson.annotations.SerializedName;

public class Now {

    @SerializedName("cond_text")
    public String info;

    @SerializedName("hum")
    public String humidity;

    @SerializedName("tmp")
    public String temperature;

    @SerializedName("wind_dir")
    public String windDirection;
}
