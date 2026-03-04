package com.gotrip.transport_service.repository;

import com.gotrip.transport_service.model.TransportReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface TransportReviewRepository extends JpaRepository<TransportReview, Long> {

    // Get all reviews for a specific transport vehicle (ordered by newest first)
    List<TransportReview> findByTransport_TransportIdOrderByCreatedAtDesc(Long transportId);

    // Check if a traveler has already reviewed a specific booking
    boolean existsByBookingId(Long bookingId);

    // Calculate the average rating for a transport vehicle
    @Query("SELECT AVG(r.rating) FROM TransportReview r WHERE r.transport.transportId = :transportId")
    Double getAverageRatingForTransport(@Param("transportId") Long transportId);

    // Used for hard-deleting reviews when a transport vehicle is removed
    @Modifying
    @Transactional
    @Query("DELETE FROM TransportReview r WHERE r.transport.transportId = :transportId")
    void deleteAllByTransport_TransportId(@Param("transportId") Long transportId);
}