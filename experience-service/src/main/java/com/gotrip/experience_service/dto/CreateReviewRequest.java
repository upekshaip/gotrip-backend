package com.gotrip.experience_service.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateReviewRequest {

    private Long experienceId;

    private int rating; // 1 to 5

    private String comment;
}
