package com.gotrip.common_library.dto.camera_service;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.time.LocalTime;

public record CameraBookingRequest(
        Long cameraId,
        int personCount,
        String requestMessage,
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate startingDate,
        @JsonFormat(pattern = "HH:mm")
        LocalTime startingTime,
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate endingDate,
        @JsonFormat(pattern = "HH:mm")
        LocalTime endingTime,
        int roomCount
) {}