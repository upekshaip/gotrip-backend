package com.gotrip.common_library.dto.restaurant_service;

import com.gotrip.common_library.dto.hotel_service.enums.BookingStatus;

public record RestaurantRespondDTO(String message, BookingStatus status) {
}
