package org.wwi21seb.vs.group5.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.wwi21seb.vs.group5.communication.DatabaseConnection;
import org.wwi21seb.vs.group5.model.Car;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

public class CarDAO {

    private final ObjectMapper mapper;

    public CarDAO() {
        this.mapper = new ObjectMapper();
    }

    private String serializeCar(Car car) throws JsonProcessingException {
        return mapper.writeValueAsString(car);
    }

    private Car deserializeCar(String carJson) throws JsonProcessingException {
        return mapper.readValue(carJson, Car.class);
    }

    public void insertCar(String model, String manufacturer, int capacity, double pricePerDay) {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DatabaseConnection.getConnection(true);

            stmt = conn.prepareStatement("INSERT INTO cars (car_id, model, manufacturer, capacity, price_per_day) VALUES (?, ?, ?, ?, ?)");
            stmt.setObject(1, UUID.randomUUID(), java.sql.Types.OTHER);
            stmt.setString(2, model);
            stmt.setString(3, manufacturer);
            stmt.setInt(4, capacity);
            stmt.setDouble(5, pricePerDay);

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteCar(UUID carId) {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DatabaseConnection.getConnection(true);

            stmt = conn.prepareStatement("DELETE FROM cars WHERE car_id = ?");
            stmt.setObject(1, carId, java.sql.Types.OTHER);

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void getCar(UUID carId) {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DatabaseConnection.getConnection(true);

            stmt = conn.prepareStatement("SELECT * FROM cars WHERE car_id = ?");
            stmt.setObject(1, carId, java.sql.Types.OTHER);

            stmt.executeQuery();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
