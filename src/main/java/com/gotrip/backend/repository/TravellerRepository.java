package com.gotrip.backend.repository;

import com.gotrip.backend.model.TravellerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TravellerRepository extends JpaRepository<TravellerProfile, Long> {
}
