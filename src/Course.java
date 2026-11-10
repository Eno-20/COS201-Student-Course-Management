import java.util.Locale;

/**
 * Represents one course in the student course management system.
 */
public class Course {
    private final String courseCode;
    private final String courseTitle;
    private final int creditUnit;

    /** Creates a course after normalizing and validating its values. */
    public Course(String courseCode, String courseTitle, int creditUnit) {
        if (courseCode == null) {
            throw new IllegalArgumentException("Course code cannot be empty.");
        }
        if (courseTitle == null) {
            throw new IllegalArgumentException("Course title cannot be empty.");
        }

        String normalizedCode = courseCode.trim().replaceAll("\\s+", "").toUpperCase(Locale.ROOT);
        String normalizedTitle = courseTitle.trim().replaceAll("\\s+", " ");

        if (normalizedCode.isEmpty()) {
            throw new IllegalArgumentException("Course code cannot be empty.");
        }
        if (normalizedTitle.isEmpty()) {
            throw new IllegalArgumentException("Course title cannot be empty.");
        }
        if (creditUnit <= 0) {
            throw new IllegalArgumentException("Credit unit must be a positive integer.");
        }

        this.courseCode = normalizedCode;
        this.courseTitle = normalizedTitle;
        this.creditUnit = creditUnit;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public String getCourseTitle() {
        return courseTitle;
    }

    public int getCreditUnit() {
        return creditUnit;
    }

    /** Prints this course as one row in the course table. */
    public void displayInfo() {
        System.out.printf("| %-15s | %-40s | %12d |%n", courseCode, courseTitle, creditUnit);
    }
}
