package com.example.lyx.coolweather;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lyx.coolweather.db.City;
import com.example.lyx.coolweather.db.County;
import com.example.lyx.coolweather.db.Province;
import com.example.lyx.coolweather.util.HttpUtil;
import com.example.lyx.coolweather.util.Utility;

import org.litepal.LitePal;
import org.litepal.crud.DataSupport;
import org.litepal.crud.LitePalSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChooseAreaFragment extends Fragment {

    public static final int LEVEL_PROVINCE = 0;

    public static final int LEVEL_CITY = 1;

    public static final int LEVEL_COUNTY = 2;

    private ProgressDialog progressDialog;

    private TextView title_text;

    private Button back_button;

    private ListView listView;

    private ArrayAdapter adapter;

    private List<String> dataList = new ArrayList<>();

    private List<Province> provinces = new ArrayList<>();

    private List<City> cities = new ArrayList<>();

    private List<County> counties = new ArrayList<>();

    private Province selectedProvince;

    private City selectCity;

    private int currentLevel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area,container,false);
        title_text = (TextView) view.findViewById(R.id.title_text);
        back_button = (Button) view.findViewById(R.id.back_button);
        listView = (ListView) view.findViewById(R.id.list_view);
        adapter = new ArrayAdapter<>(getContext(),android.R.layout.simple_list_item_1,dataList);
        listView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(currentLevel == LEVEL_PROVINCE){
                    selectedProvince = provinces.get(position);
                    queryCities();
                }else if(currentLevel == LEVEL_CITY) {
                    selectCity = cities.get(position);
                    queryCounties();
                }else if(currentLevel == LEVEL_COUNTY){
                    String weatherId = counties.get(position).getWeatherId();
                    if(getActivity() instanceof MainActivity) {
                        Intent intent = new Intent(getActivity(), WeatherActivity.class);
                        intent.putExtra(WeatherActivity.WEATHER_ID, weatherId);
                        startActivity(intent);
                        getActivity().finish();
                    }else if(getActivity() instanceof WeatherActivity){
                        WeatherActivity weatherActivity = (WeatherActivity) getActivity();
                        weatherActivity.drawerLayout.closeDrawers();
                        weatherActivity.swipeRefresh.setRefreshing(true);
                        weatherActivity.weatherId = weatherId;
                        weatherActivity.requestWeather(weatherId);
                    }
                }
            }
        });
        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentLevel == LEVEL_COUNTY){
                    queryCities();
                }else if(currentLevel == LEVEL_CITY){
                    queryProvinces();
                }
            }
        });
        queryProvinces();
    }

    private void queryProvinces(){
        title_text.setText("中国");
        back_button.setVisibility(View.GONE);
        provinces = LitePal.findAll(Province.class);
        if(provinces.size() > 0){
            dataList.clear();
            for(Province province : provinces){
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_PROVINCE;
        }else{
            String address = "http://guolin.tech/api/china/";
            queryFromServer(address,"province");
        }
    }

    private void queryCities(){
        title_text.setText(selectedProvince.getProvinceName());
        back_button.setVisibility(View.VISIBLE);
        cities = LitePal.where("provinceid = ?",String.valueOf(selectedProvince.getId())).find(City.class);
        if(cities.size() > 0){
            dataList.clear();
            for(City city:cities){
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_CITY;
        }else{
            String address = "http://guolin.tech/api/china/"+selectedProvince.getProvinceCode();
            queryFromServer(address,"city");
        }
    }

    private void queryCounties(){
        title_text.setText(selectCity.getCityName());
        back_button.setVisibility(View.VISIBLE);
        counties = LitePal.where("cityid = ?",String.valueOf(selectCity.getId())).find(County.class);
        if(counties.size() >0){
            dataList.clear();
            for(County county:counties){
                dataList.add(county.getCountyNme());
                adapter.notifyDataSetChanged();
                listView.setSelection(0);
                currentLevel = LEVEL_COUNTY;
            }
        }else {
            String address = "http://guolin.tech/api/china/"+selectedProvince.getProvinceCode()+"/"+selectCity.getCityCode();
            queryFromServer(address,"county");
        }
    }

    public void queryFromServer(String address, final String type){
        showProgressDialog();
        HttpUtil.sendOkhttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getActivity(),"Loading Failed",Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String response_text = response.body().string();
                boolean result = false;
                if("province".equals(type)){
                    result = Utility.handleProvinceResponse(response_text);
                }else if("city".equals(type)){
                    result = Utility.handleCityResponse(response_text,selectedProvince.getId());
                }else if("county".equals(type)){
                    result = Utility.handleCountyResponse(response_text,selectCity.getId());
                }
                if(result){
                    closeProgressDialog();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if("province".equals(type)){
                                queryProvinces();
                            }else if("city".equals(type)){
                                queryCities();
                            }else if("county".equals(type)){
                                queryCounties();
                            }
                        }
                    });
                }
            }
        });
    }

    public void showProgressDialog(){
        if(progressDialog == null){
            progressDialog = new ProgressDialog(getContext());
            progressDialog.setMessage("Loading...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    public void closeProgressDialog(){
        if(progressDialog != null){
            progressDialog.dismiss();
        }
    }

}
