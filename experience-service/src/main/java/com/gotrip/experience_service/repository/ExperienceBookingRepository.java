package com.gotrip.experience_service.repository;

import com.gotrip.experience_service.model.ExperienceBooking;
import feign.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ExperienceBookingRepository extends JpaRepository<ExperienceBooking, Long> {

    List<ExperienceBooking> findByTravellerId(Long travellerId);

    // In ExperienceBookingRepository.java
    Page<ExperienceBooking> findByProviderId(Long providerId, Pageable pageable);

    List<ExperienceBooking> findByStatus(String status);

    List<ExperienceBooking> findByProviderIdAndStatus(Long providerId, String status);

    List<ExperienceBooking> findByTravellerIdAndStatus(Long travellerId, String status);

    List<ExperienceBooking> findByExpiresAtBeforeAndStatus(LocalDateTime dateTime, String status);

    List<ExperienceBooking> findByExperience_ExperienceId(Long experienceId);

    @Query(value = "SELECT b FROM ExperienceBooking b JOIN FETCH b.experience WHERE b.providerId = :providerId",
            countQuery = "SELECT count(b) FROM ExperienceBooking b WHERE b.providerId = :providerId")
    Page<ExperienceBooking> findByProviderIdWithExperience(@Param("providerId") Long providerId, Pageable pageable);

    // In ExperienceBookingRepository.java
    @Query(value = "SELECT b FROM ExperienceBooking b JOIN FETCH b.experience WHERE b.travellerId = :travellerId",
            countQuery = "SELECT count(b) FROM ExperienceBooking b WHERE b.travellerId = :travellerId")
    Page<ExperienceBooking> findByTravellerIdWithExperience(@Param("travellerId") Long travellerId, Pageable pageable);
}
