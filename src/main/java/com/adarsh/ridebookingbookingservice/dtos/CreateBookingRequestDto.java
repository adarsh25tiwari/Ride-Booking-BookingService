package com.adarsh.ridebookingbookingservice.dtos;

import com.adarsh.RideBooking_EntityService.models.ExactLocation;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateBookingRequestDto {
   //private Long passengerId;
   private ExactLocation startLocation;
   private ExactLocation endLocation;
}
