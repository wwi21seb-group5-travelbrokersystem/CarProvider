package org.wwi21seb.vs.group5;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.wwi21seb.vs.group5.UDP.UDPMessage;
import org.wwi21seb.vs.group5.service.CarService;
import org.wwi21seb.vs.group5.service.RentalService;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class RentalCarProviderMain {

    public static void main(String[] args) {
        System.out.println("Rental Car Provider: Initializing!");
        ObjectMapper mapper = new ObjectMapper();

        System.out.println("Rental Car Provider: Initializing services!");
        RentalService rentalService = new RentalService();
        CarService carService = new CarService();

        try (DatagramSocket socket = new DatagramSocket(5001)) {
            System.out.printf("Rental Car Provider: Socket initialized on port %s!%n", socket.getLocalPort());

            byte[] buffer = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            System.out.println("Rental Car Provider: Waiting for message...");

            while (true) {
                socket.receive(packet);
                String message = new String(packet.getData(), 0, packet.getLength());
                System.out.printf("Rental Car Provider: Received message: %s%n", message);

                UDPMessage parsedMessage = mapper.readValue(message, UDPMessage.class);
                System.out.printf("Rental Car Provider: Parsed message: %s%n", parsedMessage);

                switch (parsedMessage.getOperation()) {
                    case PREPARE -> {
                        System.out.println("Rental Car Provider: Renting car!");
                        rentalService.prepare(parsedMessage);
                    }
                    case COMMIT -> {
                        System.out.println("Rental Car Provider: Committing transaction!");
                        rentalService.commit(parsedMessage);
                    }
                    case ABORT -> {
                        System.out.println("Rental Car Provider: Aborting transaction!");
                        rentalService.abort(parsedMessage);
                    }
                    case GET_BOOKINGS -> {
                        System.out.println("Rental Car Provider: Getting rentals!");
                        rentalService.getRentals(parsedMessage);
                    }
                    case GET_AVAILABILITY -> {
                        System.out.println("Rental Car Provider: Getting available rentals!");
                        rentalService.getAvailableRentals(parsedMessage);
                    }
                    case GET -> {
                        System.out.println("Rental Car Provider: Getting car!");
                        carService.getCar(parsedMessage);
                    }
                    default -> System.out.println("Rental Car Provider: Unknown operation!");
                }
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