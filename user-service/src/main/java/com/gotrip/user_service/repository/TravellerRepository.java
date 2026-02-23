package com.gotrip.user_service.repository;


import com.gotrip.user_service.model.TravellerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TravellerRepository extends JpaRepository<TravellerProfile, Long> {
}
