package com.gotrip.transport_service.repository;

import com.gotrip.common_library.dto.transport_service.enums.TransportStatus;
import com.gotrip.transport_service.model.Transport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransportRepository extends JpaRepository<Transport, Long> {

    // Find all transports that are NOT removed (for general browsing)
    List<Transport> findByStatusNot(TransportStatus status);

    // For the provider's dashboard to see their own vehicles
    List<Transport> findByProviderId(Long providerId);

    // Find a specific transport by ID but only if it's not removed
    Optional<Transport> findByTransportIdAndStatusNot(Long transportId, TransportStatus status);

    // Find transports for a specific city that are ACTIVE
    List<Transport> findByCityIgnoreCaseAndStatus(String city, TransportStatus status);

    // Find all transports owned by a specific provider
    List<Transport> findByProviderIdAndStatusNot(Long providerId, TransportStatus status);

    // Find featured transports that are active
    List<Transport> findByIsFeaturedTrueAndStatus(TransportStatus status);

    long countByStatus(TransportStatus status);
}