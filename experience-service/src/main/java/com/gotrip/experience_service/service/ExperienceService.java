package com.gotrip.experience_service.service;

import com.gotrip.common_library.dto.user.TravellerContactInfo;
import com.gotrip.experience_service.client.UserServiceClient;
import com.gotrip.experience_service.dto.*;
import com.gotrip.experience_service.model.Experience;
import com.gotrip.experience_service.repository.ExperienceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ExperienceService {

    private final ExperienceRepository experienceRepository;
    private final UserServiceClient userServiceClient;

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


    public ExperienceResponseDTO updateByAdmin(Long experienceId, UpdateExperienceRequest request, Authentication auth) {
        extractAdminId(auth);

        Experience experience = experienceRepository.findById(experienceId)
                .orElseThrow(() -> new RuntimeException("Experience not found"));

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

    public ExperienceResponseDTO updateAvailableByAdmin(Long experienceId, UpdateExperienceRequest request, Authentication auth) {
        extractAdminId(auth);

        Experience experience = experienceRepository.findById(experienceId)
                .orElseThrow(() -> new RuntimeException("Experience not found"));
        experience.setAvailable(request.available());
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

    public Map<String, Object> getExperienceById(Long experienceId) {
        Experience experience = experienceRepository.findById(experienceId)
                .orElseThrow(() -> new RuntimeException("Experience not found"));
        TravellerContactInfo contact = userServiceClient.getProviderContact(experience.getProviderId());
        return Map.of("experience", experience, "contact", contact);
    }

    public List<ExperienceResponseDTO> getAllExperiences() {
        return experienceRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    public Page<Experience> getAllExperiencesByAdmin(Authentication auth, int page, int limit, String filter) {
        extractAdminId(auth); // Ensure admin is authorized

        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.DESC, "createdAt"));

        if (filter == null || filter.equalsIgnoreCase("all")) {
            return experienceRepository.findAll(pageable);
        }

        if (filter.equalsIgnoreCase("available")) {
            return experienceRepository.findByAvailable(true, pageable);
        }

        if (filter.equalsIgnoreCase("unavailable")) {
            return experienceRepository.findByAvailable(false, pageable);
        }

        // Default fallback
        return experienceRepository.findAll(pageable);
    }


    public Page<ExperienceResponseDTO> getAvailableExperiences(int page, int limit) {
        // We create the PageRequest here
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Experience> experiencePage = experienceRepository.findByAvailableTrue(pageable);

        // .map() on a Page object keeps all the "extra" info (totalPages, first, last, etc.)
        return experiencePage.map(this::mapToResponse);
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

    public Page<ExperienceResponseDTO> getExperiencesByProvider(Long providerId, int page, int limit) {
        // page - 1 because Spring Data is 0-indexed internally
        Pageable pageable = PageRequest.of(page - 1, limit,
                Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Experience> experiencePage = experienceRepository.findByProviderId(providerId, pageable);

        // .map() on a Page object preserves all the pagination metadata you want
        return experiencePage.map(this::mapToResponse);
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
    private Long extractAdminId(Authentication auth) {
        Map<String, Object> principal = (Map<String, Object>) auth.getPrincipal();
        if (!(boolean) principal.getOrDefault("admin", false)) {
            throw new RuntimeException("Unauthorized: Only admins can manage listings.");
        }
        Map<String, Object> profile = (Map<String, Object>) principal.get("adminProfile");
        return ((Number) profile.get("adminId")).longValue();
    }
}
