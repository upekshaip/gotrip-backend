package com.gotrip.user_service.repository;


import com.gotrip.user_service.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.travellerProfile WHERE u.traveller = true")
    Page<User> findAllTravellers(Pageable pageable);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.serviceProviderProfile WHERE u.serviceProvider = true")
    Page<User> findAllProviders(Pageable pageable);
}
