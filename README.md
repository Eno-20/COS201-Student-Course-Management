# Student Course Management System (Mini-System)

A beginner-friendly Java application for adding, viewing, searching, and totaling courses. It opens a Swing desktop interface by default and retains the original console menu as an option. Courses are kept in an `ArrayList<Course>` and saved as UTF-8 tab-separated records in `data/courses.txt`.

## Requirements

- Java Development Kit (JDK) 17 or later
- Visual Studio Code with the Java Extension Pack (optional, but useful)

## Compile and run

Open a terminal in the `COS201_Student_Course_Management` project folder, then compile and run the Swing application:

```powershell
javac -d out src\*.java
java -cp out Main
```

The application reads and writes `data/courses.txt` relative to the project folder. Run it from that folder so it uses the intended data file. Saved courses are loaded when the window opens. If the file does not exist, the application continues with an empty list.

To run the original console interface instead, use `java -cp out Main --console`.

## Swing interface

The window provides course entry and validation, a non-editable course table, case-insensitive recursive search, course/unit summaries, Save Courses, Load Courses, and Exit controls. Exit asks whether to save first.

Course codes are stored uppercase, whitespace is normalized, credit units must be positive integers, and duplicate codes are rejected. File records contain the course code, title, and credit unit separated by tabs. Invalid records and duplicates encountered during loading are reported and skipped.
