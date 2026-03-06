public Double getAverageRating(Long transportId) {
    System.out.println("shakeef");
    System.out.println("shakeef");
    return reviewRepository.findAllByTransportId(transportId)
            .stream().mapToInt(Review::getRating).average().orElse(0.0);
}

