package com.gotrip.backend.dto.auth;

public record UserSignupUpdateRequest(
        String name,
        String gender,
        String dob,
        String phone,
        String address
) {}
