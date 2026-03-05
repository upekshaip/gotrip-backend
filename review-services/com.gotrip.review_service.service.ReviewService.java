public Double getAverageRating(Long transportId) {
    return reviewRepository.findAllByTransportId(transportId)
            .stream().mapToInt(Review::getRating).average().orElse(0.0);
}

