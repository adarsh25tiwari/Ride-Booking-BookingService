package com.adarsh.ridebookingbookingservice.service;

import com.adarsh.ridebookingbookingservice.dtos.*;

import java.io.IOException;

public interface BookingService {
    CreateBookingResponseDto createBooking(String authHeader,CreateBookingRequestDto createBookingRequestDto) throws IOException;
    UpdateBookingResponseDto updateBooking(Long bookingId,String authHeader,UpdateBookingRequestDto requestDto);
    UpdateBookingResponseDto internalUpdateBooking(Long bookingId, InternalBookingUpdateRequestDto requestDto);
}
