package com.gotrip.review_service;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // Custom method to filter reviews by a specific transport/bus/train
    List<Review> findByTransportId(Long transportId);
}