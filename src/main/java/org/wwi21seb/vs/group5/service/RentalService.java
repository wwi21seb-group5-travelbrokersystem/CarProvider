package org.wwi21seb.vs.group5.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.platform.commons.logging.LoggerFactory;
import org.wwi21seb.vs.group5.Request.ReservationRequest;
import org.wwi21seb.vs.group5.Request.TransactionResult;
import org.wwi21seb.vs.group5.TwoPhaseCommit.*;
import org.wwi21seb.vs.group5.UDP.UDPMessage;
import org.wwi21seb.vs.group5.dao.RentalDAO;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RentalService {

    private static final Logger LOGGER = Logger.getLogger(RentalService.class.getName());
    private final ConcurrentHashMap<UUID, ParticipantContext> contexts = new ConcurrentHashMap<>();
    private final LogWriter<ParticipantContext> logWriter = new LogWriter<>();
    private static final String CAR_PROIVDER = "CarProvider";
    private final RentalDAO rentalDAO;
    private final ObjectMapper mapper;

    public RentalService() {
        this.rentalDAO = new RentalDAO();
        this.mapper = new ObjectMapper();
    }

    public UDPMessage prepare(UDPMessage message) {
        // Parse the data payload of the UDPMessage to a CoordinatorContext
        CoordinatorContext coordinatorContext = null;
        try {
            coordinatorContext = mapper.readValue(message.getData(), CoordinatorContext.class);
        } catch (JsonProcessingException e) {
            LOGGER.log(Level.SEVERE, "Error parsing CoordinatorContext", e);
            throw new RuntimeException(e);
        }

        // Create a new ParticipantContext with the coordinatorContext
        ParticipantContext participantContext = new ParticipantContext(coordinatorContext);
        contexts.put(participantContext.getTransactionId(), participantContext);
        logWriter.writeLog(participantContext.getTransactionId(), participantContext);

        // Get the bookingContext of the car provider
        BookingContext bookingContext = participantContext.getParticipants().stream().filter(participant -> participant.getName().equals(CAR_PROIVDER)).findFirst().orElseThrow().getBookingContext();

        ReservationRequest reservationRequest = new ReservationRequest(bookingContext.getResourceId(), bookingContext.getStartDate(), bookingContext.getEndDate(), bookingContext.getNumberOfPersons());
        UUID bookingId = rentalDAO.reserveCar(reservationRequest, message.getTransactionId());
        TransactionResult transactionResult = null;

        if (bookingId == null) {
            // If the bookingId is null, the reservation failed
            // We need to set our decision to ABORT and send it to the coordinator
            participantContext.setParticipants(
                    participantContext.getParticipants().stream()
                            .peek(participant -> {
                                if (participant.getName().equals(CAR_PROIVDER)) {

                                    participant.setVote(Vote.NO);
                                }
                            })
                            .toList()
            );

            transactionResult = new TransactionResult(false);
        } else {
            participantContext.setParticipants(
                    participantContext.getParticipants().stream()
                            .peek(participant -> {
                                if (participant.getName().equals(CAR_PROIVDER)) {
                                    participant.setVote(Vote.YES);
                                }
                            })
                            .toList()
            );
            participantContext.setBookingIdForParticipant(bookingId, CAR_PROIVDER);
            transactionResult = new TransactionResult(true);
        }

        // Update the context in the log
        contexts.put(participantContext.getTransactionId(), participantContext);
        logWriter.writeLog(participantContext.getTransactionId(), participantContext);

        // Create a new UDPMessage with the bookingId as payload
        String transactionResultString = "";

        try {
            transactionResultString = mapper.writeValueAsString(transactionResult);
        } catch (JsonProcessingException e) {
            LOGGER.log(Level.SEVERE, "Could not parse TransactionResult to JSON", e);
            throw new RuntimeException(e);
        }

        return new UDPMessage(
                message.getOperation(),
                message.getTransactionId(),
                CAR_PROIVDER,
                transactionResultString
        );
    }

    public UDPMessage commit(UDPMessage message) {
        boolean success = rentalDAO.confirmRental(message.getData());

        // Create a new UDPMessage with an acknowledgement as payload
        String commitResultJsonString = "{\"success\": " + success + "}";
        return new UDPMessage(
                message.getOperation(),
                message.getTransactionId(),
                CAR_PROIVDER,
                commitResultJsonString
        );
    }

    public UDPMessage abort(UDPMessage message) {
        boolean success = rentalDAO.abortRental(message.getData());

        // Create a new UDPMessage with an acknowledgement as payload
        String commitResultJsonString = "{\"success\": " + success + "}";
        return new UDPMessage(
                message.getOperation(),
                message.getTransactionId(),
                CAR_PROIVDER,
                commitResultJsonString
        );
    }

    /**
     * Get all rentals from the database
     *
     * @param parsedMessage the parsed UDPMessage
     * @return a UDPMessage containing all rentals
     */
    public UDPMessage getRentals(UDPMessage parsedMessage) {
        String rentalsString = rentalDAO.getRentals();

        // Create a new UDPMessage with the rentalsString as payload
        return new UDPMessage(
                parsedMessage.getOperation(),
                parsedMessage.getTransactionId(),
                CAR_PROIVDER,
                rentalsString
        );
    }

    /**
     * Get all available rentals from the database
     *
     * @param parsedMessage the parsed UDPMessage
     * @return a UDPMessage containing all available rentals
     */
    public UDPMessage getAvailableRentals(UDPMessage parsedMessage) {
        String availableRentalsString = rentalDAO.getAvailableCars(parsedMessage.getData());

        // Create a new UDPMessage with the availableRentalsString as payload
        return new UDPMessage(
                parsedMessage.getOperation(),
                parsedMessage.getTransactionId(),
                CAR_PROIVDER,
                availableRentalsString
        );
    }

}
