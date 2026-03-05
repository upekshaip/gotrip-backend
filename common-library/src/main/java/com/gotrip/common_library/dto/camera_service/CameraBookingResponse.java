package com.gotrip.common_library.dto.camera_service;

import com.gotrip.common_library.dto.camera_service.CameraBookingDTO;
import com.gotrip.common_library.dto.camera_service.CameraSummaryResponse;
import com.gotrip.common_library.dto.user.TravellerContactInfo;

public record CameraBookingResponse(
        CameraBookingDTO booking,
        CameraSummaryResponse cameraDetails, // Includes name, city, image
        TravellerContactInfo travellerInfo // Only included for Provider view
) {}