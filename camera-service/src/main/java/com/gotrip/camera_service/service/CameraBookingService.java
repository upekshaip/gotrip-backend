package com.gotrip.camera_service.service;

import com.gotrip.common_library.dto.camera_service.*;
import com.gotrip.common_library.dto.camera_service.enums.BookingStatus;
import com.gotrip.common_library.dto.camera_service.enums.PriceUnit;
import com.gotrip.common_library.dto.user.TravellerContactInfo;
import com.gotrip.camera_service.client.UserServiceClient;
import com.gotrip.camera_service.model.Camera;
import com.gotrip.camera_service.model.CameraBooking;
import com.gotrip.camera_service.repository.CameraBookingRepository;
import com.gotrip.camera_service.repository.CameraRepository;
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
public class CameraBookingService {

    private final CameraBookingRepository bookingRepository;
    private final CameraRepository cameraRepository;
    private final UserServiceClient userServiceClient;

    @Transactional
    public CameraBooking createBookingRequest(CameraBookingRequest req, Authentication auth) {
        Map<String, Object> principal = (Map<String, Object>) auth.getPrincipal();

        Map<String, Object> travellerProfile = (Map<String, Object>) principal.get("travellerProfile");
        if (travellerProfile == null) {
            throw new RuntimeException("User does not have a Traveller profile.");
        }
        Long travellerId = ((Number) travellerProfile.get("travellerId")).longValue();

        Camera camera = cameraRepository.findById(req.cameraId())
                .orElseThrow(() -> new RuntimeException("Camera not found"));

        CameraBooking booking = new CameraBooking();
        booking.setTravellerId(travellerId);
        booking.setCameraId(req.cameraId());
        booking.setProviderId(camera.getProviderId());

         booking.setBasePrice(camera.getPrice());
         booking.setPriceUnit(camera.getPriceUnit());

        booking.setBookingReference("GT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        booking.setStatus(BookingStatus.PENDING);

        booking.setStartingDate(req.startingDate());
        booking.setStartingTime(req.startingTime());
        booking.setEndingDate(req.endingDate());
        booking.setEndingTime(req.endingTime());
        booking.setPersonCount(req.personCount());
        booking.setRequestMessage(req.requestMessage());

        BigDecimal total = BigDecimal.ZERO;
        BigDecimal discount = camera.getDiscount() != null ? camera.getDiscount() : BigDecimal.ZERO;

        if (camera.getPriceUnit() == PriceUnit.PER_DAY) {
            long days = ChronoUnit.DAYS.between(req.startingDate(), req.endingDate());
            if (days <= 0) days = 1;

            total = camera.getPrice()
                    .multiply(BigDecimal.valueOf(req.roomCount()))
                    .multiply(BigDecimal.valueOf(days));

        } else if (camera.getPriceUnit() == PriceUnit.PER_HOUR) {
            LocalDateTime start = LocalDateTime.of(req.startingDate(), req.startingTime());
            LocalDateTime end = LocalDateTime.of(req.endingDate(), req.endingTime());
            long hours = ChronoUnit.HOURS.between(start, end);
            if (hours <= 0) hours = 1;

            total = camera.getPrice()
                    .multiply(BigDecimal.valueOf(req.roomCount()))
                    .multiply(BigDecimal.valueOf(hours));

        } else if (camera.getPriceUnit() == PriceUnit.PER_PERSON) {
            long days = ChronoUnit.DAYS.between(req.startingDate(), req.endingDate());
            if (days <= 0) days = 1;

            total = camera.getPrice()
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
    public Page<CameraBookingResponse> getTravellerBookings(BookingStatus status, int page, int limit, Authentication auth) {
        Map<String, Object> principal = (Map<String, Object>) auth.getPrincipal();
        Map<String, Object> profile = (Map<String, Object>) principal.get("travellerProfile");

        if (profile == null) throw new RuntimeException("Traveller profile not found");
        Long travellerId = ((Number) profile.get("travellerId")).longValue();

        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by("createdAt").descending());

        Page<CameraBooking> bookings = (status != null)
                ? bookingRepository.findByTravellerIdAndStatus(travellerId, status, pageable)
                : bookingRepository.findByTravellerId(travellerId, pageable);

        return bookings.map(b -> {
            Camera camera = cameraRepository.findById(b.getCameraId()).orElse(null);
            return new CameraBookingResponse(
                    mapToBookingDTO(b),
                    mapToCameraSummary(camera),
                    null // Contact info not needed for traveller view
            );
        });
    }

    // Perspective: Booking Requests (Provider)
    public Page<CameraBookingResponse> getProviderBookings(BookingStatus status, int page, int limit, Authentication auth) {
        Map<String, Object> principal = (Map<String, Object>) auth.getPrincipal();
        Map<String, Object> profile = (Map<String, Object>) principal.get("serviceProviderProfile");

        if (profile == null) throw new RuntimeException("Service Provider profile not found");
        Long providerId = ((Number) profile.get("providerId")).longValue();

        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by("createdAt").descending());

        Page<CameraBooking> bookings = (status != null)
                ? bookingRepository.findByProviderIdAndStatus(providerId, status, pageable)
                : bookingRepository.findByProviderId(providerId, pageable);

        return bookings.map(b -> {
            Camera camera = cameraRepository.findById(b.getCameraId()).orElse(null);
            TravellerContactInfo contact = userServiceClient.getTravellerContact(b.getTravellerId());

            return new CameraBookingResponse(
                    mapToBookingDTO(b),
                    mapToCameraSummary(camera),
                    contact
            );
        });
    }

    private CameraSummaryResponse mapToCameraSummary(Camera h) {
        if (h == null) return null;
        return new CameraSummaryResponse(
                h.getCameraId(),
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

    private CameraBookingDTO mapToBookingDTO(CameraBooking b) {
        return new CameraBookingDTO(
                b.getBookingId(), b.getBookingReference(), b.getStatus(),
                b.getPersonCount(), b.getRoomCount(), b.getStartingDate(),
                b.getStartingTime(), b.getEndingDate(), b.getEndingTime(),
                b.getFinalAmount(), b.getRequestMessage(), b.getProviderMessage(),
                b.getCreatedAt(), b.getTotalAmount(), b.getDiscountAmount(), b.getBasePrice()
        );
    }


    @Transactional
    public CameraBooking respondToBooking(Long bookingId, CameraRespondDTO cameraRespondDTO, Authentication auth) {
        // 1. Fetch the booking
        CameraBooking booking = bookingRepository.findById(bookingId)
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
        booking.setStatus(cameraRespondDTO.status());
        booking.setProviderMessage(cameraRespondDTO.message());
        return bookingRepository.save(booking);
    }

    @Transactional
    public CameraBooking cancelBooking(Long bookingId, Authentication auth) {
        // 1. Fetch the booking
        CameraBooking booking = bookingRepository.findById(bookingId)
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