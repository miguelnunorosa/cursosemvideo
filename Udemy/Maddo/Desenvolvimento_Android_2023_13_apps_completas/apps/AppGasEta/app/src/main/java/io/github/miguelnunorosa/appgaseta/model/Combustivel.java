package io.github.miguelnunorosa.appgaseta.model;

public class Combustivel {

    private String fuelType, suggestion;
    private double fuelPrice;



    public String getFuelType() {
        return fuelType;
    }


    public void setFuelType(String fuelType) {
        this.fuelType = fuelType;
    }


    public String getSuggestion() {
        return suggestion;
    }


    public void setSuggestion(String suggestion) {
        this.suggestion = suggestion;
    }


    public double getFuelPrice() {
        return fuelPrice;
    }


    public void setFuelPrice(double fuelPrice) {
        this.fuelPrice = fuelPrice;
    }


}
