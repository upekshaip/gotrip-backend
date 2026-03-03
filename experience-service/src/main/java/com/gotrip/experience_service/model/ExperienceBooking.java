package com.gotrip.experience_service.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "experience_bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class ExperienceBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "experience_id", nullable = false)
    private Experience experience;

    @Column(nullable = false)
    private Long travellerId;

    @Column(nullable = false)
    private Long providerId;

    @Column(nullable = false)
    private LocalDate bookingDate;

    private LocalTime startTime;

    private int quantity; // number of pax or items

    private int durationHours;

    @Column(nullable = false)
    private double totalPrice;

    @Column(nullable = false)
    private String status; // PENDING, ACCEPTED, DECLINED, CANCELLED, EXPIRED, COMPLETED

    private String providerMessage;

    private String declineReason;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
