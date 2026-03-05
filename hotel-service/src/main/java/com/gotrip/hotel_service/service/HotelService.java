package com.gotrip.hotel_service.service;

import com.gotrip.common_library.dto.hotel_service.HotelCreateRequest;
import com.gotrip.common_library.dto.hotel_service.HotelSummaryResponse;
import com.gotrip.common_library.dto.hotel_service.enums.HotelStatus;
import com.gotrip.common_library.dto.user.TravellerContactInfo;
import com.gotrip.hotel_service.client.UserServiceClient;
import com.gotrip.hotel_service.model.Hotel;
import com.gotrip.hotel_service.repository.HotelRepository;
import com.gotrip.hotel_service.repository.HotelReviewRepository;
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
public class HotelService {

    private final HotelRepository hotelRepository;
    private final HotelReviewRepository reviewRepository;
    private final UserServiceClient userServiceClient;

    @Transactional
    public Hotel createHotel(HotelCreateRequest request, Authentication auth) {
        Long providerId = extractProviderId(auth);
        Hotel hotel = new Hotel();
        mapDtoToEntity(request, hotel);
        hotel.setProviderId(providerId);
        hotel.setStatus(HotelStatus.ACTIVE); // Auto-active on creation
        return hotelRepository.save(hotel);
    }

    public Page<HotelSummaryResponse> getAllActive(int page, int limit) {
        // 0-indexed page for Spring Data, sorting by featured first, then newest
        Pageable pageable = PageRequest.of(page - 1, limit,
                Sort.by(Sort.Direction.DESC, "isFeatured")
                        .and(Sort.by(Sort.Direction.DESC, "updatedAt")));

        Page<Hotel> hotelPage = hotelRepository.findByStatus(HotelStatus.ACTIVE, pageable);

        // Map the Entity to our Response DTO
        return hotelPage.map(h -> new HotelSummaryResponse(
                h.getHotelId(),
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

    public Page<Hotel> getMyAll(HotelStatus status, int page, int limit, Authentication auth) {
        Long providerId = extractProviderId(auth);

        // PageRequest.of returns the correct org.springframework.data.domain.Pageable
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by("updatedAt").descending());

        if (status != null) {
            return hotelRepository.findByProviderIdAndStatusAndStatusNot(
                    providerId, status, HotelStatus.REMOVED, pageable);
        }

        return hotelRepository.findByProviderIdAndStatusNot(
                providerId, HotelStatus.REMOVED, pageable);
    }



    public Hotel getById(Long id) {
        return hotelRepository.findById(id)
                .filter(h -> h.getStatus() != HotelStatus.REMOVED)
                .orElseThrow(() -> new RuntimeException("Hotel not found or has been removed."));
    }

    public Map<String, Object> getByIdForTraveller(Long id) {
        Hotel hotel =  hotelRepository.findById(id)
                .filter(h -> h.getStatus() == HotelStatus.ACTIVE)
                .orElseThrow(() -> new RuntimeException("Hotel not found or has been removed."));
        TravellerContactInfo contact = userServiceClient.getProviderContact(hotel.getProviderId());
        return Map.of("hotel", hotel, "provider", contact.name());
    }

    public Hotel getByIdForProvider(Long id,Authentication auth) {
        Hotel hotel = getById(id);
        validateOwnership(hotel, auth);

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
    public Hotel delete(Long id, Authentication auth) {
        Hotel hotel = getById(id);
        validateOwnership(hotel, auth);

        // Soft Delete the Hotel
        hotel.setStatus(HotelStatus.REMOVED);
        hotelRepository.save(hotel);

        // Hard Delete all associated reviews
        reviewRepository.deleteAllByHotel_HotelId(id);
        return  hotel;
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
        hotel.setDiscount(req.discount());
    }
}