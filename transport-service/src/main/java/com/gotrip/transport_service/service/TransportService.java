package com.gotrip.transport_service.service;

import com.gotrip.common_library.dto.transport_service.TransportCreateRequest;
import com.gotrip.common_library.dto.transport_service.enums.TransportStatus;
import com.gotrip.transport_service.model.Transport;
import com.gotrip.transport_service.repository.TransportRepository;
import com.gotrip.transport_service.repository.TransportReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TransportService {

    private final TransportRepository transportRepository;
    private final TransportReviewRepository reviewRepository;

    @Transactional
    public Transport createTransport(TransportCreateRequest request, Authentication auth) {
        Long providerId = extractProviderId(auth);
        Transport transport = new Transport();
        mapDtoToEntity(request, transport);
        transport.setProviderId(providerId);
        transport.setStatus(TransportStatus.PENDING);
        return transportRepository.save(transport);
    }

    public List<Transport> getAllActive() {
        return transportRepository.findAll().stream()
                .filter(t -> t.getStatus() == TransportStatus.ACTIVE)
                .toList();
    }

    public Transport getById(Long id) {
        return transportRepository.findById(id)
                .filter(t -> t.getStatus() != TransportStatus.REMOVED)
                .orElseThrow(() -> new RuntimeException("Transport vehicle not found or has been removed."));
    }

    @Transactional
    public Transport update(Long id, TransportCreateRequest request, Authentication auth) {
        Transport transport = getById(id);
        validateOwnership(transport, auth);
        mapDtoToEntity(request, transport);
        return transportRepository.save(transport);
    }

    @Transactional
    public void delete(Long id, Authentication auth) {
        Transport transport = getById(id);
        validateOwnership(transport, auth);

        // Soft Delete the Transport
        transport.setStatus(TransportStatus.REMOVED);
        transportRepository.save(transport);

        // Hard Delete all associated reviews
        reviewRepository.deleteAllByTransport_TransportId(id);
    }

    private void validateOwnership(Transport transport, Authentication auth) {
        if (!transport.getProviderId().equals(extractProviderId(auth))) {
            throw new RuntimeException("Access Denied: Ownership verification failed.");
        }
    }

    private Long extractProviderId(Authentication auth) {
        Map<String, Object> principal = (Map<String, Object>) auth.getPrincipal();
        if (!(boolean) principal.getOrDefault("serviceProvider", false)) {
            throw new RuntimeException("Unauthorized: Only Service Providers can manage listings.");
        }
        Map<String, Object> profile = (Map<String, Object>) principal.get("serviceProviderProfile");
        return ((Number) profile.get("providerId")).longValue();
    }

    private void mapDtoToEntity(TransportCreateRequest req, Transport transport) {
        transport.setVehicleMake(req.vehicleMake());
        transport.setVehicleModel(req.vehicleModel());
        transport.setVehicleType(req.vehicleType());
        transport.setDescription(req.description());
        transport.setCity(req.city());
        transport.setPriceUnit(req.priceUnit());
        transport.setPrice(req.price());
        transport.setCapacity(req.capacity());
        transport.setLatitude(req.latitude());
        transport.setLongitude(req.longitude());
        transport.setImageUrl(req.imageUrl());
        transport.setFeatured(req.isFeatured());
    }
}