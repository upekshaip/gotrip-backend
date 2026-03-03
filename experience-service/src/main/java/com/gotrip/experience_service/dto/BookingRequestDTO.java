package com.gotrip.experience_service.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record BookingRequestDTO(
        Long experienceId,
        LocalDate bookingDate,
        LocalTime startTime,
        int quantity,
        int durationHours
) {}
