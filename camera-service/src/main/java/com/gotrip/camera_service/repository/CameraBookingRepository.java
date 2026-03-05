package com.gotrip.camera_service.repository;

import com.gotrip.common_library.dto.camera_service.enums.BookingStatus;
import com.gotrip.camera_service.model.CameraBooking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CameraBookingRepository extends JpaRepository<CameraBooking, Long> {

    // Fixes "Cannot resolve symbol 'findByProviderIdAndStatus'"
    Page<CameraBooking> findByProviderIdAndStatus(Long providerId, BookingStatus status, Pageable pageable);

    // Fixes "Cannot resolve symbol 'findByProviderId'"
    Page<CameraBooking> findByProviderId(Long providerId, Pageable pageable);

    // Add these for the traveller side as well
    Page<CameraBooking> findByTravellerId(Long travellerId, Pageable pageable);
    Page<CameraBooking> findByTravellerIdAndStatus(Long travellerId, BookingStatus status, Pageable pageable);
}