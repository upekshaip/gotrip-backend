package com.gotrip.common_library.dto.hotel_service;

import com.gotrip.common_library.dto.hotel_service.enums.HotelStatus;
import com.gotrip.common_library.dto.hotel_service.enums.PriceUnit;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record HotelSummaryResponse(
        Long hotelId,
        String name,
        String description,
        String address,
        String city,
        String imageUrl,
        PriceUnit priceUnit,
        BigDecimal price,
        BigDecimal discount,
        Boolean featured,
        HotelStatus status,
        Long providerId,
        LocalDateTime updatedAt
) {}