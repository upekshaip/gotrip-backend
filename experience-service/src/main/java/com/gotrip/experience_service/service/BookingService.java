package com.gotrip.experience_service.service;

import com.gotrip.common_library.dto.user.TravellerContactInfo;
import com.gotrip.experience_service.client.UserServiceClient;
import com.gotrip.experience_service.dto.*;
import com.gotrip.experience_service.model.Experience;
import com.gotrip.experience_service.model.ExperienceBooking;
import com.gotrip.experience_service.repository.ExperienceBookingRepository;
import com.gotrip.experience_service.repository.ExperienceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final ExperienceBookingRepository bookingRepository;
    private final ExperienceRepository experienceRepository;

    // 4-hour buffer rule: no booking starting in less than 4 hours
    private static final int MIN_HOURS_BEFORE_START = 4;

    // Expiry timer: provider has 1 hour to respond for urgent bookings (< 6 hours),
    // otherwise 24 hours
    private static final int URGENT_THRESHOLD_HOURS = 6;
    private static final int URGENT_EXPIRY_HOURS = 1;
    private static final int NORMAL_EXPIRY_HOURS = 24;
    private final UserServiceClient userServiceClient;

    public BookingResponseDTO createBooking(BookingRequestDTO request, Long travellerId) {
        Experience experience = experienceRepository.findById(request.experienceId())
                .orElseThrow(() -> new RuntimeException("Experience not found"));

        if (!experience.isAvailable()) {
            throw new RuntimeException("This experience is not currently available");
        }

        // 4-hour buffer check
        LocalDateTime bookingStart = request.bookingDate().atTime(
                request.startTime() != null ? request.startTime() : java.time.LocalTime.of(9, 0)
        );
        if (bookingStart.isBefore(LocalDateTime.now().plusHours(MIN_HOURS_BEFORE_START))) {
            throw new RuntimeException("Bookings must be made at least 4 hours in advance");
        }

        // Calculate total price
        double totalPrice = experience.getPricePerUnit() * request.quantity();
        if (request.durationHours() > 0 && "PER_HOUR".equals(experience.getPriceUnit())) {
            totalPrice = experience.getPricePerUnit() * request.durationHours() * request.quantity();
        }

        // Calculate expiry based on urgency
        long hoursUntilStart = java.time.Duration.between(LocalDateTime.now(), bookingStart).toHours();
        int expiryHours = hoursUntilStart < URGENT_THRESHOLD_HOURS ? URGENT_EXPIRY_HOURS : NORMAL_EXPIRY_HOURS;

        ExperienceBooking booking = ExperienceBooking.builder()
                .experience(experience)
                .travellerId(travellerId)
                .providerId(experience.getProviderId())
                .bookingDate(request.bookingDate())
                .startTime(request.startTime())
                .quantity(request.quantity())
                .durationHours(request.durationHours())
                .totalPrice(totalPrice)
                .status("PENDING")
                .expiresAt(LocalDateTime.now().plusHours(expiryHours))
                .build();

        TravellerContactInfo contact = userServiceClient.getTravellerContact(travellerId);
        ExperienceBooking saved = bookingRepository.save(booking);
        return mapToResponse(saved, contact);
    }

    public BookingResponseDTO providerAction(Long bookingId, ProviderActionDTO actionDTO, Long providerId) {
        ExperienceBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!booking.getProviderId().equals(providerId)) {
            throw new RuntimeException("You are not authorized to manage this booking");
        }

        if (!"PENDING".equals(booking.getStatus())) {
            throw new RuntimeException("Booking is no longer pending");
        }

        if (booking.getExpiresAt().isBefore(LocalDateTime.now())) {
            booking.setStatus("EXPIRED");
            bookingRepository.save(booking);
            throw new RuntimeException("Booking request has expired");
        }

        if ("ACCEPT".equalsIgnoreCase(actionDTO.action())) {
            booking.setStatus("ACCEPTED");
            booking.setProviderMessage(actionDTO.message());
        } else if ("DECLINE".equalsIgnoreCase(actionDTO.action())) {
            booking.setStatus("DECLINED");
            booking.setDeclineReason(actionDTO.declineReason());
        } else {
            throw new RuntimeException("Invalid action. Use ACCEPT or DECLINE");
        }
        ExperienceBooking saved = bookingRepository.save(booking);
        TravellerContactInfo contact = userServiceClient.getTravellerContact(saved.getTravellerId());
        return mapToResponse(saved, contact);
    }

    public BookingResponseDTO cancelBooking(Long bookingId, Long travellerId) {
        ExperienceBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!booking.getTravellerId().equals(travellerId)) {
            throw new RuntimeException("You are not authorized to cancel this booking");
        }

        if ("COMPLETED".equals(booking.getStatus()) || "CANCELLED".equals(booking.getStatus())) {
            throw new RuntimeException("Booking cannot be cancelled in its current state");
        }

        booking.setStatus("CANCELLED");
        ExperienceBooking saved = bookingRepository.save(booking);
        TravellerContactInfo contact = userServiceClient.getTravellerContact(travellerId);
        return mapToResponse(saved, contact);
    }

    public List<BookingResponseDTO> getBookingsByTraveller(Long travellerId) {
        TravellerContactInfo contact = userServiceClient.getTravellerContact(travellerId);
        return bookingRepository.findByTravellerId(travellerId).stream()
                .map(booking -> mapToResponse(booking, contact))
                .toList();
    }

    public Page<BookingResponseDTO> getBookingsByProvider(Authentication authentication, Pageable pageable) {
        Long providerId = extractProviderId(authentication);
        Page<ExperienceBooking> bookingPage = bookingRepository.findByProviderId(providerId, pageable);

        return bookingPage.map(booking -> {
            // Fetch details for each traveller
            TravellerContactInfo contact = userServiceClient.getTravellerContact(booking.getTravellerId());
            return mapToResponse(booking, contact);
        });
    }

    private Long extractProviderId(Authentication auth) {
        Map<String, Object> principal = (Map<String, Object>) auth.getPrincipal();
        if (!(boolean) principal.getOrDefault("serviceProvider", false)) {
            throw new RuntimeException("Unauthorized: Only Service Providers can manage listings.");
        }
        Map<String, Object> profile = (Map<String, Object>) principal.get("serviceProviderProfile");
        return ((Number) profile.get("providerId")).longValue();
    }

    public List<BookingResponseDTO> getPendingBookingsByProvider(Long providerId) {
        return bookingRepository.findByProviderIdAndStatus(providerId, "PENDING").stream()
                .map(booking -> {
                    TravellerContactInfo contact = userServiceClient.getTravellerContact(booking.getTravellerId());
                    return mapToResponse(booking, contact);
                })
                .toList();
    }

    public BookingResponseDTO getBookingById(Long bookingId) {
        ExperienceBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        TravellerContactInfo contact = userServiceClient.getTravellerContact(booking.getTravellerId());
        return mapToResponse(booking, contact);
    }

    private BookingResponseDTO mapToResponse(ExperienceBooking booking, TravellerContactInfo contact) {
        return new BookingResponseDTO(
                booking.getBookingId(),
                booking.getExperience().getExperienceId(),
                booking.getExperience().getTitle(),
                booking.getExperience().getCategory(),
                booking.getExperience().getType(),
                booking.getTravellerId(),
                booking.getProviderId(),
                booking.getBookingDate(),
                booking.getStartTime(),
                booking.getQuantity(),
                booking.getDurationHours(),
                booking.getTotalPrice(),
                booking.getStatus(),
                booking.getProviderMessage(),
                booking.getDeclineReason(),
                booking.getExpiresAt(),
                booking.getCreatedAt(),
                booking.getUpdatedAt(),
                contact // Injecting the external data here
        );
    }
}
