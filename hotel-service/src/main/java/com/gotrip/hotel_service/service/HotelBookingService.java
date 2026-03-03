package com.gotrip.hotel_service.service;

import com.gotrip.common_library.dto.hotel_service.HotelBookingRequest;
import com.gotrip.common_library.dto.hotel_service.enums.BookingStatus;
import com.gotrip.hotel_service.model.Hotel;
import com.gotrip.hotel_service.model.HotelBooking;
import com.gotrip.hotel_service.repository.HotelBookingRepository;
import com.gotrip.hotel_service.repository.HotelRepository;
import lombok.RequiredArgsConstructor;
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

    @Transactional
    public HotelBooking createBookingRequest(HotelBookingRequest req, Authentication auth) {
        Map<String, Object> principal = (Map<String, Object>) auth.getPrincipal();

        // Extract travellerId from the nested travellerProfile
        Map<String, Object> travellerProfile = (Map<String, Object>) principal.get("travellerProfile");
        if (travellerProfile == null) {
            throw new RuntimeException("User does not have a Traveller profile.");
        }
        Long travellerId = ((Number) travellerProfile.get("travellerId")).longValue();

        Hotel hotel = hotelRepository.findById(req.hotelId())
                .orElseThrow(() -> new RuntimeException("Hotel not found"));

        HotelBooking booking = new HotelBooking();
        booking.setTravellerId(travellerId); // Correctly using 28, not userId
        booking.setHotelId(req.hotelId());
        booking.setBookingReference("GT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        booking.setStatus(BookingStatus.PENDING);

        // ... rest of mapping (dates, times, personCount)

        // Price calculation
        long days = ChronoUnit.DAYS.between(req.startingDate(), req.endingDate());
        if (days <= 0) days = 1;
        BigDecimal total = hotel.getPrice().multiply(BigDecimal.valueOf(days));

        booking.setTotalAmount(total);
        booking.setFinalAmount(total);

        return bookingRepository.save(booking);
    }

    @Transactional
    public HotelBooking respondToBooking(Long bookingId, BookingStatus newStatus, String message, Authentication auth) {
        HotelBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // Verify the user responding is the actual Provider of this hotel
        validateProviderOwnership(booking.getHotelId(), auth);

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new RuntimeException("Can only respond to PENDING requests.");
        }

        if (newStatus != BookingStatus.ACCEPTED && newStatus != BookingStatus.DECLINED) {
            throw new RuntimeException("Invalid response status.");
        }

        booking.setStatus(newStatus);
        booking.setProviderMessage(message);
        return bookingRepository.save(booking);
    }

    @Transactional
    public HotelBooking cancelBooking(Long bookingId, Authentication auth) {
        HotelBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        Map<String, Object> principal = (Map<String, Object>) auth.getPrincipal();
        Map<String, Object> travellerProfile = (Map<String, Object>) principal.get("travellerProfile");
        Long currentTravellerId = ((Number) travellerProfile.get("travellerId")).longValue();

        if (!booking.getTravellerId().equals(currentTravellerId)) {
            throw new RuntimeException("You can only cancel your own bookings.");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        return bookingRepository.save(booking);
    }

    private void validateProviderOwnership(Long hotelId, Authentication auth) {
        Hotel hotel = hotelRepository.findById(hotelId).orElseThrow();
        Map<String, Object> principal = (Map<String, Object>) auth.getPrincipal();
        Map<String, Object> profile = (Map<String, Object>) principal.get("serviceProviderProfile");
        Long providerId = ((Number) profile.get("providerId")).longValue();

        if (!hotel.getProviderId().equals(providerId)) {
            throw new RuntimeException("Unauthorized: You don't own this hotel.");
        }
    }
}