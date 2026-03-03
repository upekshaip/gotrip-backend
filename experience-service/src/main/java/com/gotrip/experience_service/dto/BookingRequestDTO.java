package com.gotrip.experience_service.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalTime;

public record BookingRequestDTO(
        @NotNull(message = "Experience ID is required")
        Long experienceId,

        @NotNull(message = "Booking date is required")
        @FutureOrPresent(message = "Booking date must be today or in the future")
        LocalDate bookingDate,

        LocalTime startTime,

        @Min(value = 1, message = "Quantity must be at least 1")
        int quantity,

        @Min(value = 0, message = "Duration hours cannot be negative")
        int durationHours
) {}
