package com.gotrip.restaurant_service.repository;

import com.gotrip.common_library.dto.restaurant_service.enums.RestaurantStatus;
import com.gotrip.restaurant_service.model.Restaurant;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    // Find all restaurants that are NOT removed (for general browsing)
    List<Restaurant> findByStatusNot(RestaurantStatus status);

    // Inside RestaurantRepository.java
    Page<Restaurant> findByStatus(RestaurantStatus status, Pageable pageable);

    Page<Restaurant> findAll(Pageable pageable);

    // Find a specific restaurant by ID but only if it's not removed
    Optional<Restaurant> findByRestaurantIdAndStatusNot(Long restaurantId, RestaurantStatus status);

    // Find restaurants for a specific city that are ACTIVE
    List<Restaurant> findByCityIgnoreCaseAndStatus(String city, RestaurantStatus status);

    // Find all restaurants owned by a specific provider
    List<Restaurant> findByProviderIdAndStatusNot(Long providerId, RestaurantStatus status);

    // Find featured restaurants that are active
    List<Restaurant> findByIsFeaturedTrueAndStatus(RestaurantStatus status);

    // Spring Data JPA needs this specific Pageable to handle the SQL "LIMIT" and "OFFSET"
    Page<Restaurant> findByProviderIdAndStatusAndStatusNot(
            Long providerId,
            RestaurantStatus status,
            RestaurantStatus excludeStatus,
            Pageable pageable
    );

    Page<Restaurant> findByProviderIdAndStatusNot(
            Long providerId,
            RestaurantStatus excludeStatus,
            Pageable pageable
    );

    // In RestaurantRepository.java

}