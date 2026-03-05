package com.gotrip.experience_service.service;

import com.gotrip.common_library.dto.user.TravellerContactInfo;
import com.gotrip.experience_service.client.UserServiceClient;
import com.gotrip.experience_service.dto.*;
import com.gotrip.experience_service.model.Experience;
import com.gotrip.experience_service.model.ExperienceBooking;
import com.gotrip.experience_service.model.enums.BookingStatus;
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
                .requestMessage(request.requestMessage())
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

        booking.setProviderMessage(actionDTO.message());
        if (actionDTO.status() == BookingStatus.ACCEPTED) {
            booking.setStatus("ACCEPTED");
        } else if (actionDTO.status() == BookingStatus.DECLINED) {
            booking.setStatus("DECLINED");
        } else {
            throw new RuntimeException("Invalid action. Use ACCEPTED or DECLINED");
        }
        ExperienceBooking saved = bookingRepository.save(booking);
        TravellerContactInfo contact = userServiceClient.getTravellerContact(saved.getTravellerId());
        return mapToResponse(saved, contact);
    }

    public BookingResponseDTO cancelBooking(Long bookingId, Authentication authentication) {
        Long travellerId = extractTravellerId(authentication);

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

    public Page<ProviderBookingDetailDTO> getBookingsByTraveller(Authentication authentication, Pageable pageable) {
        Long travellerId = extractTravellerId(authentication);

        // Fetch the traveller's own contact info once
        TravellerContactInfo contact = userServiceClient.getTravellerContact(travellerId);

        // Fetch paginated bookings with experience details
        Page<ExperienceBooking> bookingPage = bookingRepository.findByTravellerIdWithExperience(travellerId, pageable);

        return bookingPage.map(booking -> {
            // 1. Map the standard BookingResponseDTO
            BookingResponseDTO bookingDto = mapToResponse(booking, contact);

            // 2. Manually map the Experience entity to ExperienceResponseDTO
            Experience exp = booking.getExperience();
            ExperienceResponseDTO expDto = new ExperienceResponseDTO(
                    exp.getExperienceId(),
                    exp.getTitle(),
                    exp.getDescription(),
                    exp.getCategory(),
                    exp.getType(),
                    exp.getLocation(),
                    exp.getPricePerUnit(),
                    exp.getPriceUnit(),
                    exp.getMaxCapacity(),
                    exp.getImageUrl(),
                    exp.isAvailable(),
                    exp.getProviderId(),
                    exp.getCreatedAt(),
                    exp.getUpdatedAt()
            );

            // 3. Return the combined wrapper
            return new ProviderBookingDetailDTO(bookingDto, expDto);
        });
    }



    public Page<ProviderBookingDetailDTO> getBookingsByProvider(Authentication authentication, Pageable pageable) {
        Long providerId = extractProviderId(authentication);

        // Fetch bookings along with experience data to avoid N+1 issues
        Page<ExperienceBooking> bookingPage = bookingRepository.findByProviderIdWithExperience(providerId, pageable);

        return bookingPage.map(booking -> {
            // 1. Fetch external traveller details
            TravellerContactInfo contact = userServiceClient.getTravellerContact(booking.getTravellerId());

            // 2. Map the standard BookingResponseDTO
            BookingResponseDTO bookingDto = mapToResponse(booking, contact);

            // 3. Manually map the Experience entity to ExperienceResponseDTO
            Experience exp = booking.getExperience();
            ExperienceResponseDTO expDto = new ExperienceResponseDTO(
                    exp.getExperienceId(),
                    exp.getTitle(),
                    exp.getDescription(),
                    exp.getCategory(),
                    exp.getType(),
                    exp.getLocation(),
                    exp.getPricePerUnit(),
                    exp.getPriceUnit(),
                    exp.getMaxCapacity(),
                    exp.getImageUrl(),
                    exp.isAvailable(),
                    exp.getProviderId(),
                    exp.getCreatedAt(),
                    exp.getUpdatedAt()
            );

            // 4. Return the combined wrapper
            return new ProviderBookingDetailDTO(bookingDto, expDto);
        });
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

    public long countAll() {
        return bookingRepository.count();
    }

    public long countPending() {
        return bookingRepository.countByStatus("PENDING");
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
                booking.getRequestMessage(),
                booking.getExpiresAt(),
                booking.getCreatedAt(),
                booking.getUpdatedAt(),
                contact // Injecting the external data here
        );
    }

    private Long extractTravellerId(Authentication auth) {
        Map<String, Object> principal = (Map<String, Object>) auth.getPrincipal();
        if (!(boolean) principal.getOrDefault("traveller", false)) {
            throw new RuntimeException("Unauthorized: You are not authorized to perform this operation..");
        }
        Map<String, Object> profile = (Map<String, Object>) principal.get("travellerProfile");
        return ((Number) profile.get("travellerId")).longValue();
    }

    private Long extractProviderId(Authentication auth) {
        Map<String, Object> principal = (Map<String, Object>) auth.getPrincipal();
        if (!(boolean) principal.getOrDefault("serviceProvider", false)) {
            throw new RuntimeException("Unauthorized: Only Service Providers can manage listings.");
        }
        Map<String, Object> profile = (Map<String, Object>) principal.get("serviceProviderProfile");
        return ((Number) profile.get("providerId")).longValue();
    }
}
