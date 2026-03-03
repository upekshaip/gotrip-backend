package com.gotrip.hotel_service.repository;

import com.gotrip.common_library.dto.hotel_service.enums.HotelStatus;
import com.gotrip.hotel_service.model.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HotelRepository extends JpaRepository<Hotel, Long> {

    // Find all hotels that are NOT removed (for general browsing)
    List<Hotel> findByStatusNot(HotelStatus status);

    // Find a specific hotel by ID but only if it's not removed
    Optional<Hotel> findByHotelIdAndStatusNot(Long hotelId, HotelStatus status);

    // Find hotels for a specific city that are ACTIVE
    List<Hotel> findByCityIgnoreCaseAndStatus(String city, HotelStatus status);

    // Find all hotels owned by a specific provider
    List<Hotel> findByProviderIdAndStatusNot(Long providerId, HotelStatus status);

    // Find featured hotels that are active
    List<Hotel> findByIsFeaturedTrueAndStatus(HotelStatus status);
}