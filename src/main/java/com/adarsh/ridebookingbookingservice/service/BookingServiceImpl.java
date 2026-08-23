package com.adarsh.ridebookingbookingservice.service;

import com.adarsh.RideBooking_EntityService.models.Booking;
import com.adarsh.RideBooking_EntityService.models.BookingStatus;
import com.adarsh.RideBooking_EntityService.models.Driver;
import com.adarsh.RideBooking_EntityService.models.Passenger;
import com.adarsh.ridebookingbookingservice.apis.AuthServiceApi;
import com.adarsh.ridebookingbookingservice.apis.LocationServiceApi;
import com.adarsh.ridebookingbookingservice.apis.SocketServiceApi;
import com.adarsh.ridebookingbookingservice.dtos.*;
import com.adarsh.ridebookingbookingservice.repository.BookingRepository;
import com.adarsh.ridebookingbookingservice.repository.DriverRepository;
import com.adarsh.ridebookingbookingservice.repository.PassengerRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class BookingServiceImpl implements BookingService {

    private final PassengerRepository passengerRepository;
    private final BookingRepository bookingRepository;
    private final DriverRepository driverRepository;
    private final RestTemplate restTemplate;
    private final LocationServiceApi  locationServiceApi;
    private final AuthServiceApi authServiceApi;
    private final SocketServiceApi socketServiceApi;
    private final ScheduledExecutorService bookingScheduler;

    public BookingServiceImpl(PassengerRepository passengerRepository,
                              BookingRepository bookingRepository,
                              DriverRepository driverRepository,
                              RestTemplate restTemplate,
                              LocationServiceApi locationServiceApi,
                              AuthServiceApi authServiceApi,
                              SocketServiceApi socketServiceApi,
                              ScheduledExecutorService bookingScheduler) {
        this.passengerRepository = passengerRepository;
        this.bookingRepository = bookingRepository;
        this.driverRepository = driverRepository;
        this.restTemplate = restTemplate;
        this.locationServiceApi = locationServiceApi;
        this.authServiceApi = authServiceApi;
        this.socketServiceApi = socketServiceApi;
        this.bookingScheduler = bookingScheduler;
    }

    @Override
    public CreateBookingResponseDto createBooking(String authHeader,
                                                  CreateBookingRequestDto createBookingRequestDto) throws IOException {

        if(authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or Invalid Authorization Header");
        }

        String token = authHeader.substring(7);
        AuthValidationRequestDto authRequest = new AuthValidationRequestDto();
        authRequest.setToken(token);
        AuthValidationResponseDto authResponse;

        try {

            Response<AuthValidationResponseDto> response = authServiceApi.validateToken(authRequest).execute();
            if(!response.isSuccessful() || response.body() == null) {
                throw new RuntimeException("Unable to validate token");
            }

            authResponse = response.body();

        } catch (Exception e) {
            throw new RuntimeException(
                    "Auth Service Unavailable",
                    e
            );
        }
       //checking
        System.out.println("Auth validation response: " + authResponse.isValid());

        System.out.println("Email: " + authResponse.getEmail());

        System.out.println("Role: " + authResponse.getRole());


        if(!authResponse.isValid()) {
            throw new RuntimeException("Invalid Token");
        }

        if (!"ROLE_PASSENGER".equals(authResponse.getRole())) {
            throw new RuntimeException("Only passengers can create bookings");
        }

        Passenger passenger = passengerRepository.findByEmail(authResponse.getEmail()).orElseThrow(() ->
                                new RuntimeException("Passenger not found"));

        Booking booking = Booking.builder()
                .bookingStatus(BookingStatus.ASSIGNING_DRIVER)
                .startLocation(createBookingRequestDto.getStartLocation())
                .endLocation(createBookingRequestDto.getEndLocation())
                .passenger(passenger)
                .build();

        Booking newBooking = bookingRepository.save(booking);

        NearByDriversLocationRequestDto requestDto =
                NearByDriversLocationRequestDto.builder()
                        .latitude(createBookingRequestDto.getStartLocation().getLatitude())
                        .longitude(createBookingRequestDto.getStartLocation().getLongitude())
                        .build();

        //check
        System.out.println("Searching nearby drivers at: latitude=" + requestDto.getLatitude() + ", longitude="
                        + requestDto.getLongitude());
      // new
        Response<DriverLocationDto[]> nearbyDriverResponse =
                locationServiceApi.getNearbyDrivers(requestDto)
                        .execute();

        //check
        System.out.println("Location Service HTTP Code: "
                + nearbyDriverResponse.code());

        System.out.println("Location Service response: "
                + Arrays.toString(nearbyDriverResponse.body()));


        if (!nearbyDriverResponse.isSuccessful() ||
                nearbyDriverResponse.body() == null) {

            throw new RuntimeException("Unable to find nearby drivers");
        }

        //convert array to list
        List<Long> driverIds = Arrays.stream(nearbyDriverResponse.body())
                .map(DriverLocationDto::getDriverId)
                .map(Long::parseLong)
                .toList();

        //handling empty drivers
        if (driverIds.isEmpty()) {

            newBooking.setBookingStatus(BookingStatus.NO_DRIVER_AVAILABLE);
            bookingRepository.save(newBooking);

            return CreateBookingResponseDto.builder()
                    .bookingId(newBooking.getId())
                    .bookingStatus(newBooking.getBookingStatus().toString())
                    .driver(Optional.empty())
                    .build();
        }


        System.out.println("Nearby Driver IDs: " + driverIds);
        RideRequestDto rideRequestDto = RideRequestDto.builder()
                .driverIds(driverIds)
                .bookingId(newBooking.getId())
                .build();

        raiseRideRequestAsync(rideRequestDto);
        // 2-minute ride request timeout
        scheduleBookingTimeout(newBooking.getId());

        return CreateBookingResponseDto.builder()
                .bookingId(newBooking.getId())
                .bookingStatus(newBooking.getBookingStatus().toString())
                .driver(Optional.ofNullable(newBooking.getDriver()))
                .build();
    }


    @Override
    public UpdateBookingResponseDto updateBooking(
            Long bookingId,
            String authHeader,
            UpdateBookingRequestDto requestDto) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or Invalid Authorization Header");
        }

        String token = authHeader.substring(7);
        AuthValidationRequestDto authRequest = new AuthValidationRequestDto();
        authRequest.setToken(token);

        AuthValidationResponseDto authResponse;

        try {
            Response<AuthValidationResponseDto> response = authServiceApi.validateToken(authRequest)
                            .execute();
            if (!response.isSuccessful() || response.body() == null) {
                throw new RuntimeException("Unable to validate token");
            }
            authResponse = response.body();

        } catch (Exception e) {
            throw new RuntimeException("Auth Service Unavailable", e);
        }

        if (!authResponse.isValid()) {
            throw new RuntimeException("Invalid Token");
        }

        if (!"ROLE_DRIVER".equals(authResponse.getRole())) {
            throw new RuntimeException("Only drivers can update bookings");
        }

        Driver driver = driverRepository
                .findByEmail(authResponse.getEmail())
                .orElseThrow(() ->
                        new RuntimeException("Driver not found")
                );

        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() ->
                        new RuntimeException("Booking not found"));

        // new change
        if (booking.getBookingStatus() != BookingStatus.ASSIGNING_DRIVER) {
            throw new RuntimeException("Booking already assigned");
        }

        if (booking.getDriver() != null &&
                !booking.getDriver().getId().equals(driver.getId())) {
            throw new RuntimeException("Booking is already assigned to another driver");
        }

        if (requestDto.getBookingStatus() == null) {
            throw new RuntimeException("Booking status is required");
        }

        bookingRepository.updateBookingStatusAndDriverById(
                bookingId,
                requestDto.getBookingStatus(),
                driver
        );

        System.out.println("Rows updated");

        booking = bookingRepository.findById(bookingId).orElseThrow(() ->
                        new RuntimeException("Booking not found"));

        Passenger passenger = booking.getPassenger();
        PassengerResponseDto passengerResponseDto = new PassengerResponseDto(
                        passenger.getId(),
                        passenger.getName(),
                        passenger.getPhoneNumber()
                );


        DriverResponseDto driverResponseDto = new DriverResponseDto(
                driver.getId(),
                driver.getName(),
                driver.getMobileNumber(),
                driver.getLicenceNumber()
        );

        return UpdateBookingResponseDto.builder()
                .bookingId(booking.getId())
                .bookingStatus(booking.getBookingStatus())
                .driver(driverResponseDto)
                .passenger(passengerResponseDto)
                .build();
    }

    // For simplicity of socket implementation

    @Override
    public UpdateBookingResponseDto internalUpdateBooking(Long bookingId, InternalBookingUpdateRequestDto requestDto) {

        if (requestDto.getDriverId() == null) {
            throw new RuntimeException("Driver ID is required");
        }

        Driver driver = driverRepository.findById(requestDto.getDriverId())
                .orElseThrow(() -> new RuntimeException("Driver not found"));

        // TEMPORARY - only for concurrency testing no 2 driver can accept same ride
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Thread interrupted", e);
        }

        // ATOMIC DRIVER ASSIGNMENT
        int updatedRow = bookingRepository.assignDriverToBooking(
                bookingId,
                BookingStatus.ASSIGNING_DRIVER,
                BookingStatus.SCHEDULED,
                driver
        );

        if (updatedRow == 0) {
            throw new RuntimeException("Ride is already assigned to another driver or is no longer available");
        }

        System.out.println("Booking " + bookingId +" assigned to driver " + driver.getId());
        // Reload updated booking
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        Passenger passenger = booking.getPassenger();
        PassengerResponseDto passengerResponseDto = new PassengerResponseDto(
                        passenger.getId(),
                        passenger.getName(),
                        passenger.getPhoneNumber()
                );

        DriverResponseDto driverResponseDto = new DriverResponseDto(
                        driver.getId(),
                        driver.getName(),
                        driver.getMobileNumber(),
                        driver.getLicenceNumber()
                );

        return UpdateBookingResponseDto.builder()
                .bookingId(booking.getId())
                .bookingStatus(booking.getBookingStatus())
                .driver(driverResponseDto)
                .passenger(passengerResponseDto)
                .build();
    }

    @Override
    public UpdateBookingResponseDto getBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() ->
                        new RuntimeException("Booking not found"));

        Passenger passenger = booking.getPassenger();
        PassengerResponseDto passengerResponseDto = new PassengerResponseDto(
                        passenger.getId(),
                        passenger.getName(),
                        passenger.getPhoneNumber()
                );
        DriverResponseDto driverResponseDto = null;

        if (booking.getDriver() != null) {
            Driver driver = booking.getDriver();

            driverResponseDto = new DriverResponseDto(
                            driver.getId(),
                            driver.getName(),
                            driver.getMobileNumber(),
                            driver.getLicenceNumber()
            );
        }

        return UpdateBookingResponseDto.builder()
                .bookingId(booking.getId())
                .bookingStatus(booking.getBookingStatus())
                .driver(driverResponseDto)
                .passenger(passengerResponseDto)
                .build();
    }


    public void processNearbyDriversAsync(NearByDriversLocationRequestDto nearByDriversLocationRequestDto,
            Long bookingId) {

        // checking
        System.out.println("processNearbyDriversAsync CALLED");
        System.out.println("Booking ID: " + bookingId);

        Call<DriverLocationDto[]> call = locationServiceApi.getNearbyDrivers(nearByDriversLocationRequestDto);
        call.enqueue(new Callback<DriverLocationDto[]>() {

            @Override
            public void onResponse(Call<DriverLocationDto[]> call, Response<DriverLocationDto[]> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<DriverLocationDto> driverLocations = Arrays.asList(response.body());

                    driverLocations.forEach(driverLocation -> {
                        System.out.println(
                                driverLocation.getDriverId() + " " +
                                        driverLocation.getLatitude() + " " +
                                        driverLocation.getLongitude()
                        );
                    });

                    // Extract driver IDs
                    List<Long> driverIds = driverLocations.stream()
                            .map(DriverLocationDto::getDriverId)
                            .map(Long::valueOf)
                            .toList();

                    System.out.println("Nearby Driver IDs: " + driverIds);

                    // Send ride request to Socket Service
                    raiseRideRequestAsync(RideRequestDto.builder()
                                    .driverIds(driverIds)
                                    .bookingId(bookingId)
                                    .build()
                    );

                } else {
                    System.out.println("Request failed");
                    System.out.println("HTTP Code: " + response.code());
                    System.out.println("Message: " + response.message());
                    System.out.println("Error Body: " + response.errorBody());
                }
            }

            @Override
            public void onFailure(Call<DriverLocationDto[]> call, Throwable throwable) {
                throwable.printStackTrace();
            }
        });
    }



    private void raiseRideRequestAsync(RideRequestDto requestDto){
        Call<Boolean> call = socketServiceApi.sendNewRideRequest(requestDto);
        call.enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                System.out.println("Socket Service HTTP Status: "+ response.code());

                if (response.isSuccessful() && response.body() != null) {
                    System.out.println("Ride request sent to Socket Service: " + response.body());
                } else {
                    System.out.println("Socket Service request failed: "+ response.message());
                }
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable throwable) {
                System.out.println("Socket Service call FAILED");
                throwable.printStackTrace();
            }
        });


    }


    private void scheduleBookingTimeout(Long bookingId) {
        bookingScheduler.schedule(() -> {
            try {
                System.out.println("Checking booking timeout for booking: " + bookingId);
                int updatedRows = bookingRepository.markBookingAsNoDriverAvailable(bookingId,
                        BookingStatus.ASSIGNING_DRIVER,
                        BookingStatus.NO_DRIVER_AVAILABLE);

                if (updatedRows == 1) {
                    System.out.println("Booking " + bookingId + " expired. No driver accepted the ride.");
                } else {
                    System.out.println("Booking " + bookingId + " was already assigned or otherwise completed.");
                }

            } catch (Exception e) {
                System.out.println("Error while processing booking timeout: " + bookingId);
                e.printStackTrace();
            }

        }, 2, TimeUnit.MINUTES);

    }


}
