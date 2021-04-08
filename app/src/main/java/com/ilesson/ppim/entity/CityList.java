package com.ilesson.ppim.entity;

/**
 * Copyright 2021 json.cn
 */
import java.util.List;

/**
 * Auto-generated: 2021-03-24 16:32:46
 *
 * @author json.cn (i@json.cn)
 * @website http://www.json.cn/java2pojo/
 */
public class CityList {

    private String code;
    private String name;
    private List<AreaList> areaList;
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

    public void setAreaList(List<AreaList> areaList) {
        this.areaList = areaList;
    }
    public List<AreaList> getAreaList() {
        return areaList;
    }

}