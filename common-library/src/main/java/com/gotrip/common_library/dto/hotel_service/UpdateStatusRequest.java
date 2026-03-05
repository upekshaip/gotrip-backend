package com.gotrip.common_library.dto.hotel_service;

import com.gotrip.common_library.dto.hotel_service.enums.HotelStatus;

public record UpdateStatusRequest(
        HotelStatus status
) {
}
