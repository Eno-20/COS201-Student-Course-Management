import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.awt.GraphicsEnvironment;

/** Console entry point for the Student Course Management System. */
public class Main {
    private static final Path DATA_FILE = Paths.get("data", "courses.txt");

    public static void main(String[] args) {
        if ((args.length > 0 && "--console".equalsIgnoreCase(args[0]))
                || GraphicsEnvironment.isHeadless()) {
            runConsole();
            return;
        }
        CourseManagementGUI.launch();
    }

    /** Keeps the original console application available with the --console option. */
    private static void runConsole() {
        Scanner scanner = new Scanner(System.in);
        CourseManager courseManager = new CourseManager();

        System.out.println("========================================");
        System.out.println(" Student Course Management System");
        System.out.println("========================================");
        courseManager.loadCourses(DATA_FILE);

        boolean running = true;
        while (running) {
            displayMenu();
            String selection = prompt(scanner, "Enter your choice: ").trim();

            switch (selection) {
                case "1":
                    addCourse(scanner, courseManager);
                    break;
                case "2":
                    courseManager.displayCourses();
                    break;
                case "3":
                    searchCourse(scanner, courseManager);
                    break;
                case "4":
                    System.out.println("Total credit units: " + courseManager.computeTotalUnits());
                    break;
                case "5":
                    courseManager.saveCourses(DATA_FILE);
                    break;
                case "6":
                    courseManager.loadCourses(DATA_FILE);
                    break;
                case "7":
                    running = !exitProgram(scanner, courseManager);
                    break;
                default:
                    System.out.println("Invalid menu selection. Enter a number from 1 to 7.");
            }
            System.out.println();
        }

        scanner.close();
    }

    private static void displayMenu() {
        System.out.println("1. Add course");
        System.out.println("2. View all courses");
        System.out.println("3. Search course by code");
        System.out.println("4. Compute total units");
        System.out.println("5. Save courses to file");
        System.out.println("6. Load courses from file");
        System.out.println("7. Exit");
    }

    private static void addCourse(Scanner scanner, CourseManager courseManager) {
        String courseCode = promptForRequiredText(scanner, "Enter course code: ", "Course code");
        if (courseCode == null) {
            return;
        }
        String courseTitle = promptForRequiredText(scanner, "Enter course title: ", "Course title");
        if (courseTitle == null) {
            return;
        }

        int creditUnit = promptForCreditUnit(scanner);
        if (creditUnit == -1) {
            return;
        }

        try {
            Course course = new Course(courseCode, courseTitle, creditUnit);
            if (courseManager.addCourse(course)) {
                System.out.println("Course added successfully.");
            } else {
                System.out.println("A course with that code already exists.");
            }
        } catch (IllegalArgumentException exception) {
            System.out.println("Invalid course: " + exception.getMessage());
        }
    }

    private static String promptForRequiredText(Scanner scanner, String message, String fieldName) {
        while (true) {
            String value = prompt(scanner, message).trim().replaceAll("\\s+", " ");
            if (!value.isEmpty()) {
                return value;
            }
            System.out.println(fieldName + " cannot be empty. Please try again.");
        }
    }

    private static int promptForCreditUnit(Scanner scanner) {
        while (true) {
            String value = prompt(scanner, "Enter credit unit (positive integer): ").trim();
            try {
                int creditUnit = Integer.parseInt(value);
                if (creditUnit > 0) {
                    return creditUnit;
                }
                System.out.println("Credit unit must be greater than zero.");
            } catch (NumberFormatException exception) {
                System.out.println("Credit unit must be a valid whole number.");
            }
        }
    }

    private static void searchCourse(Scanner scanner, CourseManager courseManager) {
        String courseCode = promptForRequiredText(scanner, "Enter course code to search: ", "Course code");
        if (courseCode == null) {
            return;
        }

        Course course = courseManager.searchCourseByCode(courseCode);
        if (course == null) {
            System.out.println("No course found with code " + courseCode + ".");
        } else {
            System.out.println("Course found:");
            System.out.println("+-----------------+------------------------------------------+--------------+");
            System.out.printf("| %-15s | %-40s | %12s |%n", "Course Code", "Course Title", "Credit Unit");
            System.out.println("+-----------------+------------------------------------------+--------------+");
            course.displayInfo();
            System.out.println("+-----------------+------------------------------------------+--------------+");
        }
    }

    /** Returns true when the user confirms exiting. */
    private static boolean exitProgram(Scanner scanner, CourseManager courseManager) {
        if (!scanner.hasNextLine()) {
            System.out.println("No more input was provided; saving before exit.");
            courseManager.saveCourses(DATA_FILE);
            System.out.println("Exiting the application.");
            return true;
        }
        while (true) {
            String answer = prompt(scanner, "Save changes before exiting? (y/n/c to cancel): ")
                    .trim().toLowerCase();
            if (answer.equals("y") || answer.equals("yes")) {
                courseManager.saveCourses(DATA_FILE);
                System.out.println("Exiting the application.");
                return true;
            }
            if (answer.equals("n") || answer.equals("no")) {
                System.out.println("Exiting without saving new changes.");
                return true;
            }
            if (answer.equals("c") || answer.equals("cancel")) {
                return false;
            }
            System.out.println("Please enter y, n, or c.");
        }
    }

    private static String prompt(Scanner scanner, String message) {
        System.out.print(message);
        if (!scanner.hasNextLine()) {
            return "7";
        }
        return scanner.nextLine();
    }
}
