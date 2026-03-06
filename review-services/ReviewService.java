@Transactional(readOnly = true)
public Map<String, Object> getReviewAnalytics(Long transportId) {
    log.info("Generating detailed analytics report for transport ID: {}", transportId);

    // In a real app, this would come from a repository call
    List<ReviewDTO> reviews = getMockReviews(transportId);

    double average = reviews.stream()
            .mapToInt(ReviewDTO::rating)
            .average()
            .orElse(0.0);

    long highRatings = reviews.stream()
            .filter(r -> r.rating() >= 4)
            .count();

    Map<String, Object> stats = new LinkedHashMap<>();
    stats.put("transportI", transportId);
    stats.put("averageRating", Math.round(average * 100.0) / 100.0);
    stats.put("totalReviews", reviews.size());
    stats.put("positiveFeedbackPercentage", (reviews.isEmpty()) ? 0 : (highRatings * 100 / reviews.size()));

    return stats;
}