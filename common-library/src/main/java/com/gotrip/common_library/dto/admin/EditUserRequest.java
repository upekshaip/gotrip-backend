package com.gotrip.common_library.dto.admin;

public record EditUserRequest(
        Long userId,
        String name,
        String phone,
        String dob, // Expecting ISO date string "YYYY-MM-DD"
        String gender
) {
}
