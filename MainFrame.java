package com.library.ui;

import com.library.dao.BookDAO;
import com.library.dao.StudentDAO;
import com.library.exception.LibraryException;
import com.library.model.Book;
import com.library.model.Loan;
import com.library.model.Student;
import com.library.service.LibraryService;
import com.library.util.FileReportExporter;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;

public class MainFrame extends JFrame {
    private final BookDAO bookDAO = new BookDAO();
    private final StudentDAO studentDAO = new StudentDAO();
    private final LibraryService libraryService = new LibraryService();

    private final DefaultTableModel booksModel = new DefaultTableModel(
            new String[]{"ID", "Title", "Author", "Total", "Available"}, 0);
    private final DefaultTableModel studentsModel = new DefaultTableModel(
            new String[]{"ID", "Name", "Email"}, 0);
    private final DefaultTableModel loansModel = new DefaultTableModel(
            new String[]{"Issue ID", "Book ID", "Student ID", "Book", "Student",
                    "Issue Date", "Due Date", "Return Date", "Status"}, 0);

    private final JTextField titleField = new JTextField(12);
    private final JTextField authorField = new JTextField(12);
    private final JTextField copiesField = new JTextField(5);
    private final JTextField bookIdField = new JTextField(5);
    private final JTextField searchField = new JTextField(12);

    private final JTextField studentNameField = new JTextField(12);
    private final JTextField studentEmailField = new JTextField(15);

    private final JTextField issueBookIdField = new JTextField(5);
    private final JTextField issueStudentIdField = new JTextField(5);
    private final JTextField returnIssueIdField = new JTextField(5);

