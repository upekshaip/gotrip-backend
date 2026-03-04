package com.gotrip.common_library.dto.hotel_service;

import com.gotrip.common_library.dto.hotel_service.enums.BookingStatus;

public record HotelRespondDTO(String message, BookingStatus status) {
}
