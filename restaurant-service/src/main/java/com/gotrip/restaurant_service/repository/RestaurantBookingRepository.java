package com.gotrip.restaurant_service.repository;

import com.gotrip.common_library.dto.restaurant_service.enums.BookingStatus;
import com.gotrip.restaurant_service.model.RestaurantBooking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RestaurantBookingRepository extends JpaRepository<RestaurantBooking, Long> {

    // Fixes "Cannot resolve symbol 'findByProviderIdAndStatus'"
    Page<RestaurantBooking> findByProviderIdAndStatus(Long providerId, BookingStatus status, Pageable pageable);

    // Fixes "Cannot resolve symbol 'findByProviderId'"
    Page<RestaurantBooking> findByProviderId(Long providerId, Pageable pageable);

    // Add these for the traveller side as well
    Page<RestaurantBooking> findByTravellerId(Long travellerId, Pageable pageable);
    Page<RestaurantBooking> findByTravellerIdAndStatus(Long travellerId, BookingStatus status, Pageable pageable);
}