package com.gotrip.backend.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Setter
@Getter
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(length = 50)
    private String name;

    @Column(length = 15)
    private String phone;

    @Column(length = 10) // e.g., MALE, FEMALE
    private String gender;

    private LocalDate dob;

    @Column(length = 255)
    private String password;

    @Column(columnDefinition = "TEXT") // Matches @db.Text
    private String refreshToken;

    private LocalDateTime refreshTokenExpiry;

    // --- Auditing Fields ---
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private boolean traveller = false;

    @Column(nullable = false)
    private boolean serviceProvider = false;

    @Column(nullable = false)
    private boolean admin = false;

    // Relationships
    // We removed CascadeType.ALL because we want the Profiles to survive User deletion
    @OneToOne(mappedBy = "user", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JsonManagedReference
    private TravellerProfile travellerProfile;

    @OneToOne(mappedBy = "user", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JsonManagedReference
    private ServiceProviderProfile serviceProviderProfile;

    @OneToOne(mappedBy = "user", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JsonManagedReference
    private AdminProfile adminProfile;

    // Lifecycle method for "Set Null" logic
    @PreRemove
    private void preRemove() {
        if (travellerProfile != null) {
            travellerProfile.setUser(null);
        }
        if (serviceProviderProfile != null) {
            serviceProviderProfile.setUser(null);
        }
        if (adminProfile != null) {
            adminProfile.setUser(null);
        }
    }
}