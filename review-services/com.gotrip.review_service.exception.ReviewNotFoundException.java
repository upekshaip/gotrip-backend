public class ReviewNotFoundException extends Runtime4Exception {
    public ReviewNotFoundException(Long id) {
        super("Review with ID " + id + " not found.");
    }
}
