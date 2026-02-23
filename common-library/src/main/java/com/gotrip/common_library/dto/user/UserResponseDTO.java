package com.gotrip.common_library.dto.user;

public record UserResponseDTO(Long userId,
                              String email,
                              String name,
                              boolean isTraveller,
                              boolean isServiceProvider,
                              boolean isAdmin) {
}
