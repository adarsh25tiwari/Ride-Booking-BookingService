package com.adarsh.ridebookingbookingservice.controller;

import com.adarsh.ridebookingbookingservice.dtos.*;
import com.adarsh.ridebookingbookingservice.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/booking")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @PostMapping
    public ResponseEntity<CreateBookingResponseDto> createBooking( @RequestHeader("Authorization") String authHeader,
                                                                   @RequestBody CreateBookingRequestDto createBookingRequestDto) throws IOException {
        return new  ResponseEntity<>(bookingService.createBooking(authHeader,createBookingRequestDto),
                HttpStatus.CREATED);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<UpdateBookingResponseDto> updateBooking( @PathVariable Long bookingId,
                                                                   @RequestHeader("Authorization") String authHeader,
                                                                   @RequestBody UpdateBookingRequestDto updateBookingRequestDto){

        return new ResponseEntity<>(bookingService.updateBooking(bookingId,authHeader,updateBookingRequestDto),
                HttpStatus.OK);
    }


    @PatchMapping("/internal/{bookingId}")
    public ResponseEntity<UpdateBookingResponseDto> internalUpdateBooking(@PathVariable Long bookingId,
            @RequestBody InternalBookingUpdateRequestDto requestDto) {

        return ResponseEntity.ok(bookingService.internalUpdateBooking(bookingId, requestDto));
    }

}
