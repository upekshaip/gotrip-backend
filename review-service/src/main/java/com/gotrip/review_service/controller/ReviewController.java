package com.gotrip.review_service.controller;

import com.gotrip.review_service.Review;
import com.gotrip.review_service.ReviewRepository;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewRepository repository;

    public ReviewController(ReviewRepository repository) {
        this.repository = repository;
    }

    // CREATE: Save a new review
    @PostMapping
    public Review saveReview(@RequestBody Review review) {
        return repository.save(review);
    }

    // READ: Get all reviews in the system
    @GetMapping
    public List<Review> getAllReviews() {
        return repository.findAll();
    }

    // READ: Get reviews for a specific transport (e.g., /api/reviews/transport/101)
    @GetMapping("/transport/{id}")
    public List<Review> getReviewsByTransport(@PathVariable Long id) {
        return repository.findByTransportId(id);
    }

    // DELETE: Remove a review by its ID
    @DeleteMapping("/{id}")
    public void deleteReview(@PathVariable Long id) {
        repository.deleteById(id);
    }
}