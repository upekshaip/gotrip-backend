package com.gotrip.experience_service.repository;

import com.gotrip.experience_service.model.Experience;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExperienceRepository extends JpaRepository<Experience, Long> {

    // In ExperienceRepository.java
    Page<Experience> findByProviderId(Long providerId, Pageable pageable);

    List<Experience> findByCategory(String category);

    List<Experience> findByType(String type);

    List<Experience> findByLocation(String location);

    // In ExperienceRepository.java
    Page<Experience> findByAvailableTrue(Pageable pageable);

    long countByAvailableTrue();

    List<Experience> findByCategoryAndAvailableTrue(String category);

    List<Experience> findByLocationAndAvailableTrue(String location);
}
