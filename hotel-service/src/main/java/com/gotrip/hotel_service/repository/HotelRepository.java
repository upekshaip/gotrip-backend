package com.gotrip.hotel_service.repository;


import com.gotrip.hotel_service.model.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HotelRepository extends JpaRepository<Hotel, Long> {

    // Find all active hotels in a specific city
    List<Hotel> findByCityAndIsActiveTrue(String city);

    // Find all hotels owned by a specific service provider
    List<Hotel> findByProviderId(Long providerId);

    // Get featured hotels for the landing page
    List<Hotel> findByIsFeaturedTrueAndIsActiveTrue();
}