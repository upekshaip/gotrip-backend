package com.gotrip.user_service.repository;


import com.gotrip.user_service.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Basic CRUD (Save, Delete, FindByID) is already included!
    Optional<User> findByEmail(String email);

    Optional<User> findByRefreshToken(String refreshToken);

    boolean existsByEmail(String email);

    // Finds user by the ID inside the nested TravellerProfile object
    Optional<User> findByTravellerProfile_TravellerId(Long travellerId);

    // Finds user by the ID inside the nested ServiceProviderProfile object
    Optional<User> findByServiceProviderProfile_ProviderId(Long providerId);
}
