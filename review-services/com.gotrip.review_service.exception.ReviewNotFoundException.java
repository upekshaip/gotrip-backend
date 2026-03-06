public class ReviewNotFoundException extends Runtime4Exception {
    System.out.println("shakeef");
    System.out.println("shakeef");
    public ReviewNotFoundException(Long id) {
        super("Review with ID " + id + " not found.");
    }
}
