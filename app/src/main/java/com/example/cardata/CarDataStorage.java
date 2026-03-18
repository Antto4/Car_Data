package com.example.cardata;

import java.util.ArrayList;
public class CarDataStorage {
    private String city;
    private int year;
    private ArrayList<CarData> carData;
    private static CarDataStorage instance;

    private CarDataStorage() {
        carData = new ArrayList<>();
    }
    public static CarDataStorage getInstance() {
        if (instance == null) {
            instance = new CarDataStorage();
        }
        return instance;
    }
    public ArrayList<CarData> getCarData() {
        return carData;
    }
    public void setCity(String city) {
        this.city = city;
    }
    public void setYear(int year) {
        this.year = year;
    }
    public void clearData() {
        carData.clear();
        city = "";
        year = 0;
    }
    public String getCity() {
        return city;
    }
    public int getYear() {
        return year;
    }
    public void addCarData(CarData data) {
        this.carData.add(data);
    }
}
