package com.gotrip.hotel_service.service;

import com.gotrip.common_library.dto.hotel_service.HotelBookingDTO;
import com.gotrip.common_library.dto.hotel_service.HotelBookingRequest;
import com.gotrip.common_library.dto.hotel_service.HotelBookingResponse;
import com.gotrip.common_library.dto.hotel_service.HotelSummaryResponse;
import com.gotrip.common_library.dto.hotel_service.enums.BookingStatus;
import com.gotrip.common_library.dto.hotel_service.enums.PriceUnit;
import com.gotrip.common_library.dto.user.TravellerContactInfo;
import com.gotrip.hotel_service.client.UserServiceClient;
import com.gotrip.hotel_service.model.Hotel;
import com.gotrip.hotel_service.model.HotelBooking;
import com.gotrip.hotel_service.repository.HotelBookingRepository;
import com.gotrip.hotel_service.repository.HotelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HotelBookingService {

    private final HotelBookingRepository bookingRepository;
    private final HotelRepository hotelRepository;
    private final UserServiceClient userServiceClient;

    @Transactional
    public HotelBooking createBookingRequest(HotelBookingRequest req, Authentication auth) {
        Map<String, Object> principal = (Map<String, Object>) auth.getPrincipal();

        Map<String, Object> travellerProfile = (Map<String, Object>) principal.get("travellerProfile");
        if (travellerProfile == null) {
            throw new RuntimeException("User does not have a Traveller profile.");
        }
        Long travellerId = ((Number) travellerProfile.get("travellerId")).longValue();

        Hotel hotel = hotelRepository.findById(req.hotelId())
                .orElseThrow(() -> new RuntimeException("Hotel not found"));

        HotelBooking booking = new HotelBooking();
        booking.setTravellerId(travellerId);
        booking.setHotelId(req.hotelId());
        booking.setProviderId(hotel.getProviderId());

         booking.setBasePrice(hotel.getPrice());
         booking.setPriceUnit(hotel.getPriceUnit());

        booking.setBookingReference("GT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        booking.setStatus(BookingStatus.PENDING);

        booking.setStartingDate(req.startingDate());
        booking.setStartingTime(req.startingTime());
        booking.setEndingDate(req.endingDate());
        booking.setEndingTime(req.endingTime());
        booking.setPersonCount(req.personCount());
        booking.setRequestMessage(req.requestMessage());

        BigDecimal total = BigDecimal.ZERO;
        BigDecimal discount = hotel.getDiscount() != null ? hotel.getDiscount() : BigDecimal.ZERO;

        if (hotel.getPriceUnit() == PriceUnit.PER_DAY) {
            long days = ChronoUnit.DAYS.between(req.startingDate(), req.endingDate());
            if (days <= 0) days = 1;

            total = hotel.getPrice()
                    .multiply(BigDecimal.valueOf(req.roomCount()))
                    .multiply(BigDecimal.valueOf(days));

        } else if (hotel.getPriceUnit() == PriceUnit.PER_HOUR) {
            LocalDateTime start = LocalDateTime.of(req.startingDate(), req.startingTime());
            LocalDateTime end = LocalDateTime.of(req.endingDate(), req.endingTime());
            long hours = ChronoUnit.HOURS.between(start, end);
            if (hours <= 0) hours = 1;

            total = hotel.getPrice()
                    .multiply(BigDecimal.valueOf(req.roomCount()))
                    .multiply(BigDecimal.valueOf(hours));

        } else if (hotel.getPriceUnit() == PriceUnit.PER_PERSON) {
            long days = ChronoUnit.DAYS.between(req.startingDate(), req.endingDate());
            if (days <= 0) days = 1;

            total = hotel.getPrice()
                    .multiply(BigDecimal.valueOf(req.personCount()))
                    .multiply(BigDecimal.valueOf(days));
        }

        booking.setRoomCount(req.roomCount());
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
    public Page<HotelBookingResponse> getTravellerBookings(BookingStatus status, int page, int limit, Authentication auth) {
        Map<String, Object> principal = (Map<String, Object>) auth.getPrincipal();
        Map<String, Object> profile = (Map<String, Object>) principal.get("travellerProfile");

        if (profile == null) throw new RuntimeException("Traveller profile not found");
        Long travellerId = ((Number) profile.get("travellerId")).longValue();

        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by("createdAt").descending());

        Page<HotelBooking> bookings = (status != null)
                ? bookingRepository.findByTravellerIdAndStatus(travellerId, status, pageable)
                : bookingRepository.findByTravellerId(travellerId, pageable);

        return bookings.map(b -> {
            Hotel hotel = hotelRepository.findById(b.getHotelId()).orElse(null);
            return new HotelBookingResponse(
                    mapToBookingDTO(b),
                    mapToHotelSummary(hotel),
                    null // Contact info not needed for traveller view
            );
        });
    }

    // Perspective: Booking Requests (Provider)
    public Page<HotelBookingResponse> getProviderBookings(BookingStatus status, int page, int limit, Authentication auth) {
        Map<String, Object> principal = (Map<String, Object>) auth.getPrincipal();
        Map<String, Object> profile = (Map<String, Object>) principal.get("serviceProviderProfile");

        if (profile == null) throw new RuntimeException("Service Provider profile not found");
        Long providerId = ((Number) profile.get("providerId")).longValue();

        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by("createdAt").descending());

        Page<HotelBooking> bookings = (status != null)
                ? bookingRepository.findByProviderIdAndStatus(providerId, status, pageable)
                : bookingRepository.findByProviderId(providerId, pageable);

        return bookings.map(b -> {
            Hotel hotel = hotelRepository.findById(b.getHotelId()).orElse(null);
            TravellerContactInfo contact = userServiceClient.getTravellerContact(b.getTravellerId());

            return new HotelBookingResponse(
                    mapToBookingDTO(b),
                    mapToHotelSummary(hotel),
                    contact
            );
        });
    }

    private HotelSummaryResponse mapToHotelSummary(Hotel h) {
        if (h == null) return null;
        return new HotelSummaryResponse(
                h.getHotelId(),
                h.getName(),
                h.getDescription(),
                h.getAddress(),
                h.getCity(),
                h.getImageUrl(),
                h.getPriceUnit(),
                h.getPrice(),
                h.getDiscount(),
                h.isFeatured(), // Fixed: Matches Lombok's generated getter for 'boolean isFeatured'
                h.getStatus(),
                h.getProviderId(),
                h.getUpdatedAt()
        );
    }

    private HotelBookingDTO mapToBookingDTO(HotelBooking b) {
        return new HotelBookingDTO(
                b.getBookingId(), b.getBookingReference(), b.getStatus(),
                b.getPersonCount(), b.getRoomCount(), b.getStartingDate(),
                b.getStartingTime(), b.getEndingDate(), b.getEndingTime(),
                b.getFinalAmount(), b.getRequestMessage(), b.getProviderMessage(),
                b.getCreatedAt(), b.getTotalAmount(), b.getDiscountAmount(), b.getBasePrice()
        );
    }


    @Transactional
    public HotelBooking respondToBooking(Long bookingId, BookingStatus newStatus, String message, Authentication auth) {
        // 1. Fetch the booking
        HotelBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // 2. Extract current Provider ID from Security Context
        Map<String, Object> principal = (Map<String, Object>) auth.getPrincipal();
        Map<String, Object> profile = (Map<String, Object>) principal.get("serviceProviderProfile");

        if (profile == null) throw new RuntimeException("Service Provider profile not found");
        Long currentProviderId = ((Number) profile.get("providerId")).longValue();

        // 3. Security Check: Does this provider own this booking?
        if (!booking.getProviderId().equals(currentProviderId)) {
            throw new RuntimeException("Unauthorized: This booking does not belong to you.");
        }

        // 4. Business Logic Validation
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new RuntimeException("Can only respond to PENDING requests.");
        }

        // 5. Update and Save
        booking.setStatus(newStatus);
        booking.setProviderMessage(message);
        return bookingRepository.save(booking);
    }

    @Transactional
    public HotelBooking cancelBooking(Long bookingId, Authentication auth) {
        // 1. Fetch the booking
        HotelBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // 2. Extract current Traveller ID from Security Context
        Map<String, Object> principal = (Map<String, Object>) auth.getPrincipal();
        Map<String, Object> profile = (Map<String, Object>) principal.get("travellerProfile");

        if (profile == null) throw new RuntimeException("Traveller profile not found");
        Long currentTravellerId = ((Number) profile.get("travellerId")).longValue();

        // 3. Security Check: Is this the traveller's own booking?
        if (!booking.getTravellerId().equals(currentTravellerId)) {
            throw new RuntimeException("Unauthorized: You can only cancel your own bookings.");
        }

        // 4. Update status
        booking.setStatus(BookingStatus.CANCELLED);
        return bookingRepository.save(booking);
    }
    // ... respondToBooking and cancelBooking methods ...
}