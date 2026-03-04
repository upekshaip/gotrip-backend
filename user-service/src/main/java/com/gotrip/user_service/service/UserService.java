package com.gotrip.user_service.service;

import com.gotrip.common_library.service.JWTService;
import com.gotrip.user_service.model.User;
import com.gotrip.user_service.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tools.jackson.databind.ObjectMapper;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final JWTService jWTService;
    private final ObjectMapper objectMapper;

    public UserService(UserRepository userRepository, JWTService jWTService, ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.jWTService = jWTService;
        this.objectMapper = objectMapper;

    }

    public User getTravellerContact(Long travellerId) {
        return userRepository.findByTravellerProfile_TravellerId(travellerId)
                .orElseThrow(() -> new RuntimeException("Traveller not found"));
    }

    public User getProviderContact(Long providerId) {
        return userRepository.findByServiceProviderProfile_ProviderId(providerId)
                .orElseThrow(() -> new RuntimeException("Provider not found"));
    }
}
