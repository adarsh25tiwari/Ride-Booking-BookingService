package com.adarsh.ridebookingbookingservice.dtos;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DriverLocationDto {
    private String driverId;
    private Double latitude;
    private Double longitude;
}
