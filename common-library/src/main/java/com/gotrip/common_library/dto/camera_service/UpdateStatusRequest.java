package com.gotrip.common_library.dto.camera_service;

import com.gotrip.common_library.dto.camera_service.enums.CameraStatus;

public record UpdateStatusRequest(
        CameraStatus status
) {
}
