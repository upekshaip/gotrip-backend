package com.gotrip.restaurant_service.repository;

import com.gotrip.restaurant_service.model.RestaurantReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Repository
public interface RestaurantReviewRepository extends JpaRepository<RestaurantReview, Long> {

    // Get all reviews for a specific restaurant (ordered by newest first)
    List<RestaurantReview> findByRestaurant_RestaurantIdOrderByCreatedAtDesc(Long restaurantId);

    // Check if a traveler has already reviewed a specific booking
    boolean existsByBookingId(Long bookingId);

    // Calculate the average rating for a restaurant to update the Restaurant listing
    @Query("SELECT AVG(r.rating) FROM RestaurantReview r WHERE r.restaurant.restaurantId = :restaurantId")
    Double getAverageRatingForRestaurant(@Param("restaurantId") Long restaurantId);

    /**
     * FIX: Added for the Restaurant Service's delete logic.
     * Use @Modifying because this is a DML (Delete) operation.
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM RestaurantReview r WHERE r.restaurant.restaurantId = :restaurantId")
    void deleteAllByRestaurant_RestaurantId(@Param("restaurantId") Long restaurantId);
}