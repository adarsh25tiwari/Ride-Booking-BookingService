package com.adarsh.ridebookingbookingservice.dtos;

import com.adarsh.RideBooking_EntityService.models.BookingStatus;
import lombok.*;

import java.util.Optional;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateBookingRequestDto {
    private BookingStatus  bookingStatus;
    private Optional<Long> driverId;
}
