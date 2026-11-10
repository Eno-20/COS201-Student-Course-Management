import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Locale;

/** Manages the in-memory course collection and its file storage. */
public class CourseManager {
    private final ArrayList<Course> courses = new ArrayList<>();

    /** Adds a course unless another course already has the same code. */
    public boolean addCourse(Course course) {
        if (course == null || findCourseByCode(course.getCourseCode()) != null) {
            return false;
        }
        courses.add(course);
        return true;
    }

    /** Returns a copy so callers can view courses without changing the collection. */
    public ArrayList<Course> getCourses() {
        return new ArrayList<>(courses);
    }

    /** Displays every course in a formatted table. */
    public void displayCourses() {
        if (courses.isEmpty()) {
            System.out.println("No courses are currently registered.");
            return;
        }

        System.out.println("+-----------------+------------------------------------------+--------------+");
        System.out.printf("| %-15s | %-40s | %12s |%n", "Course Code", "Course Title", "Credit Unit");
        System.out.println("+-----------------+------------------------------------------+--------------+");
        for (Course course : courses) {
            course.displayInfo();
        }
        System.out.println("+-----------------+------------------------------------------+--------------+");
    }

    /** Starts a recursive search at the beginning of the collection. */
    public Course searchCourseByCode(String courseCode) {
        String normalizedCode = courseCode == null
                ? ""
                : courseCode.trim().replaceAll("\\s+", "").toUpperCase(Locale.ROOT);
        return searchCourseByCode(normalizedCode, 0);
    }

    /** Recursively checks one course at each index until a match or the end. */
    private Course searchCourseByCode(String courseCode, int index) {
        if (index >= courses.size()) {
            return null;
        }
        Course currentCourse = courses.get(index);
        if (currentCourse.getCourseCode().equals(courseCode)) {
            return currentCourse;
        }
        return searchCourseByCode(courseCode, index + 1);
    }

    private Course findCourseByCode(String courseCode) {
        return searchCourseByCode(courseCode);
    }

    /** Computes the sum of all registered credit units. */
    public int computeTotalUnits() {
        int total = 0;
        for (Course course : courses) {
            total += course.getCreditUnit();
        }
        return total;
    }

    /** Saves courses as tab-separated UTF-8 records. */
    public boolean saveCourses(Path filePath) {
        SaveResult result = saveCoursesWithResult(filePath);
        System.out.println(result.getMessage());
        return result.isSuccessful();
    }

    /** Saves courses and returns a message that can be shown by a graphical interface. */
    public SaveResult saveCoursesWithResult(Path filePath) {
        try {
            Path parent = filePath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
                for (Course course : courses) {
                    writer.write(course.getCourseCode());
                    writer.write('\t');
                    writer.write(course.getCourseTitle());
                    writer.write('\t');
                    writer.write(Integer.toString(course.getCreditUnit()));
                    writer.newLine();
                }
            }
            return new SaveResult(true, "Saved " + courses.size() + " course(s) to " + filePath + ".");
        } catch (IOException exception) {
            return new SaveResult(false, "Could not save courses: " + exception.getMessage());
        }
    }

    /** Loads valid records and prints a summary for the console application. */
    public void loadCourses(Path filePath) {
        LoadResult result = loadCoursesWithResult(filePath);
        System.out.println(result.getMessage());
        for (String detail : result.getDetails()) {
            System.out.println(detail);
        }
    }

    /** Loads valid records and returns a summary for a graphical interface. */
    public LoadResult loadCoursesWithResult(Path filePath) {
        int loaded = 0;
        int skipped = 0;
        int nonEmptyRecords = 0;
        ArrayList<String> details = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            String line;
            int recordNumber = 0;
            while ((line = reader.readLine()) != null) {
                recordNumber++;
                if (line.trim().isEmpty()) {
                    continue;
                }
                nonEmptyRecords++;

                String[] fields = line.split("\\t", -1);
                if (fields.length != 3) {
                    skipped++;
                    details.add("Skipped invalid record on line " + recordNumber + ".");
                    continue;
                }

                try {
                    int creditUnit = Integer.parseInt(fields[2].trim());
                    Course course = new Course(fields[0], fields[1], creditUnit);
                    if (addCourse(course)) {
                        loaded++;
                    } else {
                        skipped++;
                        details.add("Skipped duplicate course code on line " + recordNumber + ".");
                    }
                } catch (IllegalArgumentException exception) {
                    skipped++;
                    details.add("Skipped invalid record on line " + recordNumber + ": "
                            + exception.getMessage());
                }
            }

            if (nonEmptyRecords == 0) {
                return new LoadResult(loaded, skipped, "The course file is empty.", details, false);
            }
            return new LoadResult(loaded, skipped,
                    "Loaded " + loaded + " course(s); skipped " + skipped + " invalid or duplicate record(s).",
                    details, false);
        } catch (NoSuchFileException exception) {
            return new LoadResult(loaded, skipped, "No saved course file was found at " + filePath + ".",
                    details, true);
        } catch (IOException exception) {
            return new LoadResult(loaded, skipped, "Could not load courses: " + exception.getMessage(),
                    details, true);
        }
    }

    /** Result of a save operation. */
    public static class SaveResult {
        private final boolean successful;
        private final String message;

        private SaveResult(boolean successful, String message) {
            this.successful = successful;
            this.message = message;
        }

        public boolean isSuccessful() {
            return successful;
        }

        public String getMessage() {
            return message;
        }
    }

    /** Result of a load operation, including any skipped-record explanations. */
    public static class LoadResult {
        private final int loadedCount;
        private final int skippedCount;
        private final String message;
        private final ArrayList<String> details;
        private final boolean error;

        private LoadResult(int loadedCount, int skippedCount, String message,
                ArrayList<String> details, boolean error) {
            this.loadedCount = loadedCount;
            this.skippedCount = skippedCount;
            this.message = message;
            this.details = new ArrayList<>(details);
            this.error = error;
        }

        public int getLoadedCount() {
            return loadedCount;
        }

        public int getSkippedCount() {
            return skippedCount;
        }

        public String getMessage() {
            return message;
        }

        public ArrayList<String> getDetails() {
            return new ArrayList<>(details);
        }

        public boolean hasError() {
            return error;
        }
    }
}
