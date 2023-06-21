package org.wwi21seb.vs.group5.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.wwi21seb.vs.group5.Request.AvailabilityRequest;
import org.wwi21seb.vs.group5.UDP.UDPMessage;
import org.wwi21seb.vs.group5.communication.DatabaseConnection;
import org.wwi21seb.vs.group5.model.Car;
import org.wwi21seb.vs.group5.model.Rental;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class RentalDAO {

    private final ObjectMapper mapper;
    private final DateTimeFormatter dateFormatter;

    public RentalDAO() {
        this.mapper = new ObjectMapper();
        this.dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    }

    /**
     * Serialize a list of rentals to a JSON string
     * @param rentals the list of rentals to serialize
     * @return a JSON string containing the serialized rentals
     * @throws JsonProcessingException if the serialization fails
     */
    private String serializeRentals(List<Rental> rentals) throws JsonProcessingException {
        return mapper.writeValueAsString(rentals);
    }

    /**
     * Serialize a list of cars to a JSON string
     * @param cars the list of cars to serialize
     * @return a JSON string containing the serialized cars
     * @throws JsonProcessingException if the serialization fails
     */
    private String serializeCars(List<Car> cars) throws JsonProcessingException {
        return mapper.writeValueAsString(cars);
    }

    /**
     * Get all rentals from the database
     * @return a JSON string containing all rentals
     */
    public String getRentals() {
        PreparedStatement stmt = null;
        List<Rental> rentals = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection(true)) {
            stmt = conn.prepareStatement("SELECT * FROM rentals");
            stmt.executeQuery();

            ResultSet result = stmt.getResultSet();

            while (result.next()) {
                Rental rental = new Rental(
                        result.getObject("rental_id", UUID.class),
                        result.getObject("car_id", UUID.class),
                        result.getDate("start_date"),
                        result.getDate("end_date"),
                        result.getDouble("total_price")
                );

                rentals.add(rental);
            }

            return serializeRentals(rentals);
        } catch (SQLException e) {
            System.out.println("Error while getting rentals: " + e.getMessage());
            throw new RuntimeException(e);
        } catch (JsonProcessingException e) {
            System.out.println("Error while serializing rentals: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Get all available cars from the database
     * @param payload the payload of the UDPMessage containing the availability request
     * @return a JSON string containing all available rentals
     */
    public String getAvailableCars(String payload) {
        PreparedStatement stmt = null;
        List<Car> cars = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection(true)) {
            AvailabilityRequest availabilityRequest = mapper.readValue(payload, AvailabilityRequest.class);

            LocalDate startDate = LocalDate.parse(availabilityRequest.getStartDate(), dateFormatter);
            LocalDate endDate = LocalDate.parse(availabilityRequest.getEndDate(), dateFormatter);

            stmt = conn.prepareStatement("SELECT * FROM cars WHERE capacity >= ? AND car_id NOT IN (SELECT car_id FROM rentals WHERE start_date BETWEEN ? AND ? OR end_date BETWEEN ? AND ?)");
            stmt.setInt(1, availabilityRequest.getNumberOfPersons());
            stmt.setDate(2, java.sql.Date.valueOf(startDate));
            stmt.setDate(3, java.sql.Date.valueOf(endDate));
            stmt.setDate(4, java.sql.Date.valueOf(startDate));
            stmt.setDate(5, java.sql.Date.valueOf(endDate));
            stmt.executeQuery();

            ResultSet resultSet = stmt.getResultSet();
            while (resultSet.next()) {
                Car car = new Car(
                        resultSet.getObject("car_id", java.util.UUID.class),
                        resultSet.getString("model"),
                        resultSet.getString("manufacturer"),
                        resultSet.getInt("capacity"),
                        resultSet.getDouble("price_per_day")
                );

                cars.add(car);
            }

            return serializeCars(cars);
        } catch (SQLException e) {
            System.out.println("Error while getting available cars: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (JsonProcessingException e) {
            System.out.println("Error while serializing available cars: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Reserve a car
     * @param message the UDPMessage containing the reservation request
     * @return true if the reservation was successful, false otherwise
     */
    public boolean reserveCar(UDPMessage message) {
        return false;
    }

}
