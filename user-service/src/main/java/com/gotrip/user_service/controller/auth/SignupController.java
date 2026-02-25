package com.gotrip.user_service.controller.auth;


import com.gotrip.common_library.config.AppConfig;
import com.gotrip.common_library.dto.auth.UserLoginRequest;
import com.gotrip.common_library.dto.auth.UserSignupRequest;
import com.gotrip.common_library.dto.auth.UserSignupUpdateRequest;
import com.gotrip.common_library.dto.error.ApiErrorResponse;
import com.gotrip.user_service.service.SignupService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class SignupController {

    private final SignupService signupService;


    public SignupController(SignupService signupService) {
        this.signupService = signupService;
    }

    @GetMapping("/refresh")
    public ResponseEntity<?> refresh
            (@CookieValue(name = "jwt", required = false) String refreshToken,
             HttpServletResponse response) {
        try {
//            refresh token handler
            if (refreshToken == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("UNAUTHORIZED");
            }
            Map<String, Object> accessToken = signupService.refresh(refreshToken);

//            modify the accessToken
            Cookie accessCookie = new Cookie("accessToken", accessToken.get("accessToken").toString());;
            accessCookie.setPath("/");
            accessCookie.setMaxAge(AppConfig.ACCESS_TOKEN_EXPIRATION); // 5 mins
            accessCookie.setHttpOnly(false);
            response.addCookie(accessCookie);

            return ResponseEntity.ok(accessToken);
        }
        catch (Exception e) {
            ApiErrorResponse error = new ApiErrorResponse(
                    e.getMessage(),
                    HttpStatus.BAD_REQUEST.value(),
                    System.currentTimeMillis()
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@RequestBody UserSignupRequest request, HttpServletResponse response) {
        try {
            Map<String, Object> signupData = signupService.signUp(request);
            Cookie accessCookie = new Cookie("accessToken", signupData.get("accessToken").toString());;
            accessCookie.setPath("/");
            accessCookie.setMaxAge(AppConfig.ACCESS_TOKEN_EXPIRATION); // 5 mins
             accessCookie.setHttpOnly(false);
            response.addCookie(accessCookie);

            Cookie refreshCookie = new Cookie("jwt", signupData.get("refreshToken").toString());
            refreshCookie.setHttpOnly(true);
            refreshCookie.setSecure(true); // Only for HTTPS
            refreshCookie.setPath("/");
            refreshCookie.setMaxAge((int) (AppConfig.REFRESH_TOKEN_EXPIRATION));
            response.addCookie(refreshCookie);

            Map<String, Object> responseBody = Map.of(
                    "user", signupData.get("user"),
                    "accessToken", signupData.get("accessToken").toString(),
                    "refreshToken", signupData.get("refreshToken").toString()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(responseBody);

        } catch (RuntimeException e) {
            ApiErrorResponse error = new ApiErrorResponse(
                    e.getMessage(),
                    HttpStatus.BAD_REQUEST.value(),
                    System.currentTimeMillis()
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @PatchMapping("/signup")
    public ResponseEntity<?> signUpUpdate(Authentication authentication, @RequestBody UserSignupUpdateRequest request, HttpServletResponse response) {
        try {
            Map<String, Object> updatedData = signupService.signupUpdate(authentication, request);

            Cookie accessCookie = new Cookie("accessToken", updatedData.get("accessToken").toString());;
            accessCookie.setPath("/");
            accessCookie.setMaxAge(AppConfig.ACCESS_TOKEN_EXPIRATION); // 5 mins
            accessCookie.setHttpOnly(false);
            response.addCookie(accessCookie);

            Map<String, Object> responseBody = Map.of(
                    "user", updatedData.get("user"),
                    "accessToken", updatedData.get("accessToken").toString()
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(responseBody);

        } catch (Exception e) {
            ApiErrorResponse error = new ApiErrorResponse(
                    e.getMessage(),
                    HttpStatus.BAD_REQUEST.value(),
                    System.currentTimeMillis()
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserLoginRequest request, HttpServletResponse response) {
        try {
            Map<String, Object> loginData = signupService.login(request);

            Cookie accessCookie = new Cookie("accessToken", loginData.get("accessToken").toString());;
            accessCookie.setPath("/");
            accessCookie.setMaxAge(AppConfig.ACCESS_TOKEN_EXPIRATION); // 5 mins
            accessCookie.setHttpOnly(false);
            response.addCookie(accessCookie);

            Cookie refreshCookie = new Cookie("jwt", loginData.get("refreshToken").toString());
            refreshCookie.setHttpOnly(true);
            refreshCookie.setSecure(true); // Only for HTTPS
            refreshCookie.setPath("/");
            refreshCookie.setMaxAge((int) (AppConfig.REFRESH_TOKEN_EXPIRATION));
            response.addCookie(refreshCookie);

            Map<String, Object> responseBody = Map.of(
                    "user", loginData.get("user"),
                    "accessToken", loginData.get("accessToken").toString(),
                    "refreshToken", loginData.get("refreshToken").toString()
            );
            return ResponseEntity.status(HttpStatus.OK).body(responseBody);

        } catch (Exception e) {
            ApiErrorResponse error = new ApiErrorResponse(
                    e.getMessage(),
                    HttpStatus.BAD_REQUEST.value(),
                    System.currentTimeMillis()
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
}