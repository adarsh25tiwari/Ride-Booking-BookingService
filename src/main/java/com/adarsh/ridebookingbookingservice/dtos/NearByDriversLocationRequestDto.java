package com.adarsh.ridebookingbookingservice.dtos;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public  class NearByDriversLocationRequestDto {
    private Double latitude;
    private Double longitude;
}
