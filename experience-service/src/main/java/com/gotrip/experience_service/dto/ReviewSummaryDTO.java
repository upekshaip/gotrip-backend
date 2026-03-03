package com.gotrip.experience_service.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewSummaryDTO {

    private Long experienceId;

    private double averageRating;

    private long totalReviews;
}
