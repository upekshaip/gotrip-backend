package com.gotrip.backend.controller.auth;


import com.gotrip.backend.config.AppConfig;
import com.gotrip.backend.dto.auth.UserLoginRequest;
import com.gotrip.backend.dto.auth.UserSignupRequest;
import com.gotrip.backend.dto.auth.UserSignupUpdateRequest;
import com.gotrip.backend.dto.error.ApiErrorResponse;
import com.gotrip.backend.model.User;
import com.gotrip.backend.service.auth.SignupService;
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

            return ResponseEntity.status(HttpStatus.CREATED).body(signupData.get("user"));

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
            return ResponseEntity.status(HttpStatus.CREATED).body(updatedData.get("user"));

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

            return ResponseEntity.status(HttpStatus.OK).body(loginData.get("user"));

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