package com.gotrip.common_library.dto.hotel_service;

import java.time.LocalDate;
import java.time.LocalTime;

public record HotelBookingRequest(
        Long hotelId,
        int personCount,
        String requestMessage,
        LocalDate startingDate,
        LocalTime startingTime,
        LocalDate endingDate,
        LocalTime endingTime
) {}