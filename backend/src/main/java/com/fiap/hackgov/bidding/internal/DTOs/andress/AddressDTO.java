package com.fiap.hackgov.bidding.internal.DTOs.andress;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddressDTO(

        @NotBlank(message = "Street is required")
        String street,

        @NotBlank(message = "Number is required")
        String number,

        String complement,

        @NotBlank(message = "District is required")
        String district,

        @NotBlank(message = "City is required")
        String city,

        @NotBlank(message = "State is required")
        @Size(min = 2, max = 2)
        String state,

        @NotBlank(message = "Zip code is required")
        String zipCode

) {
}