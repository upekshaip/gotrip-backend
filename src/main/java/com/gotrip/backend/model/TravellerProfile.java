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
@EntityListeners(AuditingEntityListener.class) // Required for timestamps
@Table(name = "traveller_profiles")
public class TravellerProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long travellerId; // Independent ID like your Prisma 'studentId'

    // Manual setter if not using Lombok @Setter for 'user'
    @OneToOne
    @JoinColumn(name = "user_id", unique = true) // Nullable by default
    @OnDelete(action = OnDeleteAction.SET_NULL) // This is your 'onDelete: SetNull' logic
    //  @JsonIgnore
    @JsonBackReference
    private User user;

    @Column(length = 255)
    private String passportInfo;

    // --- Auditing Fields ---
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

}