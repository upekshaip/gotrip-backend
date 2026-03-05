package com.gotrip.transport_service.model;

import com.gotrip.common_library.dto.transport_service.enums.TransportStatus;
import com.gotrip.common_library.dto.transport_service.enums.PriceUnit;
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
@Table(name = "transports")
@EntityListeners(AuditingEntityListener.class)
public class Transport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transportId;

    @Column(nullable = false)
    private Long providerId;

    @Column(nullable = false)
    private String vehicleMake;

    @Column(nullable = false)
    private String vehicleModel;

    @Column(nullable = false)
    private String vehicleType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransportStatus status = TransportStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String city; // Base city for the transport

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PriceUnit priceUnit = PriceUnit.PER_DAY; // Can be PER_DAY or PER_KM

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private int capacity; // Number of passengers

    // Base Location Coordinates
    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(nullable = false)
    private boolean isFeatured = false;

    @Column
    private String imageUrl;

    // Auditing Fields
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "transport", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<TransportReview> reviews;
}