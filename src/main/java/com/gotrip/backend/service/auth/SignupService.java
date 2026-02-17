package com.gotrip.backend.service.auth;


import com.gotrip.backend.dto.auth.UserLoginRequest;
import com.gotrip.backend.dto.auth.UserSignupRequest;
import com.gotrip.backend.model.User;
import com.gotrip.backend.model.TravellerProfile;
import com.gotrip.backend.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Service
public class SignupService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JWTService jWTService;


    public SignupService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, JWTService jWTService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jWTService = jWTService;
    }

    @Transactional
    public Map<String, Object> signUp(UserSignupRequest request) {
        boolean emailExists = userRepository.existsByEmail(request.email());
        if (emailExists) {
            throw new RuntimeException("User with email " + request.email() + " already exists.");
        }

        User user = new User();
        user.setEmail(request.email());
        user.setPassword(request.password());
        user.setTraveller(true);
        user.setAdmin(false);
        user.setServiceProvider(false);

        String encodedPassword = passwordEncoder.encode(request.password());
        user.setPassword(encodedPassword);

        TravellerProfile travellerProfile = new TravellerProfile();
        travellerProfile.setUser(user);
        user.setTravellerProfile(travellerProfile);

        String accessToken = jWTService.generateAccessToken(user);
        Map<String, Object> refreshToken = jWTService.generateRefreshToken();

        user.setRefreshToken(refreshToken.get("token").toString());
        user.setRefreshTokenExpiry((LocalDateTime) refreshToken.get("expiration"));

        var created = userRepository.save(user);
        return Map.of(
                "user", created,
                "accessToken", accessToken,
                "refreshToken", refreshToken.get("token"),
                "refreshExpiration", refreshToken.get("expiration")
        );
    }

    @Transactional
    public User login(UserLoginRequest request) {
        Optional<User> myUser = userRepository.findByEmail(request.email());
        if (myUser.isEmpty()) {
            throw new RuntimeException("Invalid email or password");
        }
        User user = myUser.get();
        boolean isMatch = passwordEncoder.matches(request.password(), user.getPassword());
        if (!isMatch) {
            throw new RuntimeException("Invalid email or password");
        }
        return user;
    }
}