package com.gotrip.restaurant_service.repository;

import com.gotrip.common_library.dto.hotel_service.enums.BookingStatus;
import com.gotrip.restaurant_service.model.RestaurantBooking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RestaurantBookingRepository extends JpaRepository<RestaurantBooking, Long> {

    Page<RestaurantBooking> findByProviderIdAndStatus(Long providerId, BookingStatus status, Pageable pageable);

    Page<RestaurantBooking> findByProviderId(Long providerId, Pageable pageable);

    Page<RestaurantBooking> findByTravellerId(Long travellerId, Pageable pageable);
    Page<RestaurantBooking> findByTravellerIdAndStatus(Long travellerId, BookingStatus status, Pageable pageable);
}
