package com.gotrip.user_service.controller.user;


import com.gotrip.common_library.config.AppConfig;
import com.gotrip.common_library.dto.error.ApiErrorResponse;
import com.gotrip.common_library.dto.user.UserProfileUpdateRequest;
import com.gotrip.user_service.model.User;
import com.gotrip.user_service.repository.UserRepository;
import com.gotrip.user_service.service.SignupService;
import com.gotrip.user_service.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    private final ObjectMapper objectMapper;
    private final SignupService signupService;
    private final UserService userService;

    public UserController(UserRepository userRepository, ObjectMapper objectMapper, SignupService signupService, UserService userService) {
        this.objectMapper = objectMapper;
        this.signupService = signupService;
        this.userService = userService;
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

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(Authentication authentication) {
        try {
            User user = signupService.getFullProfile(authentication);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ApiErrorResponse(
                            e.getMessage(),
                            HttpStatus.NOT_FOUND.value(),
                            System.currentTimeMillis()
                    )
            );
        }
    }

    @PatchMapping("/update-profile")
    public ResponseEntity<?> updateProfile(
            Authentication authentication,
            @RequestBody UserProfileUpdateRequest request,
            HttpServletResponse response) {
        try {
            Map<String, Object> updatedData = signupService.updateProfile(authentication, request);

            // Update the AccessToken Cookie so the browser has the latest data
            Cookie accessCookie = new Cookie("accessToken", updatedData.get("accessToken").toString());
            accessCookie.setPath("/");
            accessCookie.setMaxAge(AppConfig.ACCESS_TOKEN_EXPIRATION);
            accessCookie.setHttpOnly(false); // Accessible by frontend if needed
            response.addCookie(accessCookie);

            return ResponseEntity.ok(Map.of(
                    "user", updatedData.get("user"),
                    "accessToken", updatedData.get("accessToken")
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ApiErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value(), System.currentTimeMillis())
            );
        }
    }

    // Internal call to get traveller contact info
    @GetMapping("/internal/traveller/{travellerId}")
    public ResponseEntity<?> getTravellerInfo(@PathVariable Long travellerId) {
        try {
            // signupService should have a method to find User by travellerId
            return ResponseEntity.ok(userService.getTravellerContact(travellerId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // Internal call to get provider contact info
    @GetMapping("/internal/provider/{providerId}")
    public ResponseEntity<?> getProviderInfo(@PathVariable Long providerId) {
        try {
            // signupService should have a method to find User by providerId
            return ResponseEntity.ok(userService.getProviderContact(providerId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
