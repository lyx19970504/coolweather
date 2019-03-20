package com.example.lyx.coolweather.gson;

import com.google.gson.annotations.SerializedName;

public class Forecast {

    @SerializedName("date")
    public String date;

    @SerializedName("tmp_max")
    public String tmp_max;

    @SerializedName("tmp_min")
    public String tmp_min;

    @SerializedName("cond_txt_d")
    public String dayConf;

    @SerializedName("cond_txt_n")
    public String nightConf;

}
