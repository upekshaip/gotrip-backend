package com.gotrip.transport_service.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record TransportReviewRequest(
        @NotNull Long transportId,
        @NotNull Long bookingId,
        @Min(1) @Max(5) Integer rating,
        String comment
) {}