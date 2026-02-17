package com.gotrip.backend.controller.auth;


import com.gotrip.backend.dto.auth.UserSignupRequest;
import com.gotrip.backend.dto.error.ApiErrorResponse;
import com.gotrip.backend.model.User;
import com.gotrip.backend.service.auth.SignupService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class SignupController {

    private final SignupService signupService;

    public SignupController(SignupService signupService) {
        this.signupService = signupService;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@RequestBody UserSignupRequest request) {
        try {
            User createdUser = signupService.signUp(request);
            return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            ApiErrorResponse error = new ApiErrorResponse(
                    e.getMessage(),
                    HttpStatus.BAD_REQUEST.value(),
                    System.currentTimeMillis()
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
}