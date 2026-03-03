package com.gotrip.hotel_service.model;

import com.gotrip.hotel_service.model.enums.BookingStatus;
import jakarta.persistence.*;
        import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Setter
@Getter
@Table(name = "hotel_bookings")
@EntityListeners(AuditingEntityListener.class)
public class HotelBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookingId;

    @Column(nullable = false)
    private Long travellerId;

    @Column(unique = true, nullable = false)
    private String bookingReference; // e.g., GT-2026-X89

    //    if approved changed to approve, or rejected, else timeout
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status = BookingStatus.PENDING;

    @Column(nullable = false)
    private Long hotelId;

    @Column(nullable = false)
    private int personCount;

    @Column(nullable = false)
    private String requestMessage;

    @Column
    private String providerMessage;

    @Column(nullable = false)
    private LocalDate startingDate;

    @Column(nullable = false)
    private LocalTime startingTime;

    @Column(nullable = false)
    private LocalDate endingDate;

    @Column(nullable = false)
    private LocalTime endingTime;

    @Column(precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(precision = 10, scale = 2)
    private BigDecimal discountAmount;

    @Column(precision = 10, scale = 2)
    private BigDecimal finalAmount;

    @Column
    private Long reviewId;

    // --- Auditing Fields ---
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

}