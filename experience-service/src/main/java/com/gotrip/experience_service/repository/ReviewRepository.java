package com.gotrip.experience_service.repository;

import com.gotrip.experience_service.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByExperienceExperienceId(Long experienceId);

    List<Review> findByTravellerId(Long travellerId);

    Optional<Review> findByExperienceExperienceIdAndTravellerId(Long experienceId, Long travellerId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.experience.experienceId = :experienceId")
    Double findAverageRatingByExperienceId(Long experienceId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.experience.experienceId = :experienceId")
    Long countByExperienceId(Long experienceId);
}
