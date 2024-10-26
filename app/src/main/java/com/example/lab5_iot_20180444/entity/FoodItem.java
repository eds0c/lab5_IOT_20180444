package com.example.lab5_iot_20180444.entity;

public class FoodItem {
    private String nombre;
    private int calorias;

    public FoodItem(String nombre, int calorias) {
        this.nombre = nombre;
        this.calorias = calorias;
    }

    public String getNombre() {
        return nombre;
    }

    public int getCalorias() {
        return calorias;
    }
}
