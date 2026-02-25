package com.gotrip.common_library.dto.user;

public record UserProfileUpdateRequest(
        String name,
        String phone,
        String dob, // Expecting ISO date string "YYYY-MM-DD"
        String gender
) {}
