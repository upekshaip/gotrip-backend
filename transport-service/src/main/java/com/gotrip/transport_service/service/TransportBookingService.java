package com.gotrip.transport_service.service;

import com.gotrip.common_library.dto.transport_service.TransportBookingRequest;
import com.gotrip.common_library.dto.hotel_service.enums.BookingStatus;
import com.gotrip.transport_service.model.Transport;
import com.gotrip.transport_service.model.TransportBooking;
import com.gotrip.transport_service.repository.TransportBookingRepository;
import com.gotrip.transport_service.repository.TransportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransportBookingService {

    private final TransportBookingRepository bookingRepository;
    private final TransportRepository transportRepository;

    @Transactional
    public TransportBooking createBookingRequest(TransportBookingRequest req, Authentication auth) {
        Map<String, Object> principal = (Map<String, Object>) auth.getPrincipal();

        // Extract travellerId from the nested travellerProfile
        Map<String, Object> travellerProfile = (Map<String, Object>) principal.get("travellerProfile");
        if (travellerProfile == null) {
            throw new RuntimeException("User does not have a Traveller profile.");
        }
        Long travellerId = ((Number) travellerProfile.get("travellerId")).longValue();

        Transport transport = transportRepository.findById(req.transportId())
                .orElseThrow(() -> new RuntimeException("Transport vehicle not found"));

        TransportBooking booking = new TransportBooking();
        booking.setTravellerId(travellerId);
        booking.setTransportId(req.transportId());
        booking.setBookingReference("TR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        booking.setStatus(BookingStatus.PENDING);

        // Map transport specific fields
        booking.setPickupLocation(req.pickupLocation());
        booking.setDropoffLocation(req.dropoffLocation());
        booking.setStartingDate(req.startingDate());
        booking.setStartingTime(req.startingTime());
        booking.setEndingDate(req.endingDate());
        booking.setEndingTime(req.endingTime());
        booking.setRequestMessage(req.requestMessage());

        // Price calculation
        long days = ChronoUnit.DAYS.between(req.startingDate(), req.endingDate());
        if (days <= 0) days = 1;
        BigDecimal total = transport.getPrice().multiply(BigDecimal.valueOf(days));

        booking.setTotalAmount(total);
        booking.setFinalAmount(total);

        return bookingRepository.save(booking);
    }

    @Transactional
    public TransportBooking respondToBooking(Long bookingId, BookingStatus newStatus, String message, Authentication auth) {
        TransportBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // Verify the user responding is the actual Provider of this transport
        validateProviderOwnership(booking.getTransportId(), auth);

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
    public TransportBooking cancelBooking(Long bookingId, Authentication auth) {
        TransportBooking booking = bookingRepository.findById(bookingId)
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

    private void validateProviderOwnership(Long transportId, Authentication auth) {
        Transport transport = transportRepository.findById(transportId).orElseThrow();
        Map<String, Object> principal = (Map<String, Object>) auth.getPrincipal();
        Map<String, Object> profile = (Map<String, Object>) principal.get("serviceProviderProfile");
        Long providerId = ((Number) profile.get("providerId")).longValue();

        if (!transport.getProviderId().equals(providerId)) {
            throw new RuntimeException("Unauthorized: You don't own this transport vehicle.");
        }
    }
}