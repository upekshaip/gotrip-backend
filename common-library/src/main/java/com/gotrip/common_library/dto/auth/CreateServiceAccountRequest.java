package com.gotrip.common_library.dto.auth;

public record CreateServiceAccountRequest(
    String businessName,
    String businessAddress,
    String businessType,
    String businessPhone
) {
}
