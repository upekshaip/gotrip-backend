package com.gotrip.restaurant_service.service;

import com.gotrip.common_library.dto.restaurant_service.RestaurantCreateRequest;
import com.gotrip.common_library.dto.restaurant_service.RestaurantSummaryResponse;
import com.gotrip.common_library.dto.restaurant_service.UpdateStatusRequest;
import com.gotrip.common_library.dto.restaurant_service.enums.RestaurantStatus;
import com.gotrip.common_library.dto.user.TravellerContactInfo;
import com.gotrip.restaurant_service.client.UserServiceClient;
import com.gotrip.restaurant_service.model.Restaurant;
import com.gotrip.restaurant_service.repository.RestaurantRepository;
import com.gotrip.restaurant_service.repository.RestaurantReviewRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final RestaurantReviewRepository reviewRepository;
    private final UserServiceClient userServiceClient;

    @Transactional
    public Restaurant createRestaurant(RestaurantCreateRequest request, Authentication auth) {
        Long providerId = extractProviderId(auth);
        Restaurant restaurant = new Restaurant();
        mapDtoToEntity(request, restaurant);
        restaurant.setProviderId(providerId);
        restaurant.setStatus(RestaurantStatus.PENDING);
        return restaurantRepository.save(restaurant);
    }

    public Page<RestaurantSummaryResponse> getAllActive(int page, int limit) {
        // 0-indexed page for Spring Data, sorting by featured first, then newest
        Pageable pageable = PageRequest.of(page - 1, limit,
                Sort.by(Sort.Direction.DESC, "isFeatured")
                        .and(Sort.by(Sort.Direction.DESC, "updatedAt")));

        Page<Restaurant> restaurantPage = restaurantRepository.findByStatus(RestaurantStatus.ACTIVE, pageable);

        // Map the Entity to our Response DTO
        return restaurantPage.map(h -> new RestaurantSummaryResponse(
                h.getRestaurantId(),
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

    public Page<Restaurant> getMyAll(RestaurantStatus status, int page, int limit, Authentication auth) {
        Long providerId = extractProviderId(auth);

        // PageRequest.of returns the correct org.springframework.data.domain.Pageable
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by("updatedAt").descending());

        if (status != null) {
            return restaurantRepository.findByProviderIdAndStatusAndStatusNot(
                    providerId, status, RestaurantStatus.REMOVED, pageable);
        }

        return restaurantRepository.findByProviderIdAndStatusNot(
                providerId, RestaurantStatus.REMOVED, pageable);
    }



    public Restaurant getById(Long id) {
        return restaurantRepository.findById(id)
                .filter(h -> h.getStatus() != RestaurantStatus.REMOVED)
                .orElseThrow(() -> new RuntimeException("Restaurant not found or has been removed."));
    }

    public Map<String, Object> getByIdForTraveller(Long id) {
        Restaurant restaurant =  restaurantRepository.findById(id)
                .filter(h -> h.getStatus() == RestaurantStatus.ACTIVE)
                .orElseThrow(() -> new RuntimeException("Restaurant not found or has been removed."));
        TravellerContactInfo contact = userServiceClient.getProviderContact(restaurant.getProviderId());
        return Map.of("restaurant", restaurant, "provider", contact.name());
    }

    public Restaurant getByIdForProvider(Long id,Authentication auth) {
        Restaurant restaurant = getById(id);
        validateOwnership(restaurant, auth);

        return restaurantRepository.findById(id)
                .filter(h -> h.getStatus() != RestaurantStatus.REMOVED)
                .orElseThrow(() -> new RuntimeException("Restaurant not found or has been removed."));
    }


    @Transactional
    public Restaurant update(Long id, RestaurantCreateRequest request, Authentication auth) {
        Restaurant restaurant = getById(id);
        validateOwnership(restaurant, auth);
        mapDtoToEntity(request, restaurant);
        return restaurantRepository.save(restaurant);
    }

    @Transactional
    public Restaurant updateByAdmin(Long id, RestaurantCreateRequest request, Authentication auth) {
        extractAdminId(auth);

        Restaurant restaurant = getById(id);
        mapDtoToEntity(request, restaurant);
        return restaurantRepository.save(restaurant);
    }

    public Restaurant updateStatusByAdmin(Long id, UpdateStatusRequest request, Authentication auth) {
        extractAdminId(auth);

        Restaurant restaurant = getById(id);
        restaurant.setStatus(request.status());
        return restaurantRepository.save(restaurant);
    }

    @Transactional
    public Restaurant delete(Long id, Authentication auth) {
        Restaurant restaurant = getById(id);
        validateOwnership(restaurant, auth);

        // Soft Delete the Restaurant
        restaurant.setStatus(RestaurantStatus.REMOVED);
        restaurantRepository.save(restaurant);

        // Hard Delete all associated reviews
        reviewRepository.deleteAllByRestaurant_RestaurantId(id);
        return  restaurant;
    }

    public Page<Restaurant> getAllRestaurantsByAdmin(Authentication authentication, int page, int limit) {
        extractAdminId(authentication);
        // 0-indexed PageRequest, sorting by newest updated
        Pageable pageable = PageRequest.of(page - 1, limit,
                Sort.by(Sort.Direction.DESC, "updatedAt"));

        Page<Restaurant> restaurantPage = restaurantRepository.findAll(pageable);

        return restaurantPage;
    }

    public Page<Restaurant> getPendingRestaurantsByAdmin(Authentication authentication, int page, int limit) {
        extractAdminId(authentication);

        Pageable pageable = PageRequest.of(page - 1, limit,
                Sort.by(Sort.Direction.DESC, "createdAt"));

        // Assuming RestaurantStatus is an Enum or String; adjust based on your model
        Page<Restaurant> restaurantPage = restaurantRepository.findByStatus(RestaurantStatus.PENDING, pageable);
        return restaurantPage;
    }

    // Helper mapper (reusing your existing logic)
    private RestaurantSummaryResponse mapToSummaryResponse(Restaurant h) {
        return new RestaurantSummaryResponse(
                h.getRestaurantId(),
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

    private void validateOwnership(Restaurant restaurant, Authentication auth) {
        if (!restaurant.getProviderId().equals(extractProviderId(auth))) {
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



    private void mapDtoToEntity(RestaurantCreateRequest req, Restaurant restaurant) {
        restaurant.setName(req.name());
        restaurant.setDescription(req.description());
        restaurant.setAddress(req.address());
        restaurant.setCity(req.city());
        restaurant.setPriceUnit(req.priceUnit());
        restaurant.setPrice(req.price());
        restaurant.setLatitude(req.latitude());
        restaurant.setLongitude(req.longitude());
        restaurant.setImageUrl(req.imageUrl());
        restaurant.setFeatured(req.featured());
        restaurant.setDiscount(req.discount());
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