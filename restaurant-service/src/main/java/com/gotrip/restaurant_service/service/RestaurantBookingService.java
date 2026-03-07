package com.gotrip.restaurant_service.service;

import com.gotrip.common_library.dto.hotel_service.enums.BookingStatus;
import com.gotrip.common_library.dto.hotel_service.enums.PriceUnit;
import com.gotrip.common_library.dto.restaurant_service.*;
import com.gotrip.common_library.dto.user.TravellerContactInfo;
import com.gotrip.restaurant_service.client.UserServiceClient;
import com.gotrip.restaurant_service.model.Restaurant;
import com.gotrip.restaurant_service.model.RestaurantBooking;
import com.gotrip.restaurant_service.repository.RestaurantBookingRepository;
import com.gotrip.restaurant_service.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RestaurantBookingService {

    private final RestaurantBookingRepository bookingRepository;
    private final RestaurantRepository restaurantRepository;
    private final UserServiceClient userServiceClient;

    @Transactional
    public RestaurantBooking createBookingRequest(RestaurantBookingRequest req, Authentication auth) {
        Map<String, Object> principal = (Map<String, Object>) auth.getPrincipal();

        Map<String, Object> travellerProfile = (Map<String, Object>) principal.get("travellerProfile");
        if (travellerProfile == null) {
            throw new RuntimeException("User does not have a Traveller profile.");
        }
        Long travellerId = ((Number) travellerProfile.get("travellerId")).longValue();

        Restaurant restaurant = restaurantRepository.findById(req.restaurantId())
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        RestaurantBooking booking = new RestaurantBooking();
        booking.setTravellerId(travellerId);
        booking.setRestaurantId(req.restaurantId());
        booking.setProviderId(restaurant.getProviderId());

        booking.setBasePrice(restaurant.getPrice());
        booking.setPriceUnit(restaurant.getPriceUnit());

        booking.setBookingReference("GT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        booking.setStatus(BookingStatus.PENDING);

        booking.setReservationDate(req.reservationDate());
        booking.setReservationTime(req.reservationTime());
        booking.setPersonCount(req.personCount());
        booking.setRequestMessage(req.requestMessage());

        // Calculate total: price per person * number of persons
        BigDecimal total = restaurant.getPrice()
                .multiply(BigDecimal.valueOf(req.personCount()));

        BigDecimal discount = restaurant.getDiscount() != null ? restaurant.getDiscount() : BigDecimal.ZERO;

        booking.setTotalAmount(total);
        booking.setDiscountAmount(discount);
        BigDecimal finalPrice = total.subtract(discount);
        if (finalPrice.compareTo(BigDecimal.ZERO) < 0) {
            finalPrice = BigDecimal.ZERO;
        }

        booking.setFinalAmount(finalPrice);
        return bookingRepository.save(booking);
    }

    // Perspective: My Trips (Traveller)
    public Page<RestaurantBookingResponse> getTravellerBookings(BookingStatus status, int page, int limit, Authentication auth) {
        Map<String, Object> principal = (Map<String, Object>) auth.getPrincipal();
        Map<String, Object> profile = (Map<String, Object>) principal.get("travellerProfile");

        if (profile == null) throw new RuntimeException("Traveller profile not found");
        Long travellerId = ((Number) profile.get("travellerId")).longValue();

        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by("createdAt").descending());

        Page<RestaurantBooking> bookings = (status != null)
                ? bookingRepository.findByTravellerIdAndStatus(travellerId, status, pageable)
                : bookingRepository.findByTravellerId(travellerId, pageable);

        return bookings.map(b -> {
            Restaurant restaurant = restaurantRepository.findById(b.getRestaurantId()).orElse(null);
            return new RestaurantBookingResponse(
                    mapToBookingDTO(b),
                    mapToRestaurantSummary(restaurant),
                    null // Contact info not needed for traveller view
            );
        });
    }

    // Perspective: Booking Requests (Provider)
    public Page<RestaurantBookingResponse> getProviderBookings(BookingStatus status, int page, int limit, Authentication auth) {
        Map<String, Object> principal = (Map<String, Object>) auth.getPrincipal();
        Map<String, Object> profile = (Map<String, Object>) principal.get("serviceProviderProfile");

        if (profile == null) throw new RuntimeException("Service Provider profile not found");
        Long providerId = ((Number) profile.get("providerId")).longValue();

        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by("createdAt").descending());

        Page<RestaurantBooking> bookings = (status != null)
                ? bookingRepository.findByProviderIdAndStatus(providerId, status, pageable)
                : bookingRepository.findByProviderId(providerId, pageable);

        return bookings.map(b -> {
            Restaurant restaurant = restaurantRepository.findById(b.getRestaurantId()).orElse(null);
            TravellerContactInfo contact = userServiceClient.getTravellerContact(b.getTravellerId());

            return new RestaurantBookingResponse(
                    mapToBookingDTO(b),
                    mapToRestaurantSummary(restaurant),
                    contact
            );
        });
    }

    private RestaurantSummaryResponse mapToRestaurantSummary(Restaurant r) {
        if (r == null) return null;
        return new RestaurantSummaryResponse(
                r.getRestaurantId(),
                r.getName(),
                r.getDescription(),
                r.getAddress(),
                r.getCity(),
                r.getCuisineType(),
                r.getImageUrl(),
                r.getPriceUnit(),
                r.getPrice(),
                r.getDiscount(),
                r.isFeatured(),
                r.getStatus(),
                r.getProviderId(),
                r.getUpdatedAt()
        );
    }

    private RestaurantBookingDTO mapToBookingDTO(RestaurantBooking b) {
        return new RestaurantBookingDTO(
                b.getBookingId(), b.getBookingReference(), b.getStatus(),
                b.getPersonCount(), b.getReservationDate(),
                b.getReservationTime(),
                b.getFinalAmount(), b.getRequestMessage(), b.getProviderMessage(),
                b.getCreatedAt(), b.getTotalAmount(), b.getDiscountAmount(), b.getBasePrice()
        );
    }


    @Transactional
    public RestaurantBooking respondToBooking(Long bookingId, RestaurantRespondDTO respondDTO, Authentication auth) {
        RestaurantBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        Map<String, Object> principal = (Map<String, Object>) auth.getPrincipal();
        Map<String, Object> profile = (Map<String, Object>) principal.get("serviceProviderProfile");

        if (profile == null) throw new RuntimeException("Service Provider profile not found");
        Long currentProviderId = ((Number) profile.get("providerId")).longValue();

        if (!booking.getProviderId().equals(currentProviderId)) {
            throw new RuntimeException("Unauthorized: This booking does not belong to you.");
        }

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new RuntimeException("Can only respond to PENDING requests.");
        }

        booking.setStatus(respondDTO.status());
        booking.setProviderMessage(respondDTO.message());
        return bookingRepository.save(booking);
    }

    @Transactional
    public RestaurantBooking cancelBooking(Long bookingId, Authentication auth) {
        RestaurantBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        Map<String, Object> principal = (Map<String, Object>) auth.getPrincipal();
        Map<String, Object> profile = (Map<String, Object>) principal.get("travellerProfile");

        if (profile == null) throw new RuntimeException("Traveller profile not found");
        Long currentTravellerId = ((Number) profile.get("travellerId")).longValue();

        if (!booking.getTravellerId().equals(currentTravellerId)) {
            throw new RuntimeException("Unauthorized: You can only cancel your own bookings.");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        return bookingRepository.save(booking);
    }
}
