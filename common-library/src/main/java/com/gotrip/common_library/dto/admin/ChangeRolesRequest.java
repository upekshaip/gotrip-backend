package com.gotrip.common_library.dto.admin;

import com.gotrip.common_library.dto.admin.enums.UserRoles;

public record ChangeRolesRequest(
        Long userId,
        UserRoles role
) {
}
