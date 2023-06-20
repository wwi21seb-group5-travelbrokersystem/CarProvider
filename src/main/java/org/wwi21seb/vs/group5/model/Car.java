package org.wwi21seb.vs.group5.model;


import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public class Car {

    @JsonProperty("car_id")
    private UUID id;

    @JsonProperty("model")
    private String model;

    @JsonProperty("manufacturer")
    private String manufacturer;

    @JsonProperty("capacity")
    private int capacity;

    @JsonProperty("price_per_day")
    private double pricePerDay;

    public Car(UUID id, String model, String manufacturer, int capacity, double pricePerDay) {
        this.id = id;
        this.model = model;
        this.manufacturer = manufacturer;
        this.capacity = capacity;
        this.pricePerDay = pricePerDay;
    }

    public UUID getId() {
        return id;
    }

    public String getModel() {
        return model;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public int getCapacity() {
        return capacity;
    }

    public double getPricePerDay() {
        return pricePerDay;
    }
}
