package com.gotrip.experience_service.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "experiences")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Experience {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long experienceId;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false)
    private String category; // TOUR, RENTAL, ACTIVITY

    @Column(nullable = false)
    private String type; // e.g., WHALE_WATCHING, HIKING, SCOOTER, SURFBOARD

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private double pricePerUnit;

    private String priceUnit; // PER_PERSON, PER_HOUR, PER_DAY, PER_ITEM

    private int maxCapacity;

    private String imageUrl;

    private boolean available;

    @Column(nullable = false)
    private Long providerId;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
