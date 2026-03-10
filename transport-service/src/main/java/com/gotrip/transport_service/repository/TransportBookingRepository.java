package com.gotrip.transport_service.repository;

import com.gotrip.common_library.dto.hotel_service.enums.BookingStatus;
import com.gotrip.transport_service.model.TransportBooking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransportBookingRepository extends JpaRepository<TransportBooking, Long> {

    // For the traveler's "My Bookings" page
    Page<TransportBooking> findByTravellerId(Long travellerId, Pageable pageable);

    Page<TransportBooking> findByTravellerIdAndStatus(Long travellerId, BookingStatus status, Pageable pageable);

    // For the provider's dashboard
    Page<TransportBooking> findByProviderIdAndStatus(Long providerId, BookingStatus status, Pageable pageable);

    Page<TransportBooking> findByProviderId(Long providerId, Pageable pageable);

    // Find a booking by its unique reference (e.g., TR-2026-X89)
    Optional<TransportBooking> findByBookingReference(String bookingReference);

    // Find bookings eligible for review (Completed rides that don't have a reviewId yet)
    List<TransportBooking> findByTravellerIdAndStatusAndReviewIdIsNull(Long travellerId, BookingStatus status);

    long countByStatus(BookingStatus status);
}