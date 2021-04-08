package com.ilesson.ppim.entity;

import java.util.List;

public class ProvinceList {
    private String code;
    private String name;
    private List<CityList> cityList;
    public void setCode(String code) {
        this.code = code;
    }
    public String getCode() {
        return code;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }

    public void setCityList(List<CityList> cityList) {
        this.cityList = cityList;
    }
    public List<CityList> getCityList() {
        return cityList;
    }
}
