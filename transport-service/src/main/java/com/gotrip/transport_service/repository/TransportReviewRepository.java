package com.gotrip.transport_service.repository;

import com.gotrip.transport_service.model.TransportReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransportReviewRepository extends JpaRepository<TransportReview, Long> {
    List<TransportReview> findByTransportId(Long transportId);
    boolean existsByBookingId(Long bookingId); // Prevents them from reviewing the same ride twice
}