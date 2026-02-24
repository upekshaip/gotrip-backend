package com.gotrip.common_library.dto.auth;

public record UserSignupUpdateRequest(
        String name,
        String gender,
        String dob,
        String phone
) {}
