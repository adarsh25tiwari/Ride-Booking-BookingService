package com.adarsh.ridebookingbookingservice.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

// for changing driver status after a timeout window
@Configuration
public class BookingSchedularConfig {

    @Bean
    public ScheduledExecutorService bookingScheduler() {
        return Executors.newScheduledThreadPool(5);
    }
}
