package com.sok.mphone.dataEntity;

import java.util.HashMap;

/**
 * Created by user on 2016/12/19.
 */

public class DataListEntity {
    protected HashMap<String,String> map=new HashMap<>();
    public String GetStringDefualt(String key, String defualtValue) {
        try {
            String object = map.get(key);
            if (object == null) {
                return defualtValue;
            } else {
                return object;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return defualtValue;
        }
    }
    public String GetStringDefualt(String key) {
        return GetStringDefualt(key,"");
    }
    public int GetIntDefualt(String key, int defualtValue) {
        try {
            Object object = map.get(key);
            if (object == null) {
                return defualtValue;
            } else {
                return Integer.parseInt(object.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return defualtValue;
        }
    }
    public double GetDoubleDefualt(String key, double defualtValue) {
        try {
            Object object = map.get(key);
            if (object == null) {
                return defualtValue;
            } else {
                return Double.parseDouble(object.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return defualtValue;
        }
    }
    public void put(String key, String value) {
        map.put(key, value);
    }
    public void setMap(HashMap<String, String> map) {
        this.map = map;
    }
    public HashMap<String,String> getMap(){
        return this.map;
    }
    public void clear(){
        map.clear();
    }
}
