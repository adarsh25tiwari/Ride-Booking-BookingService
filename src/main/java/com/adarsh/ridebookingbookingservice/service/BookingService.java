package com.adarsh.ridebookingbookingservice.service;

import com.adarsh.ridebookingbookingservice.dtos.CreateBookingRequestDto;
import com.adarsh.ridebookingbookingservice.dtos.CreateBookingResponseDto;
import com.adarsh.ridebookingbookingservice.dtos.UpdateBookingRequestDto;
import com.adarsh.ridebookingbookingservice.dtos.UpdateBookingResponseDto;

public interface BookingService {
    CreateBookingResponseDto createBooking(CreateBookingRequestDto createBookingRequestDto);
    UpdateBookingResponseDto updateBooking(Long bookingId,UpdateBookingRequestDto RequestDto);
}
