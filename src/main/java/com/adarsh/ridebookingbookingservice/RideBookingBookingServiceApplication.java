package com.adarsh.ridebookingbookingservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EntityScan("com.adarsh.RideBooking_EntityService.models")
@EnableJpaAuditing
public class RideBookingBookingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(RideBookingBookingServiceApplication.class, args);
    }

}
