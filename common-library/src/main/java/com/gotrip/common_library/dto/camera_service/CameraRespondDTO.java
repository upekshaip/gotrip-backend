package com.gotrip.common_library.dto.camera_service;

import com.gotrip.common_library.dto.camera_service.enums.BookingStatus;

public record CameraRespondDTO(String message, BookingStatus status) {
}
