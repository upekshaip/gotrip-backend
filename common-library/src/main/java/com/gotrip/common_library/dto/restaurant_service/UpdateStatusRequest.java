package com.gotrip.common_library.dto.restaurant_service;

import com.gotrip.common_library.dto.restaurant_service.enums.RestaurantStatus;

public record UpdateStatusRequest(
        RestaurantStatus status
) {
}
