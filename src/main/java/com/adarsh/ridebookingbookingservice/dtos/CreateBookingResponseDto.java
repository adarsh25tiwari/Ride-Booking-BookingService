package com.adarsh.ridebookingbookingservice.dtos;

import com.adarsh.RideBooking_EntityService.models.Driver;
import lombok.*;

import java.util.Optional;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateBookingResponseDto {
    private Long bookingId;
    private String bookingStatus;
    private Optional<Driver> driver;
}
