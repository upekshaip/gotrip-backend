package com.gotrip.common_library.dto.transport_service;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

public record TransportBookingRequest(
        @NotNull(message = "Transport ID is required") Long transportId,
        @NotBlank(message = "Pickup location is required") String pickupLocation,
        @NotBlank(message = "Dropoff location is required") String dropoffLocation,
        @NotNull(message = "Starting date is required") LocalDate startingDate,
        @NotNull(message = "Starting time is required") LocalTime startingTime,
        @NotNull(message = "Ending date is required") LocalDate endingDate,
        @NotNull(message = "Ending time is required") LocalTime endingTime,
        String requestMessage
) {}