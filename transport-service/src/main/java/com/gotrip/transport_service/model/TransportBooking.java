package com.gotrip.transport_service.model;
import com.gotrip.common_library.dto.hotel_service.enums.BookingStatus;
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
@Table(name = "transport_bookings")
@EntityListeners(AuditingEntityListener.class)
public class TransportBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookingId;

    @Column(nullable = false)
    private Long travellerId;

    @Column(nullable = false)
    private Long providerId;

    @Column(unique = true, nullable = false)
    private String bookingReference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status = BookingStatus.PENDING;

    @Column(nullable = false)
    private Long transportId;

    @Column(nullable = false)
    private String pickupLocation;

    @Column(nullable = false)
    private String dropoffLocation;

    @Column
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

    //  Auditing Fields
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}