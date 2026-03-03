package com.gotrip.experience_service.service;

import com.gotrip.experience_service.dto.*;
import com.gotrip.experience_service.model.Experience;
import com.gotrip.experience_service.repository.ExperienceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExperienceService {

    private final ExperienceRepository experienceRepository;

    public ExperienceResponseDTO createExperience(CreateExperienceRequest request, Long providerId) {
        Experience experience = Experience.builder()
                .title(request.title())
                .description(request.description())
                .category(request.category())
                .type(request.type())
                .location(request.location())
                .pricePerUnit(request.pricePerUnit())
                .priceUnit(request.priceUnit())
                .maxCapacity(request.maxCapacity())
                .imageUrl(request.imageUrl())
                .available(true)
                .providerId(providerId)
                .build();

        Experience saved = experienceRepository.save(experience);
        return mapToResponse(saved);
    }

    public ExperienceResponseDTO updateExperience(Long experienceId, UpdateExperienceRequest request, Long providerId) {
        Experience experience = experienceRepository.findById(experienceId)
                .orElseThrow(() -> new RuntimeException("Experience not found"));

        if (!experience.getProviderId().equals(providerId)) {
            throw new RuntimeException("You are not authorized to update this experience");
        }

        if (request.title() != null) experience.setTitle(request.title());
        if (request.description() != null) experience.setDescription(request.description());
        if (request.category() != null) experience.setCategory(request.category());
        if (request.type() != null) experience.setType(request.type());
        if (request.location() != null) experience.setLocation(request.location());
        if (request.pricePerUnit() != null) experience.setPricePerUnit(request.pricePerUnit());
        if (request.priceUnit() != null) experience.setPriceUnit(request.priceUnit());
        if (request.maxCapacity() != null) experience.setMaxCapacity(request.maxCapacity());
        if (request.imageUrl() != null) experience.setImageUrl(request.imageUrl());
        if (request.available() != null) experience.setAvailable(request.available());

        Experience saved = experienceRepository.save(experience);
        return mapToResponse(saved);
    }

    public void deleteExperience(Long experienceId, Long providerId) {
        Experience experience = experienceRepository.findById(experienceId)
                .orElseThrow(() -> new RuntimeException("Experience not found"));

        if (!experience.getProviderId().equals(providerId)) {
            throw new RuntimeException("You are not authorized to delete this experience");
        }

        experienceRepository.delete(experience);
    }

    public ExperienceResponseDTO getExperienceById(Long experienceId) {
        Experience experience = experienceRepository.findById(experienceId)
                .orElseThrow(() -> new RuntimeException("Experience not found"));
        return mapToResponse(experience);
    }

    public List<ExperienceResponseDTO> getAllExperiences() {
        return experienceRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<ExperienceResponseDTO> getAvailableExperiences() {
        return experienceRepository.findByAvailableTrue().stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<ExperienceResponseDTO> getExperiencesByCategory(String category) {
        return experienceRepository.findByCategoryAndAvailableTrue(category).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<ExperienceResponseDTO> getExperiencesByLocation(String location) {
        return experienceRepository.findByLocationAndAvailableTrue(location).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<ExperienceResponseDTO> getExperiencesByProvider(Long providerId) {
        return experienceRepository.findByProviderId(providerId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    private ExperienceResponseDTO mapToResponse(Experience experience) {
        return new ExperienceResponseDTO(
                experience.getExperienceId(),
                experience.getTitle(),
                experience.getDescription(),
                experience.getCategory(),
                experience.getType(),
                experience.getLocation(),
                experience.getPricePerUnit(),
                experience.getPriceUnit(),
                experience.getMaxCapacity(),
                experience.getImageUrl(),
                experience.isAvailable(),
                experience.getProviderId(),
                experience.getCreatedAt(),
                experience.getUpdatedAt()
        );
    }
}
