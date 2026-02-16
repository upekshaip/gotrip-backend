package com.gotrip.backend.dto.auth;

public record UserUpdateRequest(
        String name,
        String gender,
        String dob,
        String phone,
        String address
) {}
