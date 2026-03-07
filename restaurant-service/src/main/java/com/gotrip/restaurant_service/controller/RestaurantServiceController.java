package com.gotrip.restaurant_service.controller;


import com.gotrip.common_library.dto.restaurant_service.RestaurantCreateRequest;
import com.gotrip.common_library.dto.restaurant_service.RestaurantSummaryResponse;
import com.gotrip.common_library.dto.restaurant_service.UpdateStatusRequest;
import com.gotrip.common_library.dto.restaurant_service.enums.RestaurantStatus;
import com.gotrip.restaurant_service.model.Restaurant;
import com.gotrip.restaurant_service.service.RestaurantService;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

@RestController
@RequestMapping("/restaurant-service")
public class RestaurantServiceController {

    private final ObjectMapper objectMapper;
    private final RestaurantService restaurantService;

    public RestaurantServiceController(ObjectMapper objectMapper, RestaurantService restaurantService) {
        this.objectMapper = objectMapper;
        this.restaurantService = restaurantService;
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody RestaurantCreateRequest req, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(restaurantService.createRestaurant(req, auth));
    }

    @GetMapping
    public ResponseEntity<Page<RestaurantSummaryResponse>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int limit
    ) {
        return ResponseEntity.ok(restaurantService.getAllActive(page, limit));
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyRestaurants(
            @RequestParam(required = false) RestaurantStatus status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            Authentication auth) {
        return ResponseEntity.ok(restaurantService.getMyAll(status, page, limit, auth));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        return ResponseEntity.ok(restaurantService.getById(id));
    }

    @GetMapping("traveller/{id}")
    public ResponseEntity<?> getByTraveller(@PathVariable Long id) {
        return ResponseEntity.ok(restaurantService.getByIdForTraveller(id));
    }

    @GetMapping("/provider/{id}")
    public ResponseEntity<?> getByProvider(@PathVariable Long id, @Valid @RequestBody RestaurantCreateRequest req, Authentication auth) {
        return ResponseEntity.ok(restaurantService.getByIdForProvider(id, auth));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody RestaurantCreateRequest req, Authentication auth) {
        return ResponseEntity.ok(restaurantService.update(id, req, auth));
    }

    @PutMapping("admin/{id}")
    public ResponseEntity<?> updateByAdmin(@PathVariable Long id, @Valid @RequestBody RestaurantCreateRequest req, Authentication auth) {
        return ResponseEntity.ok(restaurantService.updateByAdmin(id, req, auth));
    }

    @PutMapping("admin/status/{id}")
    public ResponseEntity<?> updateStatusByAdmin(@PathVariable Long id, @Valid @RequestBody UpdateStatusRequest req, Authentication auth) {
        return ResponseEntity.ok(restaurantService.updateStatusByAdmin(id, req, auth));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(restaurantService.delete(id, auth));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMyDetails(Authentication authentication) {
        System.out.println("request coming...");
        return ResponseEntity.ok(authentication.getPrincipal());
    }

    @GetMapping("admin/all")
    public ResponseEntity<Page<Restaurant>> getAllRestaurantsByAdmin(
            Authentication authentication,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(restaurantService.getAllRestaurantsByAdmin(authentication, page, limit));
    }

    @GetMapping("admin/pending")
    public ResponseEntity<Page<Restaurant>> getPendingRestaurantsByAdmin(
            Authentication authentication,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(restaurantService.getPendingRestaurantsByAdmin(authentication, page, limit));
    }

}
