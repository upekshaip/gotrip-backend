package com.gotrip.common_library.dto.restaurant_service;

import com.gotrip.common_library.dto.restaurant_service.enums.BookingStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record RestaurantBookingDTO(
        Long bookingId,
        String bookingReference,
        BookingStatus status,
        int personCount,
        int roomCount,
        LocalDate startingDate,
        LocalTime startingTime,
        LocalDate endingDate,
        LocalTime endingTime,
        BigDecimal finalAmount,
        String requestMessage,
        String providerMessage,
        LocalDateTime createdAt,
        BigDecimal totalAmount,
        BigDecimal discountAmount,
        BigDecimal basePrice

) {}