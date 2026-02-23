package com.gotrip.user_service.repository;

import com.gotrip.user_service.model.ServiceProviderProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceProviderRepository extends JpaRepository<ServiceProviderProfile, Long> {
}