    public MainFrame() {
        setTitle("Library Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1050, 650);
        setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Books", buildBooksPanel());
        tabs.addTab("Students", buildStudentsPanel());
        tabs.addTab("Issue / Return", buildIssuePanel());
        tabs.addTab("Transactions", buildTransactionsPanel());
        add(tabs);

        refreshAll();
    }

    private JPanel buildBooksPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT));
        form.add(new JLabel("Title:")); form.add(titleField);
        form.add(new JLabel("Author:")); form.add(authorField);
        form.add(new JLabel("Copies:")); form.add(copiesField);
        JButton add = new JButton("Add");
        JButton update = new JButton("Update by ID");
        JButton delete = new JButton("Delete by ID");
        form.add(add); form.add(new JLabel("Book ID:")); form.add(bookIdField);
        form.add(update); form.add(delete);

        JPanel search = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton find = new JButton("Search title/author");
        JButton showAll = new JButton("Show all");
        search.add(new JLabel("Search:")); search.add(searchField); search.add(find); search.add(showAll);

        JPanel top = new JPanel(new BorderLayout());
        top.add(form, BorderLayout.CENTER);
        top.add(search, BorderLayout.SOUTH);
        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(new JTable(booksModel)), BorderLayout.CENTER);

        add.addActionListener(e -> safeAction(() -> {
            String title = required(titleField.getText(), "Title");
            String author = required(authorField.getText(), "Author");
            int copies = positiveInt(copiesField.getText(), "Copies");
            bookDAO.addBook(title, author, copies);
            clearBookFields();
            refreshBooks();
            message("Book added successfully.");
        }));

        update.addActionListener(e -> safeAction(() -> {
            int id = positiveInt(bookIdField.getText(), "Book ID");
            String title = required(titleField.getText(), "Title");
            String author = required(authorField.getText(), "Author");
            int copies = positiveInt(copiesField.getText(), "Copies");
            bookDAO.updateBook(id, title, author, copies);
            refreshBooks();
            message("Book updated successfully.");
        }));

        delete.addActionListener(e -> safeAction(() -> {
            int id = positiveInt(bookIdField.getText(), "Book ID");
            bookDAO.deleteBook(id);
            refreshBooks();
            message("Book deleted successfully.");
        }));

        find.addActionListener(e -> safeAction(() -> showBooks(bookDAO.searchBooks(searchField.getText().trim()))));
        showAll.addActionListener(e -> safeAction(this::refreshBooks));
        return panel;
    }

    private JPanel buildStudentsPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT));
        form.add(new JLabel("Name:")); form.add(studentNameField);
        form.add(new JLabel("Email:")); form.add(studentEmailField);
        JButton add = new JButton("Register student");
        JButton refresh = new JButton("Refresh");
        form.add(add); form.add(refresh);
        panel.add(form, BorderLayout.NORTH);
        panel.add(new JScrollPane(new JTable(studentsModel)), BorderLayout.CENTER);

        add.addActionListener(e -> safeAction(() -> {
            String name = required(studentNameField.getText(), "Student name");
            String email = required(studentEmailField.getText(), "Email");
            if (!email.contains("@")) throw new IllegalArgumentException("Enter a valid email address.");
            studentDAO.addStudent(name, email);
            studentNameField.setText("");
            studentEmailField.setText("");
            refreshStudents();
            message("Student registered successfully.");
        }));
        refresh.addActionListener(e -> safeAction(this::refreshStudents));
        return panel;
    }

    private JPanel buildIssuePanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 10, 10));

        JPanel issue = new JPanel(new FlowLayout(FlowLayout.LEFT));
        issue.setBorder(BorderFactory.createTitledBorder("Issue a book (due in 14 days)"));
        issue.add(new JLabel("Book ID:")); issue.add(issueBookIdField);
        issue.add(new JLabel("Student ID:")); issue.add(issueStudentIdField);
        JButton issueButton = new JButton("Issue book");
        issue.add(issueButton);
        issueButton.addActionListener(e -> safeAction(() -> {
            int bookId = positiveInt(issueBookIdField.getText(), "Book ID");
            int studentId = positiveInt(issueStudentIdField.getText(), "Student ID");
            libraryService.issueBook(bookId, studentId);
            refreshAll();
            message("Book issued successfully.");
        }));

        JPanel returned = new JPanel(new FlowLayout(FlowLayout.LEFT));
        returned.setBorder(BorderFactory.createTitledBorder("Return a book"));
        returned.add(new JLabel("Issue ID:")); returned.add(returnIssueIdField);
        JButton returnButton = new JButton("Return book");
        returned.add(returnButton);
        returnButton.addActionListener(e -> safeAction(() -> {
            int issueId = positiveInt(returnIssueIdField.getText(), "Issue ID");
            libraryService.returnBook(issueId);
            refreshAll();
            message("Book returned successfully.");
        }));

        panel.add(issue); panel.add(returned);
        return panel;
    }

    private JPanel buildTransactionsPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        JButton refresh = new JButton("Refresh transactions");
        JButton export = new JButton("Export to text file");
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.add(refresh); buttons.add(export);
        panel.add(buttons, BorderLayout.NORTH);
        panel.add(new JScrollPane(new JTable(loansModel)), BorderLayout.CENTER);

        refresh.addActionListener(e -> safeAction(this::refreshLoans));
        export.addActionListener(e -> safeAction(() -> {
            Path path = FileReportExporter.exportLoans(libraryService.getAllLoans());
            message("Report exported to:\n" + path);
        }));
        return panel;
    }

    private void refreshAll() {
        safeAction(() -> {
            refreshBooks();
            refreshStudents();
            refreshLoans();
        });
    }

    private void refreshBooks() throws SQLException {
        showBooks(bookDAO.getAllBooks());
    }

    private void showBooks(List<Book> books) {
        booksModel.setRowCount(0);
        for (Book b : books) {
            booksModel.addRow(new Object[]{b.getBookId(), b.getTitle(), b.getAuthor(),
                    b.getTotalCopies(), b.getAvailableCopies()});
        }
    }

    private void refreshStudents() throws SQLException {
        studentsModel.setRowCount(0);
        for (Student s : studentDAO.getAllStudents()) {
            studentsModel.addRow(new Object[]{s.getId(), s.getName(), s.getEmail()});
        }
    }

    private void refreshLoans() throws SQLException {
        loansModel.setRowCount(0);
        for (Loan l : libraryService.getAllLoans()) {
            loansModel.addRow(new Object[]{l.getIssueId(), l.getBookId(), l.getStudentId(),
                    l.getBookTitle(), l.getStudentName(), l.getIssueDate(), l.getDueDate(),
                    l.getReturnDate() == null ? "-" : l.getReturnDate(), l.getStatus()});
        }
    }

    private int positiveInt(String text, String label) {
        try {
            int value = Integer.parseInt(text.trim());
            if (value <= 0) throw new NumberFormatException();
            return value;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(label + " must be a positive whole number.");
        }
    }

    private String required(String text, String label) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException(label + " cannot be empty.");
        }
        return text.trim();
    }

    @FunctionalInterface
    private interface ActionWithException {
        void run() throws Exception;
    }

    private void safeAction(ActionWithException action) {
        try {
            action.run();
        } catch (LibraryException | IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Notice", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Database error: " + ex.getMessage() +
                    "\nCheck that MySQL is running and DBConnection.java has the correct password.",
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "File error: " + ex.getMessage(),
                    "File Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Unexpected error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearBookFields() {
        titleField.setText("");
        authorField.setText("");
        copiesField.setText("");
    }

    private void message(String text) {
        JOptionPane.showMessageDialog(this, text);
    }
}
