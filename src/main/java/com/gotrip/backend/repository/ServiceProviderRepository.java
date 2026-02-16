package com.gotrip.backend.repository;

import com.gotrip.backend.model.ServiceProviderProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceProviderRepository extends JpaRepository<ServiceProviderProfile, Long> {
}
