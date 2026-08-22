package com.adarsh.ridebookingbookingservice.apis;

import com.adarsh.ridebookingbookingservice.dtos.AuthValidationRequestDto;
import com.adarsh.ridebookingbookingservice.dtos.AuthValidationResponseDto;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthServiceApi {
    @POST("/api/v1/auth/validate")
    Call<AuthValidationResponseDto> validateToken(
            @Body AuthValidationRequestDto requestDto
    );
}
