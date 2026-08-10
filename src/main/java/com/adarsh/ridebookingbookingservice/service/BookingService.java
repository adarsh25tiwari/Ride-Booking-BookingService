package com.adarsh.ridebookingbookingservice.service;

import com.adarsh.RideBooking_EntityService.models.Booking;
import com.adarsh.ridebookingbookingservice.dtos.CreateBookingRequestDto;
import com.adarsh.ridebookingbookingservice.dtos.CreateBookingResponseDto;

public interface BookingService {
    public CreateBookingResponseDto createBooking(CreateBookingRequestDto createBookingRequestDto);
}
