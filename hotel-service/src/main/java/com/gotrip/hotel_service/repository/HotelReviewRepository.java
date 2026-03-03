package com.gotrip.hotel_service.repository;

import com.gotrip.hotel_service.model.HotelReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface HotelReviewRepository extends JpaRepository<HotelReview, Long> {

    // Get all reviews for a specific hotel (ordered by newest first)
    List<HotelReview> findByHotel_HotelIdOrderByCreatedAtDesc(Long hotelId);

    // Check if a traveler has already reviewed a specific booking
    boolean existsByBookingId(Long bookingId);

    // Calculate the average rating for a hotel to update the Hotel listing
    @Query("SELECT AVG(r.rating) FROM HotelReview r WHERE r.hotel.hotelId = :hotelId")
    Double getAverageRatingForHotel(@Param("hotelId") Long hotelId);
}