package org.wwi21seb.vs.group5.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;
import java.util.UUID;

public class Rental {

    @JsonProperty("rental_id")
    private UUID id;

    @JsonProperty("car_id")
    private UUID car_id;

    @JsonProperty("start_date")
    private Date start_date;

    @JsonProperty("end_date")
    private Date end_date;

    @JsonProperty("total_price")
    private double total_price;

    public Rental(UUID id, UUID car_id, Date start_date, Date end_date, double total_price) {
        this.id = id;
        this.car_id = car_id;
        this.start_date = start_date;
        this.end_date = end_date;
        this.total_price = total_price;
    }

    public UUID getId() {
        return id;
    }

    public UUID getCar_id() {
        return car_id;
    }

    public Date getStart_date() {
        return start_date;
    }

    public Date getEnd_date() {
        return end_date;
    }

    public double getTotal_price() {
        return total_price;
    }

}
