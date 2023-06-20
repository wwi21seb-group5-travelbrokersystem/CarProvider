package org.wwi21seb.vs.group5.service;

import org.wwi21seb.vs.group5.TwoPhaseCommit.Participant;
import org.wwi21seb.vs.group5.UDP.Operation;
import org.wwi21seb.vs.group5.UDP.UDPMessage;
import org.wwi21seb.vs.group5.dao.CarDAO;
import org.wwi21seb.vs.group5.dao.RentalDAO;
import org.wwi21seb.vs.group5.model.Rental;

import java.util.List;

public class RentalService implements Participant {

    private final RentalDAO rentalDAO;

    public RentalService() {
        this.rentalDAO = new RentalDAO();
    }

    @Override
    public void prepare(UDPMessage message) {

    }

    @Override
    public void commit(UDPMessage message) {

    }

    @Override
    public void abort(UDPMessage message) {

    }

    public void getRentals(UDPMessage parsedMessage) {
        String rentalsString = rentalDAO.getRentals();

        // Create a new UDPMessage with the rentalsString as payload
        UDPMessage response = new UDPMessage(
                parsedMessage.getOperation(),
                "RENTAL_CAR_PROVIDER",
                rentalsString
        );

        // Send the response to the client
    }

    public void getAvailableRentals(UDPMessage parsedMessage) {
    }

}
