package com.gotrip.transport_service.service;

import com.gotrip.common_library.dto.transport_service.TransportCreateRequest;
import com.gotrip.common_library.dto.transport_service.enums.TransportStatus;
import com.gotrip.transport_service.model.Transport;
import com.gotrip.transport_service.repository.TransportRepository;
import com.gotrip.transport_service.repository.TransportReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    public Page<Transport> getAllActive(int page, int limit) {
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by("createdAt").descending());
        return transportRepository.findByStatus(TransportStatus.ACTIVE, pageable);
    }

    public Transport getById(Long id) {
        return transportRepository.findById(id)
                .filter(t -> t.getStatus() != TransportStatus.REMOVED)
                .orElseThrow(() -> new RuntimeException("Transport vehicle not found or has been removed."));
    }

    public Page<Transport> searchTransports(String city, int page, int limit) {
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by("createdAt").descending());
        return transportRepository.findByCityIgnoreCaseAndStatus(city, TransportStatus.ACTIVE, pageable);
    }

    public List<Transport> searchTransports(String city) {
        return transportRepository.findByCityIgnoreCaseAndStatus(city,
                com.gotrip.common_library.dto.transport_service.enums.TransportStatus.ACTIVE);
    }

    public Page<Transport> getMyAll(TransportStatus status, int page, int limit, Authentication auth) {
        Long providerId = extractProviderId(auth);
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by("createdAt").descending());

        if (status != null) {
            return transportRepository.findByProviderIdAndStatusAndStatusNot(
                    providerId, status, TransportStatus.REMOVED, pageable);
        }

        return transportRepository.findByProviderIdAndStatusNot(
                providerId, TransportStatus.REMOVED, pageable);
    }

    public Page<Transport> getAllTransportsByAdmin(Authentication authentication, int page, int limit) {
        verifyAdmin(authentication);
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by("createdAt").descending());
        return transportRepository.findAll(pageable);
    }

    public Page<Transport> getPendingTransportsByAdmin(Authentication authentication, int page, int limit) {
        verifyAdmin(authentication);
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by("createdAt").descending());
        return transportRepository.findByStatus(TransportStatus.PENDING, pageable);
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

    @Transactional
    public Transport approveTransport(Long id, Authentication authentication) {
        verifyAdmin(authentication);
        Transport transport = getById(id);
        transport.setStatus(TransportStatus.ACTIVE);
        return transportRepository.save(transport);
    }

    public long countAll() {
        return transportRepository.count();
    }

    public long countActive() {
        return transportRepository.countByStatus(TransportStatus.ACTIVE);
    }

    private void validateOwnership(Transport transport, Authentication auth) {
        if (!transport.getProviderId().equals(extractProviderId(auth))) {
            throw new RuntimeException("Access Denied: Ownership verification failed.");
        }
    }

    private void verifyAdmin(Authentication authentication) {
        Map<String, Object> principal = (Map<String, Object>) authentication.getPrincipal();
        if (!(boolean) principal.getOrDefault("admin", false)) {
            throw new RuntimeException("Unauthorized: Only admins can perform this action.");
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
        transport.setImageUrl(req.imageUrl());
        transport.setFeatured(req.isFeatured());
    }
}