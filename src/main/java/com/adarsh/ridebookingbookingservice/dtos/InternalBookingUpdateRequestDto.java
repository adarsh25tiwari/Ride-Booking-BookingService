package com.adarsh.ridebookingbookingservice.dtos;

import com.adarsh.RideBooking_EntityService.models.BookingStatus;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InternalBookingUpdateRequestDto {
    private Long driverId;
    private BookingStatus bookingStatus;
}
