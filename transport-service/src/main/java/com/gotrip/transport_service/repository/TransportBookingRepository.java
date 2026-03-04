package com.gotrip.transport_service.repository;

import com.gotrip.common_library.dto.transport_service.enums.BookingStatus;
import com.gotrip.transport_service.model.TransportBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransportBookingRepository extends JpaRepository<TransportBooking, Long> {

    // For the traveler's "My Bookings" page
    List<TransportBooking> findByTravellerId(Long travellerId);

    // For the provider's dashboard to see requests for their vehicles
    List<TransportBooking> findByTransportId(Long transportId);

    // Find pending requests for a provider to approve/reject
    List<TransportBooking> findByTransportIdAndStatus(Long transportId, BookingStatus status);

    // Find a booking by its unique reference (e.g., TR-2026-X89)
    Optional<TransportBooking> findByBookingReference(String bookingReference);

    // Find bookings eligible for review (Completed rides that don't have a reviewId yet)
    List<TransportBooking> findByTravellerIdAndStatusAndReviewIdIsNull(Long travellerId, BookingStatus status);
}