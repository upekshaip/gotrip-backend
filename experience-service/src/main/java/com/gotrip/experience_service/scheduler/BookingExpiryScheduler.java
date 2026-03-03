package com.gotrip.experience_service.scheduler;

import com.gotrip.experience_service.model.ExperienceBooking;
import com.gotrip.experience_service.repository.ExperienceBookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookingExpiryScheduler {

    private final ExperienceBookingRepository bookingRepository;

    /**
     * Runs every 5 minutes to check for expired pending bookings.
     * If a provider hasn't responded before the expiry time,
     * the booking is automatically set to EXPIRED so the traveller can move on.
     */
    @Scheduled(fixedRate = 300000) // 5 minutes in milliseconds
    public void expirePendingBookings() {
        List<ExperienceBooking> expiredBookings = bookingRepository
                .findByExpiresAtBeforeAndStatus(LocalDateTime.now(), "PENDING");

        if (!expiredBookings.isEmpty()) {
            log.info("Expiring {} pending bookings", expiredBookings.size());

            for (ExperienceBooking booking : expiredBookings) {
                booking.setStatus("EXPIRED");
                bookingRepository.save(booking);
                log.info("Booking {} expired (experience: {}, traveller: {})",
                        booking.getBookingId(),
                        booking.getExperience().getTitle(),
                        booking.getTravellerId());
            }
        }
    }
}
