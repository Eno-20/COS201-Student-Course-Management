import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;

/** Swing desktop interface connected to the existing CourseManager. */
public class CourseManagementGUI extends JFrame {
    private static final long serialVersionUID = 1L;
    private static final Path DATA_FILE = Paths.get("data", "courses.txt");
    private static final Color NAVY = new Color(28, 43, 69);
    private static final Color BLUE = new Color(45, 105, 190);
    private static final Color PAGE_BACKGROUND = new Color(242, 245, 249);
    private static final Color TEXT_COLOR = new Color(43, 54, 70);
    private static final Color MUTED_COLOR = new Color(112, 124, 141);
    private static final Color BORDER_COLOR = new Color(224, 230, 238);

    private final CourseManager courseManager;
    private final Path dataFile;
    private final DefaultTableModel tableModel;
    private final JTable courseTable;
    private final JLabel courseCountValue;
    private final JLabel totalUnitsValue;
    private final JLabel totalUnitsSectionValue;
    private final JLabel statusLabel;
    private final JTextField courseCodeField;
    private final JTextField courseTitleField;
    private final JTextField courseUnitField;
    private final JTextField searchCodeField;
    private final JLabel searchResultLabel;
    private boolean hasUnsavedChanges;

    public CourseManagementGUI() {
        this(DATA_FILE);
    }

