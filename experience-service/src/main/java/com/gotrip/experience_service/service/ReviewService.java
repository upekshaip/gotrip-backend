package com.gotrip.experience_service.service;

import com.gotrip.experience_service.dto.CreateReviewRequest;
import com.gotrip.experience_service.dto.ReviewResponseDTO;
import com.gotrip.experience_service.dto.ReviewSummaryDTO;
import com.gotrip.experience_service.exception.BadRequestException;
import com.gotrip.experience_service.exception.ResourceNotFoundException;
import com.gotrip.experience_service.model.Experience;
import com.gotrip.experience_service.model.Review;
import com.gotrip.experience_service.repository.ExperienceRepository;
import com.gotrip.experience_service.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ExperienceRepository experienceRepository;

    public ReviewResponseDTO createReview(CreateReviewRequest request, Long travellerId) {
        Experience experience = experienceRepository.findById(request.getExperienceId())
                .orElseThrow(() -> new ResourceNotFoundException("Experience not found"));

        // Check if the traveller has already reviewed this experience
        reviewRepository.findByExperienceExperienceIdAndTravellerId(request.getExperienceId(), travellerId)
                .ifPresent(existing -> {
                    throw new BadRequestException("You have already reviewed this experience");
                });

        // Validate rating range
        if (request.getRating() < 1 || request.getRating() > 5) {
            throw new BadRequestException("Rating must be between 1 and 5");
        }

        Review review = Review.builder()
                .experience(experience)
                .travellerId(travellerId)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        Review saved = reviewRepository.save(review);
        return mapToDTO(saved);
    }

    public ReviewResponseDTO updateReview(Long reviewId, CreateReviewRequest request, Long travellerId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        if (!review.getTravellerId().equals(travellerId)) {
            throw new BadRequestException("You can only update your own reviews");
        }

        if (request.getRating() < 1 || request.getRating() > 5) {
            throw new BadRequestException("Rating must be between 1 and 5");
        }

        review.setRating(request.getRating());
        review.setComment(request.getComment());

        Review updated = reviewRepository.save(review);
        return mapToDTO(updated);
    }

    public void deleteReview(Long reviewId, Long travellerId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        if (!review.getTravellerId().equals(travellerId)) {
            throw new BadRequestException("You can only delete your own reviews");
        }

        reviewRepository.delete(review);
    }

    public List<ReviewResponseDTO> getReviewsByExperience(Long experienceId) {
        return reviewRepository.findByExperienceExperienceId(experienceId)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    public List<ReviewResponseDTO> getMyReviews(Long travellerId) {
        return reviewRepository.findByTravellerId(travellerId)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    public ReviewSummaryDTO getReviewSummary(Long experienceId) {
        Double avgRating = reviewRepository.findAverageRatingByExperienceId(experienceId);
        Long totalReviews = reviewRepository.countByExperienceId(experienceId);

        return ReviewSummaryDTO.builder()
                .experienceId(experienceId)
                .averageRating(avgRating != null ? avgRating : 0.0)
                .totalReviews(totalReviews != null ? totalReviews : 0)
                .build();
    }

    private ReviewResponseDTO mapToDTO(Review review) {
        return ReviewResponseDTO.builder()
                .reviewId(review.getReviewId())
                .experienceId(review.getExperience().getExperienceId())
                .experienceTitle(review.getExperience().getTitle())
                .travellerId(review.getTravellerId())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}
