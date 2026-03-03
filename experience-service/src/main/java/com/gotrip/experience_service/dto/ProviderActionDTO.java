package com.gotrip.experience_service.dto;

public record ProviderActionDTO(
        String action,       // ACCEPT or DECLINE
        String message,      // provider message on accept (e.g., "Bikes are ready!")
        String declineReason // reason on decline
) {}
