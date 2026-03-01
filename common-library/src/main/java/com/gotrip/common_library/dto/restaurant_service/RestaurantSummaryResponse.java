package com.gotrip.common_library.dto.restaurant_service;

import com.gotrip.common_library.dto.restaurant_service.enums.RestaurantStatus;
import com.gotrip.common_library.dto.restaurant_service.enums.PriceUnit;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RestaurantSummaryResponse(
        Long restaurantId,
        String name,
        String description,
        String address,
        String city,
        String imageUrl,
        PriceUnit priceUnit,
        BigDecimal price,
        BigDecimal discount,
        Boolean featured,
        RestaurantStatus status,
        Long providerId,
        LocalDateTime updatedAt
) {}