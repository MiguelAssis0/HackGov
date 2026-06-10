package com.fiap.hackgov.bidding.internal.DTOs.andress;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AddressDTO(

        @NotBlank(message = "Street is required")
        @Size(min = 3, max = 255)
        @Pattern(regexp = "^[\\p{L}\\p{N} .,'()/-]+$", message = "Invalid street")
        String street,

        @NotBlank(message = "Number is required")
        @Size(max = 20)
        @Pattern(regexp = "^[\\p{L}\\p{N} ./-]+$", message = "Invalid number")
        String number,

        @Size(max = 255)
        String complement,

        @NotBlank(message = "District is required")
        @Size(min = 2, max = 255)
        @Pattern(regexp = "^[\\p{L}\\p{N} .'-]+$", message = "Invalid district")
        String district,

        @NotBlank(message = "City is required")
        @Size(min = 2, max = 255)
        @Pattern(regexp = "^[\\p{L} .'-]+$", message = "Invalid city")
        String city,

        @NotBlank(message = "State is required")
        @Size(min = 2, max = 2)
        @Pattern(regexp = "^[A-Z]{2}$", message = "Invalid state")
        String state,

        @NotBlank(message = "Zip code is required")
        @Pattern(regexp = "^\\d{5}-?\\d{3}$", message = "Invalid zip code")
        String zipCode

) {
}
