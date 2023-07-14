package org.wwi21seb.vs.group5;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.wwi21seb.vs.group5.Logger.LoggerFactory;
import org.wwi21seb.vs.group5.UDP.UDPMessage;
import org.wwi21seb.vs.group5.service.RentalService;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.logging.Logger;

public class RentalCarProviderMain {

    private static final Logger LOGGER = LoggerFactory.setupLogger(RentalCarProviderMain.class.getName());

    public synchronized static void main(String[] args) {
        LOGGER.info("Starting up!");
        ObjectMapper mapper = new ObjectMapper();

        // Initialize the RentalService
        LOGGER.info("Initializing RentalService!");
        RentalService rentalService = new RentalService();

        try (DatagramSocket socket = new DatagramSocket(5001)) {
            LOGGER.info("Socket initialized on port 5001!");

            byte[] buffer = new byte[16384];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            while (true) {
                LOGGER.info("Waiting for message!");
                socket.receive(packet);
                String message = new String(packet.getData(), 0, packet.getLength());

                UDPMessage parsedMessage = mapper.readValue(message, UDPMessage.class);
                LOGGER.info(String.format("Received %s message from %s: %s", parsedMessage.getOperation(), parsedMessage.getSender(), parsedMessage.getData()));

                UDPMessage response = null;

                switch (parsedMessage.getOperation()) {
                    case PREPARE -> response = rentalService.prepare(parsedMessage);
                    case COMMIT -> response = rentalService.commit(parsedMessage);
                    case ABORT -> response = rentalService.abort(parsedMessage);
                    case GET_BOOKINGS -> response = rentalService.getRentals(parsedMessage);
                    case GET_AVAILABILITY -> response = rentalService.getAvailableRentals(parsedMessage);
                    default -> LOGGER.severe("Unknown operation received!");
                }

                if (response != null) {
                    LOGGER.info(String.format("Sending %s message to %s: %s", response.getOperation(), response.getSender(), response.getData()));
                    byte[] responseBytes = mapper.writeValueAsBytes(response);
                    DatagramPacket responsePacket = new DatagramPacket(responseBytes, responseBytes.length, packet.getAddress(), packet.getPort());
                    socket.send(responsePacket);
                }
            }
        } catch (SocketException e) {
            LOGGER.severe("Error while initializing socket!");
            throw new RuntimeException(e);
        } catch (IOException e) {
            LOGGER.severe("Error while receiving message!");
            throw new RuntimeException(e);
        }
    }

}