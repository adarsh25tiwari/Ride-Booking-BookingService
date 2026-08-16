package com.adarsh.ridebookingbookingservice.controller;

import com.adarsh.ridebookingbookingservice.dtos.CreateBookingRequestDto;
import com.adarsh.ridebookingbookingservice.dtos.CreateBookingResponseDto;
import com.adarsh.ridebookingbookingservice.dtos.UpdateBookingRequestDto;
import com.adarsh.ridebookingbookingservice.dtos.UpdateBookingResponseDto;
import com.adarsh.ridebookingbookingservice.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/booking")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @PostMapping
    public ResponseEntity<CreateBookingResponseDto> createBooking(@RequestBody CreateBookingRequestDto createBookingRequestDto){
        return new  ResponseEntity<>(bookingService.createBooking(createBookingRequestDto),
                HttpStatus.CREATED);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<UpdateBookingResponseDto> updateBooking( @PathVariable Long bookingId,
                                                                   @RequestBody UpdateBookingRequestDto updateBookingRequestDto){

        return new ResponseEntity<>(bookingService.updateBooking(bookingId,updateBookingRequestDto),
                HttpStatus.OK);
    }

}
