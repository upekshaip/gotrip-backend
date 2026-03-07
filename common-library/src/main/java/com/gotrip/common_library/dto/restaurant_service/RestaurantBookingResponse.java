package com.gotrip.common_library.dto.restaurant_service;

import com.gotrip.common_library.dto.user.TravellerContactInfo;

public record RestaurantBookingResponse(
        RestaurantBookingDTO booking,
        RestaurantSummaryResponse restaurantDetails,
        TravellerContactInfo travellerInfo // Only included for Provider view
) {}
