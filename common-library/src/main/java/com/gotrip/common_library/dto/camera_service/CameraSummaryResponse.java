package com.gotrip.common_library.dto.camera_service;

import com.gotrip.common_library.dto.camera_service.enums.CameraStatus;
import com.gotrip.common_library.dto.camera_service.enums.PriceUnit;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CameraSummaryResponse(
        Long cameraId,
        String name,
        String description,
        String address,
        String city,
        String imageUrl,
        PriceUnit priceUnit,
        BigDecimal price,
        BigDecimal discount,
        Boolean featured,
        CameraStatus status,
        Long providerId,
        LocalDateTime updatedAt
) {}