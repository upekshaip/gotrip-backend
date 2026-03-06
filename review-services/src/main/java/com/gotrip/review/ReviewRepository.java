package com.gotrip.review;

import java.util.List;

public interface ReviewRepository {
    Review save(Review review);
    List<Review> findByHotelId(String hotelId);
    List<Review> findByUserId(String userId);
}