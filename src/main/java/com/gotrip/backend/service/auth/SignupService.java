package com.gotrip.backend.service.auth;


import com.gotrip.backend.dto.auth.UserSignupRequest;
import com.gotrip.backend.model.User;
import com.gotrip.backend.model.TravellerProfile;
import com.gotrip.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SignupService {

    private final UserRepository userRepository;

    public SignupService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public User signUp(UserSignupRequest request) {
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

        TravellerProfile travellerProfile = new TravellerProfile();
        travellerProfile.setUser(user);

        user.setTravellerProfile(travellerProfile);

        return userRepository.save(user);
    }
}