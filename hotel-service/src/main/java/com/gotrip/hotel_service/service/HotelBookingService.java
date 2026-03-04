package com.gotrip.hotel_service.service;

import com.gotrip.common_library.dto.hotel_service.HotelBookingRequest;
import com.gotrip.common_library.dto.hotel_service.enums.BookingStatus;
import com.gotrip.common_library.dto.hotel_service.enums.PriceUnit;
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

        // 1. Extract travellerId safely
        Map<String, Object> travellerProfile = (Map<String, Object>) principal.get("travellerProfile");
        if (travellerProfile == null) {
            throw new RuntimeException("User does not have a Traveller profile.");
        }
        Long travellerId = ((Number) travellerProfile.get("travellerId")).longValue();

        // 2. Fetch Hotel
        Hotel hotel = hotelRepository.findById(req.hotelId())
                .orElseThrow(() -> new RuntimeException("Hotel not found"));

        // 3. Initialize Booking
        HotelBooking booking = new HotelBooking();
        booking.setTravellerId(travellerId);
        booking.setHotelId(req.hotelId());
        booking.setProviderId(hotel.getProviderId());
        booking.setBasePrice(hotel.getPrice());
        booking.setPriceUnit(hotel.getPriceUnit());

        booking.setBookingReference("GT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        booking.setStatus(BookingStatus.PENDING);

        // Mapping requested data
        booking.setStartingDate(req.startingDate());
        booking.setStartingTime(req.startingTime());
        booking.setEndingDate(req.endingDate());
        booking.setEndingTime(req.endingTime());
        booking.setPersonCount(req.personCount());
        booking.setRequestMessage(req.requestMessage());

        // 4. Advanced Price Calculation
        // roomCount logic integrated
        BigDecimal total = BigDecimal.ZERO;
        BigDecimal discount = hotel.getDiscount() != null ? hotel.getDiscount() : BigDecimal.ZERO;

        if (hotel.getPriceUnit() == PriceUnit.PER_DAY) {
            long days = ChronoUnit.DAYS.between(req.startingDate(), req.endingDate());
            if (days <= 0) days = 1;

            // Total = (Price per Room) * (Number of Rooms) * (Number of Days)
            total = hotel.getPrice()
                    .multiply(BigDecimal.valueOf(req.roomCount()))
                    .multiply(BigDecimal.valueOf(days));

        } else if (hotel.getPriceUnit() == PriceUnit.PER_HOUR) {
            LocalDateTime start = LocalDateTime.of(req.startingDate(), req.startingTime());
            LocalDateTime end = LocalDateTime.of(req.endingDate(), req.endingTime());
            long hours = ChronoUnit.HOURS.between(start, end);
            if (hours <= 0) hours = 1;

            // Total = (Price per Room per Hour) * (Number of Rooms) * (Number of Hours)
            total = hotel.getPrice()
                    .multiply(BigDecimal.valueOf(req.roomCount()))
                    .multiply(BigDecimal.valueOf(hours));

        } else if (hotel.getPriceUnit() == PriceUnit.PER_PERSON) {
            long days = ChronoUnit.DAYS.between(req.startingDate(), req.endingDate());
            if (days <= 0) days = 1;

            // Total = (Price per Person) * (Number of Persons) * (Number of Days)
            // Note: roomCount is usually implied by personCount here,
            // but if you want to charge per person AND per room, add the multiplier.
            total = hotel.getPrice()
                    .multiply(BigDecimal.valueOf(req.personCount()))
                    .multiply(BigDecimal.valueOf(days));
        }


        // Final assignment
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