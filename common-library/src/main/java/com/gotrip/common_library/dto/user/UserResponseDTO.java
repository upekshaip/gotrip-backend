package com.gotrip.common_library.dto.user;

import java.time.LocalDate;

public record UserResponseDTO(
        Long userId,
        String email,
        String name,
        String phone,
        String gender,
        LocalDate dob,
        boolean traveller,
        boolean serviceProvider,
        boolean admin
) {
}