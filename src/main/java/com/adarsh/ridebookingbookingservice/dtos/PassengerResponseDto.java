package com.adarsh.ridebookingbookingservice.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PassengerResponseDto {
    private Long id;
    private String name;
    private String phoneNumber;

}
