package org.wwi21seb.vs.group5.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.wwi21seb.vs.group5.Model.Car;
import org.wwi21seb.vs.group5.Model.Rental;
import org.wwi21seb.vs.group5.Request.AvailabilityRequest;
import org.wwi21seb.vs.group5.Request.ReservationRequest;
import org.wwi21seb.vs.group5.Request.TransactionResult;
import org.wwi21seb.vs.group5.communication.DatabaseConnection;

import java.sql.Connection;
import java.sql.Date;
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

        try (Connection conn = DatabaseConnection.getConnection()) {
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

        try (Connection conn = DatabaseConnection.getConnection()) {
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
     * @param request the payload of the UDPMessage containing the reservation request
     * @param transactionId the transaction ID of the reservation
     * @return a JSON string containing the reservation result
     */
    public UUID reserveCar(ReservationRequest request, UUID transactionId) {
        PreparedStatement stmt = null;
        UUID bookingId = UUID.randomUUID();

        try (Connection conn = DatabaseConnection.getConnection()) {
            // SELECT CAR TO GET DAILY PRICE
            stmt = conn.prepareStatement("SELECT price_per_day FROM cars WHERE car_id = ?");
            stmt.setObject(1, request.getResourceId());
            stmt.executeQuery();

            ResultSet resultSet = stmt.getResultSet();
            resultSet.next();
            double dailyPrice = resultSet.getDouble("price_per_day");

            // CHECK IF CAR IS AVAILABLE
            stmt = conn.prepareStatement("SELECT * FROM rentals WHERE car_id = ? AND start_date BETWEEN ? AND ? OR end_date BETWEEN ? AND ?");
            stmt.setObject(1, request.getResourceId());
            stmt.setDate(2, Date.valueOf(request.getStartDate()));
            stmt.setDate(3, Date.valueOf(request.getEndDate()));
            stmt.setDate(4, Date.valueOf(request.getStartDate()));
            stmt.setDate(5, Date.valueOf(request.getEndDate()));
            stmt.executeQuery();

            ResultSet result = stmt.getResultSet();
            if (result.next()) {
                return null;
            }

            // PREPARE RENTAL
            LocalDate startDate = LocalDate.parse(request.getStartDate(), dateFormatter);
            LocalDate endDate = LocalDate.parse(request.getEndDate(), dateFormatter);
            double totalPrice = dailyPrice * (startDate.until(endDate).getDays() + 1);

            stmt = conn.prepareStatement("INSERT INTO rentals (rental_id, car_id, start_date, end_date, total_price, is_confirmed) VALUES (?, ?, ?, ?, ?, ?)");
            stmt.setObject(1, bookingId);
            stmt.setObject(2, request.getResourceId());
            stmt.setDate(3, Date.valueOf(request.getStartDate()));
            stmt.setDate(4, Date.valueOf(request.getEndDate()));
            stmt.setDouble(5, totalPrice);
            stmt.setBoolean(6, false);
            stmt.executeUpdate();
            return bookingId;
        } catch (SQLException e) {
            System.out.println("SQL Exception: " + e.getMessage());
            return null;
        }
    }

    /**
     * Confirm a reservation
     * @param payload the payload of the UDPMessage containing the rental id
     * @return a boolean indicating whether the reservation was confirmed
     */
    public boolean confirmRental(String payload) {
        PreparedStatement stmt = null;

        /*

        try (Connection conn = DatabaseConnection.getConnection()) {
            PrepareResult prepareResult = mapper.readValue(payload, PrepareResult.class);
            stmt = conn.prepareStatement("UPDATE rentals SET is_confirmed = true WHERE rental_id = ?");
            stmt.setObject(1, prepareResult.getResourceId(), java.sql.Types.OTHER);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("SQL Exception: " + e.getMessage());
            return false;
        } catch (JsonProcessingException e) {
            System.out.println("JSON Exception: " + e.getMessage());
            return false;
        }

         */

        return false;
    }

    public boolean abortRental(String payload) {
        PreparedStatement stmt = null;

        /*

        try (Connection conn = DatabaseConnection.getConnection()) {
            PrepareResult prepareResult = mapper.readValue(payload, PrepareResult.class);

            stmt = conn.prepareStatement("DELETE FROM rentals WHERE rental_id = ?");
            stmt.setObject(1, prepareResult.getResourceId(), java.sql.Types.OTHER);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("SQL Exception: " + e.getMessage());
            return false;
        } catch (JsonProcessingException e) {
            System.out.println("JSON Exception: " + e.getMessage());
            return false;
        }

         */

        return false;
    }

}
