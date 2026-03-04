package com.gotrip.common_library.dto.transport_service;

import com.gotrip.common_library.dto.transport_service.enums.PriceUnit;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import java.math.BigDecimal;

public record TransportCreateRequest(
        @NotBlank(message = "Vehicle make is required") String vehicleMake,
        @NotBlank(message = "Vehicle model is required") String vehicleModel,
        @NotBlank(message = "Vehicle type is required (e.g., Car, Van)") String vehicleType,
        String description,
        @NotBlank(message = "City is required") String city,
        @NotNull(message = "Price unit is required") PriceUnit priceUnit,
        @NotNull(message = "Price is required") BigDecimal price,
        @NotNull(message = "Capacity is required") @Min(1) Integer capacity,
        @NotNull(message = "Latitude is required") Double latitude,
        @NotNull(message = "Longitude is required") Double longitude,
        String imageUrl,
        boolean isFeatured
) {}