package com.adarsh.ridebookingbookingservice.apis;

import com.adarsh.ridebookingbookingservice.dtos.NearByDriversLocationRequestDto;
import com.adarsh.ridebookingbookingservice.dtos.RideRequestDto;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface SocketServiceApi {
    @POST("/api/v1/socket/newRide")
    Call<Boolean> sendNewRideRequest(@Body RideRequestDto requestDto);
}
