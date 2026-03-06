package com.gotrip.review;

import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @PostMapping
    public String addReview(@RequestBody Review review) {
        return "Review added successfully";
    }

    @GetMapping("/hotel/{hotelId}")
    public List<Review> getReviewsByHotel(@PathVariable String hotelId) {

        return new ArrayList<>();
    }
}