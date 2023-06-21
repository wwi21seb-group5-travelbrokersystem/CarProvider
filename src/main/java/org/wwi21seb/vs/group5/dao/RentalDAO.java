package org.wwi21seb.vs.group5.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.wwi21seb.vs.group5.TwoPhaseCommit.TransactionContext;
import org.wwi21seb.vs.group5.UDP.UDPMessage;
import org.wwi21seb.vs.group5.communication.DatabaseConnection;
import org.wwi21seb.vs.group5.model.Rental;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class RentalDAO implements TransactionContext {

    private final ObjectMapper mapper;

    public RentalDAO() {
        this.mapper = new ObjectMapper();
    }

    private String serializeRentals(List<Rental> rentals) throws JsonProcessingException {
        return mapper.writeValueAsString(rentals);
    }

    public String getRentals() {
        PreparedStatement stmt = null;
        List<Rental> rentals = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection(true)) {
            stmt = conn.prepareStatement("SELECT * FROM rentals");
            stmt.executeQuery();

            ResultSet result = stmt.getResultSet();

            while (result.next()) {
                Rental rental = new Rental(
                        result.getObject("id", UUID.class),
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

    public void getAvailableCars(UDPMessage message) {
        PreparedStatement stmt = null;
        List<Rental> rentals = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection(true)) {

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean reserveCar(UDPMessage message) {
        return false;
    }

    @Override
    public void commit() {

    }

    @Override
    public void abort() {

    }
}
