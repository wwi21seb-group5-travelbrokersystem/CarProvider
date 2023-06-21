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

    public UDPMessage getAvailableRentals(UDPMessage parsedMessage) {
        return null;
    }

}
