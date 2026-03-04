package com.gotrip.common_library.dto.user;

public record TravellerContactInfo(
        String name,
        String email,
        String phone,
        String gender
) {}