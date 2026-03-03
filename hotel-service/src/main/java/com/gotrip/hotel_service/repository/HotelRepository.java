package com.gotrip.hotel_service.repository;

import com.gotrip.hotel_service.model.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface HotelRepository extends JpaRepository<Hotel, Long> {

    // Find active hotels in a city for the traveler search
    List<Hotel> findByCityIgnoreCaseAndIsActiveTrue(String city);

    // Find all hotels belonging to a specific provider
    List<Hotel> findByProviderId(Long providerId);

    // Get featured hotels for the landing page
    List<Hotel> findByIsFeaturedTrueAndIsActiveTrue();
}