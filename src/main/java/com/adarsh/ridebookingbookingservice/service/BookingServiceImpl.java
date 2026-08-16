package com.adarsh.ridebookingbookingservice.service;

import com.adarsh.RideBooking_EntityService.models.Booking;
import com.adarsh.RideBooking_EntityService.models.BookingStatus;
import com.adarsh.RideBooking_EntityService.models.Driver;
import com.adarsh.RideBooking_EntityService.models.Passenger;
import com.adarsh.ridebookingbookingservice.apis.LocationServiceApi;
import com.adarsh.ridebookingbookingservice.dtos.*;
import com.adarsh.ridebookingbookingservice.repository.BookingRepository;
import com.adarsh.ridebookingbookingservice.repository.DriverRepository;
import com.adarsh.ridebookingbookingservice.repository.PassengerRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class BookingServiceImpl implements BookingService {

    private final PassengerRepository passengerRepository;
    private final BookingRepository bookingRepository;
    private final DriverRepository driverRepository;
    private final RestTemplate restTemplate;
    private final LocationServiceApi  locationServiceApi;

    public BookingServiceImpl(PassengerRepository passengerRepository,
                              BookingRepository bookingRepository,
                              DriverRepository driverRepository,
                              RestTemplate restTemplate,
                              LocationServiceApi locationServiceApi) {
        this.passengerRepository = passengerRepository;
        this.bookingRepository = bookingRepository;
        this.driverRepository = driverRepository;
        this.restTemplate = restTemplate;
        this.locationServiceApi = locationServiceApi;
    }

    @Override
     public CreateBookingResponseDto createBooking(CreateBookingRequestDto createBookingRequestDto) {
       Optional<Passenger> passenger =  passengerRepository.findById(createBookingRequestDto.getPassengerId());
       Booking booking = Booking.builder()
               .bookingStatus(BookingStatus.ASSIGNING_DRIVER)
               .startLocation(createBookingRequestDto.getStartLocation())
               .endLocation(createBookingRequestDto.getEndLocation())
               .passenger(passenger.get())
               .build();

       Booking newBooking = bookingRepository.save(booking);

       // Making api call to Location service to fetch nearby drivers
        NearByDriversLocationRequestDto requestDto = NearByDriversLocationRequestDto.builder()
                .latitude(createBookingRequestDto.getEndLocation().getLatitude())
                .longitude(createBookingRequestDto.getEndLocation().getLongitude())
                .build();

        processNearbyDriversAsync(requestDto);

       return CreateBookingResponseDto.builder()
               .bookingId(newBooking.getId())
               .bookingStatus(newBooking.getBookingStatus().toString())
               .driver(Optional.of(newBooking.getDriver()))
               .build();
    }

    @Override
    public UpdateBookingResponseDto updateBooking(Long bookingId,
                                                  UpdateBookingRequestDto RequestDto) {

        Optional<Driver> driver = driverRepository.findById(RequestDto.getDriverId().get());
        if(driver.isPresent()){
            bookingRepository.updateBookingStatusAndDriverById(bookingId,
                    RequestDto.getBookingStatus().toString(),
                    driver.get());
        }

        Optional<Booking> booking =  bookingRepository.findById(bookingId);

        return UpdateBookingResponseDto.builder()
                .bookingId(bookingId)
                .bookingStatus(booking.get().getBookingStatus())
                .driver(Optional.ofNullable(booking.get().getDriver()))
                .build();
    }

    public void processNearbyDriversAsync(NearByDriversLocationRequestDto nearByDriversLocationRequestDto) {
        Call<DriverLocationDto[]> call = locationServiceApi.getNearbyDrivers(nearByDriversLocationRequestDto);

        call.enqueue(new  Callback<DriverLocationDto[]>() {

            @Override
            public void onResponse(Call<DriverLocationDto[]> call, Response<DriverLocationDto[]> response) {
                if(response.isSuccessful() && response.body() != null) {
                    List<DriverLocationDto> driverLocations = Arrays.asList(response.body());

                    driverLocations.forEach(driverLocation -> {
                        System.out.println(driverLocation.getDriverId()+" "+driverLocation.getLatitude()+
                                " "+driverLocation.getLongitude());
                    });

                }else{
                    System.out.println("Request failed"+response.message());
                }
            }

            @Override
            public void onFailure(Call<DriverLocationDto[]> call, Throwable throwable) {
                throwable.printStackTrace();
            }
        });
    }


}
