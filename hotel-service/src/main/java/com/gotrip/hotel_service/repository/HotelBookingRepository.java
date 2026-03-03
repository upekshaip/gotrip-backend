package com.gotrip.hotel_service.repository;

import com.gotrip.common_library.dto.hotel_service.enums.BookingStatus;
import com.gotrip.hotel_service.model.HotelBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface HotelBookingRepository extends JpaRepository<HotelBooking, Long> {

    // For the traveler's "My Bookings" page
    List<HotelBooking> findByTravellerId(Long travellerId);

    // For the provider's dashboard to see requests for their hotel
    List<HotelBooking> findByHotelId(Long hotelId);

    // Find pending requests for a provider to approve/reject
    List<HotelBooking> findByHotelIdAndStatus(Long hotelId, BookingStatus status);

    // Find a booking by its unique reference (e.g., GT-2026-X89)
    Optional<HotelBooking> findByBookingReference(String bookingReference);

    // Find bookings eligible for review (Completed stays that don't have a reviewId yet)
    List<HotelBooking> findByTravellerIdAndStatusAndReviewIdIsNull(Long travellerId, BookingStatus status);
}