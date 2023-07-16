package org.wwi21seb.vs.group5;

import org.wwi21seb.vs.group5.Logger.LoggerFactory;
import org.wwi21seb.vs.group5.service.RentalService;

import java.util.logging.Logger;

public class RentalCarProviderMain {

    private static final Logger LOGGER = LoggerFactory.setupLogger(RentalCarProviderMain.class.getName());

    public static void main(String[] args) {
        LOGGER.info("Starting HotelRoomProvider");
        RentalService rentalService = new RentalService();
        rentalService.start();
    }

}