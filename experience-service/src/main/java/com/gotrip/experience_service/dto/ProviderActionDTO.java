package com.gotrip.experience_service.dto;

import com.gotrip.experience_service.model.enums.BookingStatus;

public record ProviderActionDTO(
        BookingStatus status,      // provider message on accept (e.g., "Bikes are ready!")
        String message // reason on decline
) {}
