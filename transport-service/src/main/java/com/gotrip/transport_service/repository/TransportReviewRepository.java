package com.gotrip.transport_service.repository;

import com.gotrip.transport_service.model.TransportReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface TransportReviewRepository extends JpaRepository<TransportReview, Long> {
    List<TransportReview> findByTransport_TransportId(Long transportId);
    boolean existsByBookingId(Long bookingId); // Prevents them from reviewing the same ride twice

    @Modifying
    @Transactional
    @Query("DELETE FROM TransportReview r WHERE r.transport.transportId = :transportId")
    void deleteAllByTransport_TransportId(@Param("transportId") Long transportId);
}