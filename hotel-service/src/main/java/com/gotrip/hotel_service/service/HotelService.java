package com.gotrip.hotel_service.service;

import com.gotrip.common_library.dto.hotel_service.HotelCreateRequest;
import com.gotrip.common_library.dto.hotel_service.enums.HotelStatus;
import com.gotrip.hotel_service.model.Hotel;
import com.gotrip.hotel_service.repository.HotelRepository;
import com.gotrip.hotel_service.repository.HotelReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class HotelService {

    private final HotelRepository hotelRepository;
    private final HotelReviewRepository reviewRepository;

    @Transactional
    public Hotel createHotel(HotelCreateRequest request, Authentication auth) {
        Long providerId = extractProviderId(auth);
        Hotel hotel = new Hotel();
        mapDtoToEntity(request, hotel);
        hotel.setProviderId(providerId);
        hotel.setStatus(HotelStatus.PENDING); // Auto-active on creation
        return hotelRepository.save(hotel);
    }

    public List<Hotel> getAllActive() {
        return hotelRepository.findAll().stream()
                .filter(h -> h.getStatus() == HotelStatus.ACTIVE)
                .toList();
    }

    public Hotel getById(Long id) {
        return hotelRepository.findById(id)
                .filter(h -> h.getStatus() != HotelStatus.REMOVED)
                .orElseThrow(() -> new RuntimeException("Hotel not found or has been removed."));
    }

    @Transactional
    public Hotel update(Long id, HotelCreateRequest request, Authentication auth) {
        Hotel hotel = getById(id);
        validateOwnership(hotel, auth);
        mapDtoToEntity(request, hotel);
        return hotelRepository.save(hotel);
    }

    @Transactional
    public void delete(Long id, Authentication auth) {
        Hotel hotel = getById(id);
        validateOwnership(hotel, auth);

        // Soft Delete the Hotel
        hotel.setStatus(HotelStatus.REMOVED);
        hotelRepository.save(hotel);

        // Hard Delete all associated reviews
        reviewRepository.deleteAllByHotel_HotelId(id);
    }

    private void validateOwnership(Hotel hotel, Authentication auth) {
        if (!hotel.getProviderId().equals(extractProviderId(auth))) {
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

    private void mapDtoToEntity(HotelCreateRequest req, Hotel hotel) {
        hotel.setName(req.name());
        hotel.setDescription(req.description());
        hotel.setAddress(req.address());
        hotel.setCity(req.city());
        hotel.setPriceUnit(req.priceUnit());
        hotel.setPrice(req.price());
        hotel.setLatitude(req.latitude());
        hotel.setLongitude(req.longitude());
        hotel.setImageUrl(req.imageUrl());
        hotel.setFeatured(req.featured());
    }
}