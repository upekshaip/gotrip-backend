package com.gotrip.user_service.service;

import com.gotrip.common_library.dto.admin.ChangeRolesRequest;
import com.gotrip.common_library.dto.admin.EditUserRequest;
import com.gotrip.common_library.dto.admin.enums.UserRoles;
import com.gotrip.common_library.service.JWTService;
import com.gotrip.user_service.model.AdminProfile;
import com.gotrip.user_service.model.ServiceProviderProfile;
import com.gotrip.user_service.model.TravellerProfile;
import com.gotrip.user_service.model.User;
import com.gotrip.user_service.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

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


    public Page<User> getAllTravellers(Authentication authentication, int page, int limit) {
        Long adminId = extractAdminId(authentication);

        Pageable pageable = PageRequest.of(page - 1, limit,
                Sort.by(Sort.Direction.DESC, "createdAt"));
        return userRepository.findAllTravellers(pageable);
    }

    public Page<User> getAllProviders(Authentication authentication,int page, int limit) {
        Long adminId = extractAdminId(authentication);

        Pageable pageable = PageRequest.of(page - 1, limit,
                Sort.by(Sort.Direction.DESC, "createdAt"));
        return userRepository.findAllProviders(pageable);
    }



    public User editUser(Authentication authentication, EditUserRequest editUserReq) {
        Long adminId = extractAdminId(authentication);
        User user = userRepository.findById(editUserReq.userId())
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + editUserReq.userId()));

        if (editUserReq.name() != null) user.setName(editUserReq.name());
        if (editUserReq.phone() != null) user.setPhone(editUserReq.phone());
        if (editUserReq.gender() != null) user.setGender(editUserReq.gender());

        if (editUserReq.dob() != null) {
            user.setDob(java.time.LocalDate.parse(editUserReq.dob()));
        }

        return userRepository.save(user);
    }

    public User changeRoles(Authentication authentication, ChangeRolesRequest changeRolesReq) {
        extractAdminId(authentication);

        User user = userRepository.findById(changeRolesReq.userId())
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + changeRolesReq.userId()));

        switch (changeRolesReq.role()) {
            case ADMIN:
                user.setAdmin(true);
                user.setServiceProvider(true);
                user.setTraveller(true);
                ensureAdminProfileExists(user);
                ensureServiceProviderProfileExists(user);
                ensureTravellerProfileExists(user);
                break;

            case SERVICE_PROVIDER:
                user.setAdmin(false); // Explicitly disable the role flag
                user.setServiceProvider(true);
                user.setTraveller(true);

                // Detach the Admin Profile reference
                if (user.getAdminProfile() != null) {
                    user.getAdminProfile().setUser(null); // Clear the back-reference
                    user.setAdminProfile(null);           // Clear the forward-reference
                }

                ensureServiceProviderProfileExists(user);
                ensureTravellerProfileExists(user);
                break;

            case TRAVELLER:
                user.setAdmin(false);
                user.setServiceProvider(false);
                user.setTraveller(true);

                // Detach Admin Profile
                if (user.getAdminProfile() != null) {
                    user.getAdminProfile().setUser(null);
                    user.setAdminProfile(null);
                }

                // Detach Service Provider Profile
                if (user.getServiceProviderProfile() != null) {
                    user.getServiceProviderProfile().setUser(null);
                    user.setServiceProviderProfile(null);
                }

                ensureTravellerProfileExists(user);
                break;
        }

        return userRepository.save(user);
    }

    private void ensureAdminProfileExists(User user) {
        // 1. Check if the object already has it (Active)
        if (user.getAdminProfile() == null) {
            // 2. Check the DB for an orphaned profile belonging to this User email or ID
            // Or simply create a new one if you prefer fresh profiles every time
            AdminProfile profile = new AdminProfile();
            profile.setUser(user);
            user.setAdminProfile(profile);
        } else {
            // 3. If it exists but was detached, restore the link
            user.getAdminProfile().setUser(user);
        }
    }

    private void ensureServiceProviderProfileExists(User user) {
        if (user.getServiceProviderProfile() == null) {
            ServiceProviderProfile profile = new ServiceProviderProfile();
            profile.setUser(user);
            user.setServiceProviderProfile(profile);
        } else {
            user.getServiceProviderProfile().setUser(user);
        }
    }

    private void ensureTravellerProfileExists(User user) {
        if (user.getTravellerProfile() == null) {
            TravellerProfile profile = new TravellerProfile();
            profile.setUser(user);
            user.setTravellerProfile(profile);
        } else {
            user.getTravellerProfile().setUser(user);
        }
    }



    public User getTravellerContact(Long travellerId) {
        return userRepository.findByTravellerProfile_TravellerId(travellerId)
                .orElseThrow(() -> new RuntimeException("Traveller not found"));
    }

    public User getProviderContact(Long providerId) {
        return userRepository.findByServiceProviderProfile_ProviderId(providerId)
                .orElseThrow(() -> new RuntimeException("Provider not found"));
    }

    private Long extractTravellerId(Authentication auth) {
        Map<String, Object> principal = (Map<String, Object>) auth.getPrincipal();
        if (!(boolean) principal.getOrDefault("traveller", false)) {
            throw new RuntimeException("Unauthorized: You are not authorized to perform this operation..");
        }
        Map<String, Object> profile = (Map<String, Object>) principal.get("travellerProfile");
        return ((Number) profile.get("travellerId")).longValue();
    }

    private Long extractProviderId(Authentication auth) {
        Map<String, Object> principal = (Map<String, Object>) auth.getPrincipal();
        if (!(boolean) principal.getOrDefault("serviceProvider", false)) {
            throw new RuntimeException("Unauthorized: Only Service Providers can manage listings.");
        }
        Map<String, Object> profile = (Map<String, Object>) principal.get("serviceProviderProfile");
        return ((Number) profile.get("providerId")).longValue();
    }

    private Long extractAdminId(Authentication auth) {
        Map<String, Object> principal = (Map<String, Object>) auth.getPrincipal();
        if (!(boolean) principal.getOrDefault("admin", false)) {
            throw new RuntimeException("Unauthorized: Only admins can manage listings.");
        }
        Map<String, Object> profile = (Map<String, Object>) principal.get("adminProfile");
        return ((Number) profile.get("adminId")).longValue();
    }
}
