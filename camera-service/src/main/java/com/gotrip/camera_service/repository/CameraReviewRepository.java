package com.gotrip.camera_service.repository;

import com.gotrip.camera_service.model.CameraReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Repository
public interface CameraReviewRepository extends JpaRepository<CameraReview, Long> {

    // Get all reviews for a specific camera (ordered by newest first)
    List<CameraReview> findByCamera_CameraIdOrderByCreatedAtDesc(Long cameraId);

    // Check if a traveler has already reviewed a specific booking
    boolean existsByBookingId(Long bookingId);

    // Calculate the average rating for a camera to update the Camera listing
    @Query("SELECT AVG(r.rating) FROM CameraReview r WHERE r.camera.cameraId = :cameraId")
    Double getAverageRatingForCamera(@Param("cameraId") Long cameraId);

    /**
     * FIX: Added for the Camera Service's delete logic.
     * Use @Modifying because this is a DML (Delete) operation.
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM CameraReview r WHERE r.camera.cameraId = :cameraId")
    void deleteAllByCamera_CameraId(@Param("cameraId") Long cameraId);
}