package com.adarsh.ridebookingbookingservice.dtos;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthValidationResponseDto {

    private boolean valid;
    private String email;
    private String role;
}
