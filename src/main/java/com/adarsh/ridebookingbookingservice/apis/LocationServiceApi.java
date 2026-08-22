package com.adarsh.ridebookingbookingservice.apis;
import com.adarsh.ridebookingbookingservice.dtos.DriverLocationDto;
import com.adarsh.ridebookingbookingservice.dtos.NearByDriversLocationRequestDto;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface LocationServiceApi {
    @POST("/api/v1/location/nearby/drivers")
    Call<DriverLocationDto[]> getNearbyDrivers(@Body NearByDriversLocationRequestDto nearByDriversLocationRequestDto);
}
