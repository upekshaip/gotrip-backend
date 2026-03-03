package com.gotrip.common_library.dto.hotel_service;

import com.gotrip.common_library.dto.hotel_service.enums.PriceUnit;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record HotelCreateRequest(
        @NotBlank(message = "Hotel name is required")
        String name,

        String description,

        @NotBlank(message = "Address is required")
        String address,

        @NotBlank(message = "City is required")
        String city,

        @NotNull(message = "Base price is required")
        @Positive(message = "Price must be greater than zero")
        BigDecimal price,

        @NotNull(message = "Price unit is required")
        PriceUnit priceUnit,

        @NotNull(message = "Latitude is required")
        Double latitude,

        @NotNull(message = "Longitude is required")
        Double longitude,

        String imageUrl,

        boolean isFeatured
) {
}