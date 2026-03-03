package com.gotrip.experience_service.service;

import com.gotrip.experience_service.dto.CreateReviewRequest;
import com.gotrip.experience_service.dto.ReviewResponseDTO;
import com.gotrip.experience_service.dto.ReviewSummaryDTO;
import com.gotrip.experience_service.exception.BadRequestException;
import com.gotrip.experience_service.exception.ResourceNotFoundException;
import com.gotrip.experience_service.model.Experience;
import com.gotrip.experience_service.model.ExperienceReview;
import com.gotrip.experience_service.repository.ExperienceRepository;
import com.gotrip.experience_service.repository.ExperienceReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ExperienceReviewRepository experienceReviewRepository;
    private final ExperienceRepository experienceRepository;

    public ReviewResponseDTO createReview(CreateReviewRequest request, Long travellerId) {
        Experience experience = experienceRepository.findById(request.getExperienceId())
                .orElseThrow(() -> new ResourceNotFoundException("Experience not found"));

        // Check if the traveller has already reviewed this experience
        experienceReviewRepository.findByExperienceExperienceIdAndTravellerId(request.getExperienceId(), travellerId)
                .ifPresent(existing -> {
                    throw new BadRequestException("You have already reviewed this experience");
                });

        // Validate rating range
        if (request.getRating() < 1 || request.getRating() > 5) {
            throw new BadRequestException("Rating must be between 1 and 5");
        }

        ExperienceReview experienceReview = ExperienceReview.builder()
                .experience(experience)
                .travellerId(travellerId)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        ExperienceReview saved = experienceReviewRepository.save(experienceReview);
        return mapToDTO(saved);
    }

    public ReviewResponseDTO updateReview(Long reviewId, CreateReviewRequest request, Long travellerId) {
        ExperienceReview experienceReview = experienceReviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        if (!experienceReview.getTravellerId().equals(travellerId)) {
            throw new BadRequestException("You can only update your own reviews");
        }

        if (request.getRating() < 1 || request.getRating() > 5) {
            throw new BadRequestException("Rating must be between 1 and 5");
        }

        experienceReview.setRating(request.getRating());
        experienceReview.setComment(request.getComment());

        ExperienceReview updated = experienceReviewRepository.save(experienceReview);
        return mapToDTO(updated);
    }

    public void deleteReview(Long reviewId, Long travellerId) {
        ExperienceReview experienceReview = experienceReviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        if (!experienceReview.getTravellerId().equals(travellerId)) {
            throw new BadRequestException("You can only delete your own reviews");
        }

        experienceReviewRepository.delete(experienceReview);
    }

    public List<ReviewResponseDTO> getReviewsByExperience(Long experienceId) {
        return experienceReviewRepository.findByExperienceExperienceId(experienceId)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    public List<ReviewResponseDTO> getMyReviews(Long travellerId) {
        return experienceReviewRepository.findByTravellerId(travellerId)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    public ReviewSummaryDTO getReviewSummary(Long experienceId) {
        Double avgRating = experienceReviewRepository.findAverageRatingByExperienceId(experienceId);
        Long totalReviews = experienceReviewRepository.countByExperienceId(experienceId);

        return ReviewSummaryDTO.builder()
                .experienceId(experienceId)
                .averageRating(avgRating != null ? avgRating : 0.0)
                .totalReviews(totalReviews != null ? totalReviews : 0)
                .build();
    }

    private ReviewResponseDTO mapToDTO(ExperienceReview experienceReview) {
        return ReviewResponseDTO.builder()
                .reviewId(experienceReview.getReviewId())
                .experienceId(experienceReview.getExperience().getExperienceId())
                .experienceTitle(experienceReview.getExperience().getTitle())
                .travellerId(experienceReview.getTravellerId())
                .rating(experienceReview.getRating())
                .comment(experienceReview.getComment())
                .createdAt(experienceReview.getCreatedAt())
                .updatedAt(experienceReview.getUpdatedAt())
                .build();
    }
}
