package com.gotrip.camera_service.controller;


import com.gotrip.common_library.dto.camera_service.CameraCreateRequest;
import com.gotrip.common_library.dto.camera_service.CameraSummaryResponse;
import com.gotrip.common_library.dto.camera_service.UpdateStatusRequest;
import com.gotrip.common_library.dto.camera_service.enums.CameraStatus;
import com.gotrip.camera_service.model.Camera;
import com.gotrip.camera_service.service.CameraService;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/camera-service")
public class CameraServiceController {

    private final ObjectMapper objectMapper;
    private final CameraService cameraService;

    public CameraServiceController(ObjectMapper objectMapper, CameraService cameraService) {
        this.objectMapper = objectMapper;
        this.cameraService = cameraService;
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody CameraCreateRequest req, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cameraService.createCamera(req, auth));
    }

    @GetMapping
    public ResponseEntity<Page<CameraSummaryResponse>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int limit
    ) {
        return ResponseEntity.ok(cameraService.getAllActive(page, limit));
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyCameras(
            @RequestParam(required = false) CameraStatus status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            Authentication auth) {
        return ResponseEntity.ok(cameraService.getMyAll(status, page, limit, auth));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        return ResponseEntity.ok(cameraService.getById(id));
    }

    @GetMapping("traveller/{id}")
    public ResponseEntity<?> getByTraveller(@PathVariable Long id) {
        return ResponseEntity.ok(cameraService.getByIdForTraveller(id));
    }


    @GetMapping("/provider/{id}")
    public ResponseEntity<?> getByProvider(@PathVariable Long id, @Valid @RequestBody CameraCreateRequest req, Authentication auth) {
        return ResponseEntity.ok(cameraService.getByIdForProvider(id, auth));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody CameraCreateRequest req, Authentication auth) {
        return ResponseEntity.ok(cameraService.update(id, req, auth));
    }

    @PutMapping("admin/{id}")
    public ResponseEntity<?> updateByAdmin(@PathVariable Long id, @Valid @RequestBody CameraCreateRequest req, Authentication auth) {
        return ResponseEntity.ok(cameraService.updateByAdmin(id, req, auth));
    }

    @PutMapping("admin/status/{id}")
    public ResponseEntity<?> updateStatusByAdmin(@PathVariable Long id, @Valid @RequestBody UpdateStatusRequest req, Authentication auth) {
        return ResponseEntity.ok(cameraService.updateStatusByAdmin(id, req, auth));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(cameraService.delete(id, auth));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMyDetails(Authentication authentication) {
        System.out.println("request coming...");
        return ResponseEntity.ok(authentication.getPrincipal());

    }

    @GetMapping("admin/all")
    public ResponseEntity<Page<Camera>> getAllCamerasByAdmin(
            Authentication authentication,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(cameraService.getAllCamerasByAdmin(authentication, page, limit));
    }

    @GetMapping("admin/pending")
    public ResponseEntity<Page<Camera>> getPendingCamerasByAdmin(
            Authentication authentication,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(cameraService.getPendingCamerasByAdmin(authentication, page, limit));
    }


}
