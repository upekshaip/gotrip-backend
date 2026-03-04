package com.gotrip.common_library.dto.hotel_service;

import com.gotrip.common_library.dto.user.TravellerContactInfo;

public record HotelBookingResponse(
        HotelBookingDTO booking,
        HotelSummaryResponse hotelDetails, // Includes name, city, image
        TravellerContactInfo travellerInfo // Only included for Provider view
) {}