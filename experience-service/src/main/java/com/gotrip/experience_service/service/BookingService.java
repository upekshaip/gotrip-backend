package com.gotrip.experience_service.service;

import com.gotrip.experience_service.dto.*;
import com.gotrip.experience_service.model.Experience;
import com.gotrip.experience_service.model.ExperienceBooking;
import com.gotrip.experience_service.repository.ExperienceBookingRepository;
import com.gotrip.experience_service.repository.ExperienceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

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

        ExperienceBooking saved = bookingRepository.save(booking);
        return mapToResponse(saved);
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
        return mapToResponse(saved);
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
        return mapToResponse(saved);
    }

    public List<BookingResponseDTO> getBookingsByTraveller(Long travellerId) {
        return bookingRepository.findByTravellerId(travellerId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<BookingResponseDTO> getBookingsByProvider(Long providerId) {
        return bookingRepository.findByProviderId(providerId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<BookingResponseDTO> getPendingBookingsByProvider(Long providerId) {
        return bookingRepository.findByProviderIdAndStatus(providerId, "PENDING").stream()
                .map(this::mapToResponse)
                .toList();
    }

    public BookingResponseDTO getBookingById(Long bookingId) {
        ExperienceBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        return mapToResponse(booking);
    }

    private BookingResponseDTO mapToResponse(ExperienceBooking booking) {
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
                booking.getUpdatedAt()
        );
    }
}
