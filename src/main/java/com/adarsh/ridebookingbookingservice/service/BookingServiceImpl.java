package com.adarsh.ridebookingbookingservice.service;

import com.adarsh.RideBooking_EntityService.models.Booking;
import com.adarsh.RideBooking_EntityService.models.BookingStatus;
import com.adarsh.RideBooking_EntityService.models.Passenger;
import com.adarsh.ridebookingbookingservice.dtos.CreateBookingRequestDto;
import com.adarsh.ridebookingbookingservice.dtos.CreateBookingResponseDto;
import com.adarsh.ridebookingbookingservice.repository.BookingRepository;
import com.adarsh.ridebookingbookingservice.repository.PassengerRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BookingServiceImpl implements BookingService {

    private final PassengerRepository passengerRepository;
    private final BookingRepository bookingRepository;

    public BookingServiceImpl(PassengerRepository passengerRepository, BookingRepository bookingRepository) {
        this.passengerRepository = passengerRepository;
        this.bookingRepository = bookingRepository;
    }


    @Override
    public CreateBookingResponseDto createBooking(CreateBookingRequestDto createBookingRequestDto) {
       Optional<Passenger> passenger =  passengerRepository.findById(createBookingRequestDto.getPassengerId());
       Booking booking = Booking.builder()
               .bookingStatus(BookingStatus.ASSIGNING_DRIVER)
               .startLocation(createBookingRequestDto.getStartLocation())
               .endLocation(createBookingRequestDto.getEndLocation())
               .build();

       Booking newBooking = bookingRepository.save(booking);

       return CreateBookingResponseDto.builder()
               .bookingId(newBooking.getId())
               .bookingStatus(newBooking.getBookingStatus().toString())
               .driver(Optional.of(newBooking.getDriver()))
               .build();

    }
}
