package com.gotrip.camera_service.service;

import com.gotrip.common_library.dto.camera_service.CameraCreateRequest;
import com.gotrip.common_library.dto.camera_service.CameraSummaryResponse;
import com.gotrip.common_library.dto.camera_service.UpdateStatusRequest;
import com.gotrip.common_library.dto.camera_service.enums.CameraStatus;
import com.gotrip.common_library.dto.user.TravellerContactInfo;
import com.gotrip.camera_service.client.UserServiceClient;
import com.gotrip.camera_service.model.Camera;
import com.gotrip.camera_service.repository.CameraRepository;
import com.gotrip.camera_service.repository.CameraReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CameraService {

    private final CameraRepository cameraRepository;
    private final CameraReviewRepository reviewRepository;
    private final UserServiceClient userServiceClient;

    @Transactional
    public Camera createCamera(CameraCreateRequest request, Authentication auth) {
        Long providerId = extractProviderId(auth);
        Camera camera = new Camera();
        mapDtoToEntity(request, camera);
        camera.setProviderId(providerId);
        camera.setStatus(CameraStatus.PENDING);
        return cameraRepository.save(camera);
    }

    public Page<CameraSummaryResponse> getAllActive(int page, int limit) {
        // 0-indexed page for Spring Data, sorting by featured first, then newest
        Pageable pageable = PageRequest.of(page - 1, limit,
                Sort.by(Sort.Direction.DESC, "isFeatured")
                        .and(Sort.by(Sort.Direction.DESC, "updatedAt")));

        Page<Camera> cameraPage = cameraRepository.findByStatus(CameraStatus.ACTIVE, pageable);

        // Map the Entity to our Response DTO
        return cameraPage.map(h -> new CameraSummaryResponse(
                h.getCameraId(),
                h.getName(),
                h.getDescription(),
                h.getAddress(),
                h.getCity(),
                h.getImageUrl(),
                h.getPriceUnit(),
                h.getPrice(),
                h.getDiscount(),
                h.isFeatured(),
                h.getStatus(),
                h.getProviderId(),
                h.getUpdatedAt()
        ));
    }

    public Page<Camera> getMyAll(CameraStatus status, int page, int limit, Authentication auth) {
        Long providerId = extractProviderId(auth);

        // PageRequest.of returns the correct org.springframework.data.domain.Pageable
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by("updatedAt").descending());

        if (status != null) {
            return cameraRepository.findByProviderIdAndStatusAndStatusNot(
                    providerId, status, CameraStatus.REMOVED, pageable);
        }

        return cameraRepository.findByProviderIdAndStatusNot(
                providerId, CameraStatus.REMOVED, pageable);
    }



    public Camera getById(Long id) {
        return cameraRepository.findById(id)
                .filter(h -> h.getStatus() != CameraStatus.REMOVED)
                .orElseThrow(() -> new RuntimeException("Camera not found or has been removed."));
    }

    public Map<String, Object> getByIdForTraveller(Long id) {
        Camera camera =  cameraRepository.findById(id)
                .filter(h -> h.getStatus() == CameraStatus.ACTIVE)
                .orElseThrow(() -> new RuntimeException("Camera not found or has been removed."));
        TravellerContactInfo contact = userServiceClient.getProviderContact(camera.getProviderId());
        return Map.of("camera", camera, "provider", contact.name());
    }

    public Camera getByIdForProvider(Long id,Authentication auth) {
        Camera camera = getById(id);
        validateOwnership(camera, auth);

        return cameraRepository.findById(id)
                .filter(h -> h.getStatus() != CameraStatus.REMOVED)
                .orElseThrow(() -> new RuntimeException("Camera not found or has been removed."));
    }


    @Transactional
    public Camera update(Long id, CameraCreateRequest request, Authentication auth) {
        Camera camera = getById(id);
        validateOwnership(camera, auth);
        mapDtoToEntity(request, camera);
        return cameraRepository.save(camera);
    }

    @Transactional
    public Camera updateByAdmin(Long id, CameraCreateRequest request, Authentication auth) {
        extractAdminId(auth);

        Camera camera = getById(id);
        mapDtoToEntity(request, camera);
        return cameraRepository.save(camera);
    }

    public Camera updateStatusByAdmin(Long id, UpdateStatusRequest request, Authentication auth) {
        extractAdminId(auth);

        Camera camera = getById(id);
        camera.setStatus(request.status());
        return cameraRepository.save(camera);
    }

    @Transactional
    public Camera delete(Long id, Authentication auth) {
        Camera camera = getById(id);
        validateOwnership(camera, auth);

        // Soft Delete the Camera
        camera.setStatus(CameraStatus.REMOVED);
        cameraRepository.save(camera);

        // Hard Delete all associated reviews
        reviewRepository.deleteAllByCamera_CameraId(id);
        return  camera;
    }

    public Page<Camera> getAllCamerasByAdmin(Authentication authentication, int page, int limit) {
        extractAdminId(authentication);
        // 0-indexed PageRequest, sorting by newest updated
        Pageable pageable = PageRequest.of(page - 1, limit,
                Sort.by(Sort.Direction.DESC, "updatedAt"));

        Page<Camera> cameraPage = cameraRepository.findAll(pageable);

        return cameraPage;
    }

    public Page<Camera> getPendingCamerasByAdmin(Authentication authentication, int page, int limit) {
        extractAdminId(authentication);

        Pageable pageable = PageRequest.of(page - 1, limit,
                Sort.by(Sort.Direction.DESC, "createdAt"));

        // Assuming CameraStatus is an Enum or String; adjust based on your model
        Page<Camera> cameraPage = cameraRepository.findByStatus(CameraStatus.PENDING, pageable);
        return cameraPage;
    }

    // Helper mapper (reusing your existing logic)
    private CameraSummaryResponse mapToSummaryResponse(Camera h) {
        return new CameraSummaryResponse(
                h.getCameraId(),
                h.getName(),
                h.getDescription(),
                h.getAddress(),
                h.getCity(),
                h.getImageUrl(),
                h.getPriceUnit(),
                h.getPrice(),
                h.getDiscount(),
                h.isFeatured(),
                h.getStatus(),
                h.getProviderId(),
                h.getUpdatedAt()
        );
    }

    private void validateOwnership(Camera camera, Authentication auth) {
        if (!camera.getProviderId().equals(extractProviderId(auth))) {
            throw new RuntimeException("Access Denied: Ownership verification failed.");
        }
    }

    private Long extractProviderId(Authentication auth) {
        Map<String, Object> principal = (Map<String, Object>) auth.getPrincipal();
        if (!(boolean) principal.getOrDefault("serviceProvider", false)) {
            throw new RuntimeException("Unauthorized: Only Service Providers can manage listings.");
        }
        Map<String, Object> profile = (Map<String, Object>) principal.get("serviceProviderProfile");
        return ((Number) profile.get("providerId")).longValue();
    }



    private void mapDtoToEntity(CameraCreateRequest req, Camera camera) {
        camera.setName(req.name());
        camera.setDescription(req.description());
        camera.setAddress(req.address());
        camera.setCity(req.city());
        camera.setPriceUnit(req.priceUnit());
        camera.setPrice(req.price());
        camera.setLatitude(req.latitude());
        camera.setLongitude(req.longitude());
        camera.setImageUrl(req.imageUrl());
        camera.setFeatured(req.featured());
        camera.setDiscount(req.discount());
    }

    private Long extractAdminId(Authentication auth) {
        Map<String, Object> principal = (Map<String, Object>) auth.getPrincipal();
        if (!(boolean) principal.getOrDefault("admin", false)) {
            throw new RuntimeException("Unauthorized: Only admins can manage listings.");
        }
        Map<String, Object> profile = (Map<String, Object>) principal.get("adminProfile");
        return ((Number) profile.get("adminId")).longValue();
    }
}