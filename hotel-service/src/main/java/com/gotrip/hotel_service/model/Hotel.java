package com.gotrip.hotel_service.model;

import com.gotrip.hotel_service.model.enums.PriceUnit;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Setter
@Getter
@Table(name = "hotels")
@EntityListeners(AuditingEntityListener.class)
public class Hotel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long hotelId;

    @Column(nullable = false)
    private Long providerId; // The service provider who owns this hotel

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String status; //

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String city;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PriceUnit priceUnit = PriceUnit.PER_DAY; // Default for hotels

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    // Geographic Coordinates
    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;


    @Column(nullable = false)
    private boolean isActive = true; // Is the hotel currently listed?

    @Column(nullable = false)
    private boolean isFeatured = false; // Is the hotel currently featured?

    @Column
    private String imageUrl; // URL to the main thumbnail

    // --- Auditing Fields ---
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Relationship to reviews (Optional, but helpful for fetching)
    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<HotelReview> reviews;
}