package com.gotrip.experience_service.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewResponseDTO {

    private Long reviewId;

    private Long experienceId;

    private String experienceTitle;

    private Long travellerId;

    private int rating;

    private String comment;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
