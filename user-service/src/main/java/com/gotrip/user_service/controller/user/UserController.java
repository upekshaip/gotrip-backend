package com.gotrip.user_service.controller.user;


import com.gotrip.common_library.dto.error.ApiErrorResponse;
import com.gotrip.user_service.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

    public UserController(UserRepository userRepository) {
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMyDetails(Authentication authentication) {
        try {
        if (authentication == null) {
           throw new Exception("Authorization is null");
        }
        // This is the user Map we extracted from the JSON string in the JWT
        return ResponseEntity.ok(authentication.getPrincipal());
        }  catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ApiErrorResponse(
                            e.getMessage(),
                            HttpStatus.BAD_REQUEST.value(),
                            System.currentTimeMillis()
                    )

            );
        }
    }
}
