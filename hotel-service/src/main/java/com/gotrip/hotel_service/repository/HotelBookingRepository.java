package com.gotrip.hotel_service.repository;

import com.gotrip.common_library.dto.hotel_service.enums.BookingStatus;
import com.gotrip.hotel_service.model.HotelBooking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HotelBookingRepository extends JpaRepository<HotelBooking, Long> {

    // Fixes "Cannot resolve symbol 'findByProviderIdAndStatus'"
    Page<HotelBooking> findByProviderIdAndStatus(Long providerId, BookingStatus status, Pageable pageable);

    // Fixes "Cannot resolve symbol 'findByProviderId'"
    Page<HotelBooking> findByProviderId(Long providerId, Pageable pageable);

    // Add these for the traveller side as well
    Page<HotelBooking> findByTravellerId(Long travellerId, Pageable pageable);
    Page<HotelBooking> findByTravellerIdAndStatus(Long travellerId, BookingStatus status, Pageable pageable);
}