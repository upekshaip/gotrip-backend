package com.gotrip.transport_service.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "transport_reviews")
public class TransportReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reviewId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transport_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Transport transport;

    private Long travellerId;

    private Long bookingId;

    @Column(nullable = false)
    private Integer rating; // 1 to 5 stars

    @Column(length = 1000)
    private String comment;

    @CreationTimestamp
    private LocalDateTime createdAt;
}