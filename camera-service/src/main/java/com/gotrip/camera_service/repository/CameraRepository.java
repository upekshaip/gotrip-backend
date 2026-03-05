package com.gotrip.camera_service.repository;

import com.gotrip.common_library.dto.camera_service.enums.CameraStatus;
import com.gotrip.camera_service.model.Camera;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CameraRepository extends JpaRepository<Camera, Long> {

    // Find all cameras that are NOT removed (for general browsing)
    List<Camera> findByStatusNot(CameraStatus status);

    // Inside CameraRepository.java
    Page<Camera> findByStatus(CameraStatus status, Pageable pageable);

    Page<Camera> findAll(Pageable pageable);

    // Find a specific camera by ID but only if it's not removed
    Optional<Camera> findByCameraIdAndStatusNot(Long cameraId, CameraStatus status);

    // Find cameras for a specific city that are ACTIVE
    List<Camera> findByCityIgnoreCaseAndStatus(String city, CameraStatus status);

    // Find all cameras owned by a specific provider
    List<Camera> findByProviderIdAndStatusNot(Long providerId, CameraStatus status);

    // Find featured cameras that are active
    List<Camera> findByIsFeaturedTrueAndStatus(CameraStatus status);

    // Spring Data JPA needs this specific Pageable to handle the SQL "LIMIT" and "OFFSET"
    Page<Camera> findByProviderIdAndStatusAndStatusNot(
            Long providerId,
            CameraStatus status,
            CameraStatus excludeStatus,
            Pageable pageable
    );

    Page<Camera> findByProviderIdAndStatusNot(
            Long providerId,
            CameraStatus excludeStatus,
            Pageable pageable
    );

    // In CameraRepository.java

}