    /** Allows isolated testing with a temporary file; normal launches use data/courses.txt. */
    CourseManagementGUI(Path dataFile) {
        super("STUDENT COURSE MANAGEMENT SYSTEM");
        this.dataFile = dataFile;
        courseManager = new CourseManager();
        tableModel = new DefaultTableModel(new Object[] { "Course Code", "Course Title", "Course Units" }, 0) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        courseTable = new JTable(tableModel);
        courseCountValue = new JLabel("0");
        totalUnitsValue = new JLabel("0");
        totalUnitsSectionValue = new JLabel("0");
        statusLabel = new JLabel("Ready");
        courseCodeField = new JTextField();
        courseTitleField = new JTextField();
        courseUnitField = new JTextField();
        searchCodeField = new JTextField();
        searchResultLabel = new JLabel("Search results will appear here.");

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setMinimumSize(new Dimension(900, 650));
        setSize(1160, 760);
        setLocationRelativeTo(null);
        getContentPane().setBackground(PAGE_BACKGROUND);
        setLayout(new BorderLayout(0, 0));
        add(createHeader(), BorderLayout.NORTH);
        add(createMainContent(), BorderLayout.CENTER);
        add(createFooter(), BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                exitApplication();
            }
        });

        // Read persisted courses before the user starts working in the window.
        CourseManager.LoadResult loadResult = courseManager.loadCoursesWithResult(dataFile);
        refreshCourseTable();
        statusLabel.setText(loadResult.getMessage());
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout(20, 0));
        header.setBackground(NAVY);
        header.setBorder(BorderFactory.createEmptyBorder(24, 30, 24, 30));

        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new javax.swing.BoxLayout(heading, javax.swing.BoxLayout.Y_AXIS));
        JLabel title = new JLabel("STUDENT COURSE MANAGEMENT SYSTEM");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        JLabel subtitle = new JLabel("Manage your courses and credit units");
        subtitle.setForeground(new Color(202, 214, 231));
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setBorder(BorderFactory.createEmptyBorder(7, 0, 0, 0));
        heading.add(title);
        heading.add(subtitle);
        header.add(heading, BorderLayout.WEST);

        JPanel stats = new JPanel(new java.awt.GridLayout(1, 2, 12, 0));
        stats.setOpaque(false);
        stats.add(createHeaderStat("REGISTERED COURSES", courseCountValue));
        stats.add(createHeaderStat("TOTAL CREDIT UNITS", totalUnitsValue));
        header.add(stats, BorderLayout.EAST);
        return header;
    }

    private JPanel createHeaderStat(String caption, JLabel value) {
        JPanel card = new JPanel(new BorderLayout(0, 5));
        card.setBackground(new Color(43, 61, 91));
        card.setBorder(BorderFactory.createEmptyBorder(11, 16, 11, 16));
        card.setPreferredSize(new Dimension(155, 64));
        JLabel label = new JLabel(caption);
        label.setForeground(new Color(194, 207, 226));
        label.setFont(new Font("Segoe UI", Font.BOLD, 10));
        value.setForeground(Color.WHITE);
        value.setFont(new Font("Segoe UI", Font.BOLD, 22));
        card.add(label, BorderLayout.NORTH);
        card.add(value, BorderLayout.CENTER);
        return card;
    }

    private JPanel createMainContent() {
        JPanel content = new JPanel(new GridBagLayout());
        content.setBackground(PAGE_BACKGROUND);
        content.setBorder(BorderFactory.createEmptyBorder(22, 24, 16, 24));

        GridBagConstraints leftConstraints = new GridBagConstraints();
        leftConstraints.gridx = 0;
        leftConstraints.gridy = 0;
        leftConstraints.weightx = 0.36;
        leftConstraints.weighty = 1.0;
        leftConstraints.fill = GridBagConstraints.BOTH;
        leftConstraints.insets = new Insets(0, 0, 0, 16);
        content.add(createCourseForms(), leftConstraints);

        GridBagConstraints tableConstraints = new GridBagConstraints();
        tableConstraints.gridx = 1;
        tableConstraints.gridy = 0;
        tableConstraints.weightx = 0.64;
        tableConstraints.weighty = 1.0;
        tableConstraints.fill = GridBagConstraints.BOTH;
        content.add(createCourseTablePanel(), tableConstraints);
        return content;
    }

    private JPanel createCourseForms() {
        JPanel forms = new JPanel();
        forms.setOpaque(false);
        forms.setLayout(new javax.swing.BoxLayout(forms, javax.swing.BoxLayout.Y_AXIS));
        forms.add(createAddCoursePanel());
        forms.add(javax.swing.Box.createVerticalStrut(16));
        forms.add(createSearchPanel());
        forms.add(javax.swing.Box.createVerticalGlue());
        return forms;
    }

    private JPanel createAddCoursePanel() {
        JPanel card = createCardPanel();
        card.setLayout(new GridBagLayout());
        addSectionTitle(card, "Add Course", "Enter course details below.", 0);
        addFieldLabel(card, "Course Code", 1);
        addField(card, courseCodeField, 2);
        addFieldLabel(card, "Course Title", 3);
        addField(card, courseTitleField, 4);
        addFieldLabel(card, "Course Unit", 5);
        addField(card, courseUnitField, 6);

        JButton addButton = createButton("Add Course", BLUE, Color.WHITE);
        addButton.addActionListener(event -> addCourse());
        GridBagConstraints buttonConstraints = new GridBagConstraints();
        buttonConstraints.gridx = 0;
        buttonConstraints.gridy = 7;
        buttonConstraints.weightx = 1.0;
        buttonConstraints.fill = GridBagConstraints.HORIZONTAL;
        buttonConstraints.insets = new Insets(16, 0, 0, 0);
        card.add(addButton, buttonConstraints);
        return card;
    }

    private JPanel createSearchPanel() {
        JPanel card = createCardPanel();
        card.setLayout(new GridBagLayout());
        addSectionTitle(card, "Search Course", "Search by course code.", 0);
        addFieldLabel(card, "Course Code", 1);
        configureSearchField();

        JButton searchButton = createButton("Search Course", new Color(235, 241, 250), BLUE);
        searchButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(205, 220, 241)),
            BorderFactory.createEmptyBorder(9, 10, 9, 10)));
        searchButton.addActionListener(event -> searchCourse());
        searchCodeField.addActionListener(event -> searchCourse());

        JPanel searchRow = new JPanel(new BorderLayout(8, 0));
        searchRow.setOpaque(false);
        searchRow.add(searchCodeField, BorderLayout.CENTER);
        searchRow.add(searchButton, BorderLayout.EAST);
        GridBagConstraints buttonConstraints = new GridBagConstraints();
        buttonConstraints.gridx = 0;
        buttonConstraints.gridy = 2;
        buttonConstraints.weightx = 1.0;
        buttonConstraints.fill = GridBagConstraints.HORIZONTAL;
        buttonConstraints.insets = new Insets(0, 0, 0, 0);
        card.add(searchRow, buttonConstraints);

        searchResultLabel.setForeground(MUTED_COLOR);
        searchResultLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        searchResultLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        GridBagConstraints resultConstraints = new GridBagConstraints();
        resultConstraints.gridx = 0;
        resultConstraints.gridy = 3;
        resultConstraints.weightx = 1.0;
        resultConstraints.fill = GridBagConstraints.HORIZONTAL;
        resultConstraints.insets = new Insets(0, 0, 0, 0);
        card.add(searchResultLabel, resultConstraints);
        return card;
    }

    /** Styles the editable search input so it is easy to identify and type into. */
    private void configureSearchField() {
        searchCodeField.setEditable(true);
        searchCodeField.setEnabled(true);
        searchCodeField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchCodeField.setForeground(TEXT_COLOR);
        searchCodeField.setToolTipText("Type a course code, then click Search Course or press Enter.");
        searchCodeField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
    }

    private JPanel createCourseTablePanel() {
        JPanel card = createCardPanel();
        card.setLayout(new BorderLayout(0, 14));

        JPanel titleArea = new JPanel(new BorderLayout());
        titleArea.setOpaque(false);
        JLabel title = new JLabel("View Courses");
        title.setForeground(TEXT_COLOR);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        JLabel subtitle = new JLabel("All registered courses are shown below.");
        subtitle.setForeground(MUTED_COLOR);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new javax.swing.BoxLayout(text, javax.swing.BoxLayout.Y_AXIS));
        text.add(title);
        text.add(javax.swing.Box.createVerticalStrut(4));
        text.add(subtitle);
        titleArea.add(text, BorderLayout.WEST);
        card.add(titleArea, BorderLayout.NORTH);

        courseTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        courseTable.setForeground(TEXT_COLOR);
        courseTable.setRowHeight(32);
        courseTable.setShowVerticalLines(false);
        courseTable.setGridColor(new Color(235, 239, 245));
        courseTable.setSelectionBackground(new Color(222, 235, 252));
        courseTable.setSelectionForeground(TEXT_COLOR);
        courseTable.setFillsViewportHeight(true);
        courseTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        courseTable.getTableHeader().setForeground(TEXT_COLOR);
        courseTable.getTableHeader().setBackground(new Color(245, 247, 250));
        courseTable.setAutoCreateRowSorter(true);
        JScrollPane scrollPane = new JScrollPane(courseTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        card.add(scrollPane, BorderLayout.CENTER);
        return card;
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel(new BorderLayout(16, 0));
        footer.setBackground(Color.WHITE);
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR),
                BorderFactory.createEmptyBorder(13, 24, 13, 24)));

        JPanel summaryPanel = new JPanel(new BorderLayout(10, 0));
        summaryPanel.setOpaque(false);
        JLabel totalLabel = new JLabel("Total Units");
        totalLabel.setForeground(MUTED_COLOR);
        totalLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        totalUnitsSectionValue.setForeground(NAVY);
        totalUnitsSectionValue.setFont(new Font("Segoe UI", Font.BOLD, 18));
        summaryPanel.add(totalLabel, BorderLayout.WEST);
        summaryPanel.add(totalUnitsSectionValue, BorderLayout.CENTER);
        footer.add(summaryPanel, BorderLayout.WEST);

        statusLabel.setForeground(MUTED_COLOR);
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        footer.add(statusLabel, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 8, 0));
        buttons.setOpaque(false);
        JButton saveButton = createButton("Save Courses", BLUE, Color.WHITE);
        saveButton.addActionListener(event -> saveCourses());
        JButton loadButton = createButton("Load Courses", new Color(235, 241, 250), BLUE);
        loadButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(205, 220, 241)),
                BorderFactory.createEmptyBorder(9, 14, 9, 14)));
        loadButton.addActionListener(event -> loadCourses());
        JButton exitButton = createButton("Exit", new Color(249, 235, 235), new Color(165, 55, 55));
        exitButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(239, 211, 211)),
                BorderFactory.createEmptyBorder(9, 14, 9, 14)));
        exitButton.addActionListener(event -> exitApplication());
        buttons.add(saveButton);
        buttons.add(loadButton);
        buttons.add(exitButton);
        footer.add(buttons, BorderLayout.EAST);
        return footer;
    }

    private JPanel createCardPanel() {
        JPanel card = new JPanel();
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)));
        return card;
    }

    private void addSectionTitle(JPanel panel, String titleText, String subtitleText, int row) {
        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new javax.swing.BoxLayout(titlePanel, javax.swing.BoxLayout.Y_AXIS));
        JLabel title = new JLabel(titleText);
        title.setForeground(TEXT_COLOR);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        JLabel subtitle = new JLabel(subtitleText);
        subtitle.setForeground(MUTED_COLOR);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
        titlePanel.add(title);
        titlePanel.add(subtitle);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = row;
        constraints.weightx = 1.0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(0, 0, 13, 0);
        panel.add(titlePanel, constraints);
    }

    private void addFieldLabel(JPanel panel, String text, int row) {
        JLabel label = new JLabel(text);
        label.setForeground(TEXT_COLOR);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = row;
        constraints.weightx = 1.0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(3, 0, 5, 0);
        panel.add(label, constraints);
    }

    private void addField(JPanel panel, JTextField field, int row) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setForeground(TEXT_COLOR);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = row;
        constraints.weightx = 1.0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(0, 0, 8, 0);
        panel.add(field, constraints);
    }

    private JButton createButton(String text, Color background, Color foreground) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(foreground);
        button.setBackground(background);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        button.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        return button;
    }

    private void addCourse() {
        String courseCode = courseCodeField.getText().trim();
        String courseTitle = courseTitleField.getText().trim();
        String unitText = courseUnitField.getText().trim();

        if (courseCode.isEmpty() || courseTitle.isEmpty() || unitText.isEmpty()) {
            showMessage("Course code, course title, and course unit are all required.",
                    "Missing Course Information", JOptionPane.WARNING_MESSAGE);
            return;
        }

        final int creditUnit;
        try {
            creditUnit = Integer.parseInt(unitText);
            if (creditUnit <= 0) {
                showMessage("Course units must be a positive whole number.",
                        "Invalid Course Unit", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (NumberFormatException exception) {
            showMessage("Course units must be entered as a positive whole number.",
                    "Invalid Course Unit", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Course course = new Course(courseCode, courseTitle, creditUnit);
            if (!courseManager.addCourse(course)) {
                showMessage("A course with code " + course.getCourseCode() + " already exists.",
                        "Duplicate Course", JOptionPane.WARNING_MESSAGE);
                return;
            }
            courseCodeField.setText("");
            courseTitleField.setText("");
            courseUnitField.setText("");
            hasUnsavedChanges = true;
            refreshCourseTable();
            statusLabel.setText("Added " + course.getCourseCode() + ". Remember to save your changes.");
            showMessage("Course " + course.getCourseCode() + " was added successfully.",
                    "Course Added", JOptionPane.INFORMATION_MESSAGE);
        } catch (IllegalArgumentException exception) {
            showMessage(exception.getMessage(), "Invalid Course", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void searchCourse() {
        String code = searchCodeField.getText().trim();
        if (code.isEmpty()) {
            showMessage("Enter a course code to search.", "Course Code Required", JOptionPane.WARNING_MESSAGE);
            searchCodeField.requestFocusInWindow();
            return;
        }

        // CourseManager performs the required case-insensitive recursive search.
        Course course = courseManager.searchCourseByCode(code);
        if (course == null) {
            searchResultLabel.setForeground(new Color(165, 55, 55));
            searchResultLabel.setText("No course found for code: " + code.trim());
            showMessage("No course was found with code " + code.trim() + ".",
                    "Course Not Found", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        searchResultLabel.setForeground(new Color(43, 125, 83));
        searchResultLabel.setText("Found: " + course.getCourseCode() + " — " + course.getCourseTitle());
        for (int modelRow = 0; modelRow < tableModel.getRowCount(); modelRow++) {
            if (tableModel.getValueAt(modelRow, 0).equals(course.getCourseCode())) {
                int viewRow = courseTable.convertRowIndexToView(modelRow);
                courseTable.setRowSelectionInterval(viewRow, viewRow);
                courseTable.scrollRectToVisible(courseTable.getCellRect(viewRow, 0, true));
                break;
            }
        }
        showMessage("Course found:\nCode: " + course.getCourseCode()
                + "\nTitle: " + course.getCourseTitle()
                + "\nUnits: " + course.getCreditUnit(), "Course Found", JOptionPane.INFORMATION_MESSAGE);
    }

    private void saveCourses() {
        try {
            CourseManager.SaveResult result = courseManager.saveCoursesWithResult(dataFile);
            statusLabel.setText(result.getMessage());
            showMessage(result.getMessage(), result.isSuccessful() ? "Save Complete" : "Save Failed",
                    result.isSuccessful() ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
            if (result.isSuccessful()) {
                hasUnsavedChanges = false;
            }
        } catch (RuntimeException exception) {
            showMessage("Could not save courses: " + exception.getMessage(), "Save Failed",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadCourses() {
        try {
            CourseManager.LoadResult result = courseManager.loadCoursesWithResult(dataFile);
            refreshCourseTable();
            statusLabel.setText(result.getMessage());
            StringBuilder message = new StringBuilder(result.getMessage());
            ArrayList<String> details = result.getDetails();
            for (String detail : details) {
                message.append("\n").append(detail);
            }
            int messageType = result.hasError() ? JOptionPane.ERROR_MESSAGE
                    : result.getSkippedCount() > 0 ? JOptionPane.WARNING_MESSAGE : JOptionPane.INFORMATION_MESSAGE;
            showMessage(message.toString(), result.hasError() ? "Load Failed" : "Load Complete", messageType);
        } catch (RuntimeException exception) {
            showMessage("Could not load courses: " + exception.getMessage(), "Load Failed",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshCourseTable() {
        tableModel.setRowCount(0);
        for (Course course : courseManager.getCourses()) {
            tableModel.addRow(new Object[] {
                    course.getCourseCode(), course.getCourseTitle(), course.getCreditUnit()
            });
        }
        int courseCount = tableModel.getRowCount();
        int totalUnits = courseManager.computeTotalUnits();
        courseCountValue.setText(Integer.toString(courseCount));
        totalUnitsValue.setText(Integer.toString(totalUnits));
        totalUnitsSectionValue.setText(Integer.toString(totalUnits));
    }

    private void exitApplication() {
        String confirmation = hasUnsavedChanges
            ? "You have unsaved changes. Would you like to save your courses before exiting?"
            : "Would you like to save your course list before exiting?";
        int choice = JOptionPane.showConfirmDialog(this,
            confirmation,
                "Exit Student Course Management System",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (choice == JOptionPane.CANCEL_OPTION || choice == JOptionPane.CLOSED_OPTION) {
            return;
        }
        if (choice == JOptionPane.YES_OPTION) {
            CourseManager.SaveResult result = courseManager.saveCoursesWithResult(dataFile);
            if (!result.isSuccessful()) {
                showMessage(result.getMessage(), "Save Failed", JOptionPane.ERROR_MESSAGE);
                return;
            }
            statusLabel.setText(result.getMessage());
        }
        dispose();
    }

    private void showMessage(String message, String title, int messageType) {
        JOptionPane.showMessageDialog(this, message, title, messageType);
    }

    /** Sets the application's look and feel and opens the window on the EDT. */
    public static void launch() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception exception) {
            // Swing's default look and feel is still functional if the system one is unavailable.
        }
        javax.swing.SwingUtilities.invokeLater(() -> new CourseManagementGUI().setVisible(true));
    }
}
