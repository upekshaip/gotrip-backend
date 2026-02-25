package com.gotrip.user_service.service;



import com.gotrip.common_library.dto.auth.UserLoginRequest;
import com.gotrip.common_library.dto.auth.UserSignupRequest;
import com.gotrip.common_library.dto.auth.UserSignupUpdateRequest;
import com.gotrip.common_library.dto.user.UserProfileUpdateRequest;
import com.gotrip.common_library.service.JWTService;
import com.gotrip.user_service.model.TravellerProfile;
import com.gotrip.user_service.model.User;
import com.gotrip.user_service.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Service
public class SignupService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JWTService jWTService;
    private final ObjectMapper objectMapper;


    public SignupService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, JWTService jWTService, ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jWTService = jWTService;
        this.objectMapper = objectMapper;
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

        Map<String, Object> refreshToken = jWTService.generateRefreshToken();
        user.setRefreshToken(refreshToken.get("token").toString());
        user.setRefreshTokenExpiry((LocalDateTime) refreshToken.get("expiration"));

        User createdUser = userRepository.save(user);
        String userJson = objectMapper.writeValueAsString(createdUser);
        String accessToken = jWTService.generateAccessToken(createdUser.getUserId(), createdUser.getEmail(), userJson);

        return Map.of(
                "user", createdUser,
                "accessToken", accessToken,
                "refreshToken", refreshToken.get("token"),
                "refreshExpiration", refreshToken.get("expiration")
        );
    }

    @Transactional
    public Map<String, Object> signupUpdate(Authentication auth, UserSignupUpdateRequest request) throws Exception {
        if (auth == null) {
            throw new Exception("Authorization is null");
        }
        if (request.name().isEmpty() || request.gender().isEmpty() || request.dob().isEmpty() || request.phone().isEmpty()) {
            throw new Exception("Name or gender or phone or dob is empty");
        }
        Map<String, Object> userDetails = (Map<String, Object>) auth.getPrincipal();
        Optional<User> user = userRepository.findByEmail(userDetails.get("email").toString());
        if (user.isEmpty()) {
            throw new RuntimeException("User not found with email");
        }
        // convert string date into localdate
        LocalDate _dob = LocalDate.parse(request.dob());
        User myUser = user.get();
        myUser.setName(request.name());
        myUser.setGender(request.gender());
        myUser.setPhone(request.phone());
        myUser.setDob(_dob);

        User modUser = userRepository.save(myUser);
        String userJson = objectMapper.writeValueAsString(modUser);
        String accessToken = jWTService.generateAccessToken(modUser.getUserId(), modUser.getEmail(), userJson);
        return Map.of(
                "user", modUser,
                "accessToken", accessToken);
    }

    @Transactional
    public Map<String, Object> login(UserLoginRequest request) {
        Optional<User> myUser = userRepository.findByEmail(request.email());
        if (myUser.isEmpty()) {
            throw new RuntimeException("Invalid email or password");
        }
        User user = myUser.get();
        boolean isMatch = passwordEncoder.matches(request.password(), user.getPassword());
        if (!isMatch) {
            throw new RuntimeException("Invalid email or password");
        }

        // log user in
        Map<String, Object> refreshToken = jWTService.generateRefreshToken();

        user.setRefreshToken(refreshToken.get("token").toString());
        user.setRefreshTokenExpiry((LocalDateTime) refreshToken.get("expiration"));

        User createdUser = userRepository.save(user);
        String userJson = objectMapper.writeValueAsString(createdUser);
        String accessToken = jWTService.generateAccessToken(createdUser.getUserId(), createdUser.getEmail(), userJson);
        // update user
        return Map.of(
                "user", createdUser,
                "accessToken", accessToken,
                "refreshToken", refreshToken.get("token"),
                "refreshExpiration", refreshToken.get("expiration")
        );
    }

    public Map<String, Object> refresh(String refreshToken) {
        Optional<User> myUser = userRepository.findByRefreshToken(refreshToken);
        if (myUser.isEmpty()) {
            throw new RuntimeException("Invalid refresh token");
        }
        User user = myUser.get();
        LocalDateTime _exp = user.getRefreshTokenExpiry();
        LocalDateTime  _now = LocalDateTime.now();
        boolean _valid = _now.isBefore(_exp);
        if (!_valid) {
            throw new RuntimeException("Invalid refresh token");
        }
        String userJson = objectMapper.writeValueAsString(user);
        String accessToken = jWTService.generateAccessToken(user.getUserId(), user.getEmail(), userJson);
        return Map.of("accessToken", accessToken);
    }

    // Inside SignupService.java

    @Transactional
    public Map<String, Object> updateProfile(Authentication auth, UserProfileUpdateRequest request) throws Exception {
        if (auth == null) throw new Exception("Authorization is null");

        // Extract user details from JWT principal
        Map<String, Object> userDetails = (Map<String, Object>) auth.getPrincipal();
        User user = userRepository.findByEmail(userDetails.get("email").toString())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Update fields if they are provided in the request
        if (request.name() != null) user.setName(request.name());
        if (request.phone() != null) user.setPhone(request.phone());
        if (request.gender() != null) user.setGender(request.gender());
        if (request.dob() != null) {
            user.setDob(LocalDate.parse(request.dob()));
        }

        User updatedUser = userRepository.save(user);

        // Regenerate Access Token so the frontend has the latest user data in the JWT
        String userJson = objectMapper.writeValueAsString(updatedUser);
        String accessToken = jWTService.generateAccessToken(updatedUser.getUserId(), updatedUser.getEmail(), userJson);

        return Map.of(
            "user", updatedUser,
            "accessToken", accessToken
        );
    }

    @Transactional(readOnly = true)
    public User getFullProfile(Authentication auth) throws Exception {
        if (auth == null) {
            throw new Exception("Authorization is null");
        }

        // Extract email from the JWT principal
        Map<String, Object> userDetails = (Map<String, Object>) auth.getPrincipal();
        String email = userDetails.get("email").toString();

        // Fetch from DB to get the most recent data and relations
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }
}