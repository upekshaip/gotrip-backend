package com.gotrip.hotel_service.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
@Table(name = "hotels")
@EntityListeners(AuditingEntityListener.class)
public class Hotel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookingId;

    //    if approved changed to approve, or rejected, else timeout
    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private Long travellerId;

    @Column(nullable = false)
    private Long bookingServiceId;

    @Column(nullable = false)
    private int personCount;

    @Column(nullable = false)
    private String reason;

    @Column
    private boolean isApproved;

    @Column(nullable = false)
    private String requestMessage;





    // --- Auditing Fields ---
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

}