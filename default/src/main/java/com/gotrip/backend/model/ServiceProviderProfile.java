package com.gotrip.backend.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "service_provider_profiles")
@EntityListeners(AuditingEntityListener.class) // Required for timestamps
public class ServiceProviderProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long providerId; // Independent ID to match Prisma's 'teacherId' logic

    // Manual setter (Lombok @Setter handles this, but keeping it as requested)
    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    @OnDelete(action = OnDeleteAction.SET_NULL) // This handles the DB-level 'onDelete: SetNull'
    @JsonBackReference
    private User user;

    @Column(length = 100)
    private String businessName;

    @Column(length = 255)
    private String businessAddress;

    @Column(length = 50)
    private String businessType;

    @Column(length = 20)
    private String businessPhone;

    // --- Auditing Fields ---
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

}