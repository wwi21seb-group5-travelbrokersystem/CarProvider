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
    public UDPMessage prepare(UDPMessage message) {
        return null;
    }

    @Override
    public UDPMessage commit(UDPMessage message) {
        return null;
    }

    @Override
    public UDPMessage abort(UDPMessage message) {
        return null;
    }

    /**
     * Get all rentals from the database
     * @param parsedMessage the parsed UDPMessage
     * @return a UDPMessage containing all rentals
     */
    public UDPMessage getRentals(UDPMessage parsedMessage) {
        String rentalsString = rentalDAO.getRentals();

        // Create a new UDPMessage with the rentalsString as payload
        return new UDPMessage(
                parsedMessage.getOperation(),
                parsedMessage.getTransactionId(),
                "RENTAL_CAR_PROVIDER",
                rentalsString
        );
    }

    /**
     * Get all available rentals from the database
     * @param parsedMessage the parsed UDPMessage
     * @return a UDPMessage containing all available rentals
     */
    public UDPMessage getAvailableRentals(UDPMessage parsedMessage) {
        String availableRentalsString = rentalDAO.getAvailableCars(parsedMessage.getData());

        // Create a new UDPMessage with the availableRentalsString as payload
        return new UDPMessage(
                parsedMessage.getOperation(),
                parsedMessage.getTransactionId(),
                "RENTAL_CAR_PROVIDER",
                availableRentalsString
        );
    }

}
