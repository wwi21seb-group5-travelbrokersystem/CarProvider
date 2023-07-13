package org.wwi21seb.vs.group5;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.wwi21seb.vs.group5.TwoPhaseCommit.ParticipantContext;
import org.wwi21seb.vs.group5.UDP.UDPMessage;
import org.wwi21seb.vs.group5.service.RentalService;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class RentalCarProviderMain {

    public synchronized static void main(String[] args) {
        System.out.println("Rental Car Provider: Initializing!");
        ObjectMapper mapper = new ObjectMapper();

        System.out.println("Rental Car Provider: Initializing services!");
        RentalService rentalService = new RentalService();

        try (DatagramSocket socket = new DatagramSocket(5001)) {
            System.out.printf("Rental Car Provider: Socket initialized on port %s!%n", socket.getLocalPort());

            byte[] buffer = new byte[16384];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            System.out.println("Rental Car Provider: Waiting for message...");

            while (true) {
                socket.receive(packet);
                String message = new String(packet.getData(), 0, packet.getLength());

                UDPMessage parsedMessage = mapper.readValue(message, UDPMessage.class);
                System.out.printf("Rental Car Provider: Received message: %s%n", parsedMessage);

                UDPMessage response = null;

                switch (parsedMessage.getOperation()) {
                    case PREPARE -> {
                        System.out.println("Rental Car Provider: Renting car!");
                        response = rentalService.prepare(parsedMessage);
                    }
                    case COMMIT -> {
                        System.out.println("Rental Car Provider: Committing transaction!");
                        response = rentalService.commit(parsedMessage);
                    }
                    case ABORT -> {
                        System.out.println("Rental Car Provider: Aborting transaction!");
                        response = rentalService.abort(parsedMessage);
                    }
                    case GET_BOOKINGS -> {
                        System.out.println("Rental Car Provider: Getting rentals!");
                        response = rentalService.getRentals(parsedMessage);
                    }
                    case GET_AVAILABILITY -> {
                        System.out.println("Rental Car Provider: Getting available rentals!");
                        response = rentalService.getAvailableRentals(parsedMessage);
                    }
                    default -> System.out.println("Rental Car Provider: Unknown operation!");
                }

                if (response != null) {
                    System.out.printf("Rental Car Provider: Sending response: %s%n", response);
                    byte[] responseBytes = mapper.writeValueAsBytes(response);
                    DatagramPacket responsePacket = new DatagramPacket(responseBytes, responseBytes.length, packet.getAddress(), packet.getPort());
                    socket.send(responsePacket);
                }

                System.out.println("%nRental Car Provider: Waiting for message...");
            }
        } catch (SocketException e) {
            System.out.println("Rental Car Provider: Error while initializing socket!");
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (IOException e) {
            System.out.println("Rental Car Provider: Error while receiving message!");
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

}