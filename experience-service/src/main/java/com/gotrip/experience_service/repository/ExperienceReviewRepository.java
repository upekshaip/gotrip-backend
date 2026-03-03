package com.gotrip.experience_service.repository;

import com.gotrip.experience_service.model.ExperienceReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExperienceReviewRepository extends JpaRepository<ExperienceReview, Long> {

    List<ExperienceReview> findByExperienceExperienceId(Long experienceId);

    List<ExperienceReview> findByTravellerId(Long travellerId);

    Optional<ExperienceReview> findByExperienceExperienceIdAndTravellerId(Long experienceId, Long travellerId);

    @Query("SELECT AVG(r.rating) FROM ExperienceReview r WHERE r.experience.experienceId = :experienceId")
    Double findAverageRatingByExperienceId(Long experienceId);

    @Query("SELECT COUNT(r) FROM ExperienceReview r WHERE r.experience.experienceId = :experienceId")
    Long countByExperienceId(Long experienceId);
}
