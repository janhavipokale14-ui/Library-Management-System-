package com.library.service;

import com.library.dao.LoanDAO;
import com.library.exception.LibraryException;
import com.library.util.DBConnection;
import java.sql.*;
import java.util.List;

public class LibraryService implements LibraryOperations {
    private final LoanDAO loanDAO = new LoanDAO();

    // synchronized makes issue operations mutually exclusive inside this running app instance.
    @Override
    public synchronized void issueBook(int bookId, int studentId)
            throws LibraryException, SQLException {
        String checkStudent = "SELECT student_id FROM students WHERE student_id=?";
        String lockBook = "SELECT available_copies FROM books WHERE book_id=? FOR UPDATE";
        String updateBook = "UPDATE books SET available_copies=available_copies-1 WHERE book_id=?";
        String insertLoan = "INSERT INTO loans(book_id, student_id, issue_date, due_date, status) " +
                "VALUES(?,?,CURDATE(),DATE_ADD(CURDATE(), INTERVAL 14 DAY),'ISSUED')";

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(checkStudent)) {
                    ps.setInt(1, studentId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new LibraryException("Student ID does not exist.");
                        }
                    }
                }

                try (PreparedStatement ps = con.prepareStatement(lockBook)) {
                    ps.setInt(1, bookId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new LibraryException("Book ID does not exist.");
                        }
                        if (rs.getInt("available_copies") <= 0) {
                            throw new LibraryException("No copies of this book are available.");
                        }
                    }
                }

                try (PreparedStatement ps = con.prepareStatement(updateBook)) {
                    ps.setInt(1, bookId);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement(insertLoan)) {
                    ps.setInt(1, bookId);
                    ps.setInt(2, studentId);
                    ps.executeUpdate();
                }
                con.commit();
            } catch (Exception ex) {
                con.rollback();
                if (ex instanceof LibraryException) {
                    throw (LibraryException) ex;
                }
                if (ex instanceof SQLException) {
                    throw (SQLException) ex;
                }
                throw new SQLException("Could not issue book.", ex);
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    @Override
    public synchronized void returnBook(int issueId)
            throws LibraryException, SQLException {
        String findLoan = "SELECT book_id, status FROM loans WHERE issue_id=? FOR UPDATE";
        String markReturned = "UPDATE loans SET status='RETURNED', return_date=CURDATE() WHERE issue_id=?";
        String increaseCopies = "UPDATE books SET available_copies=available_copies+1 WHERE book_id=?";

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                int bookId;
                try (PreparedStatement ps = con.prepareStatement(findLoan)) {
                    ps.setInt(1, issueId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new LibraryException("Issue ID does not exist.");
                        }
                        if (!"ISSUED".equals(rs.getString("status"))) {
                            throw new LibraryException("This book has already been returned.");
                        }
                        bookId = rs.getInt("book_id");
                    }
                }
                try (PreparedStatement ps = con.prepareStatement(markReturned)) {
                    ps.setInt(1, issueId);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement(increaseCopies)) {
                    ps.setInt(1, bookId);
                    ps.executeUpdate();
                }
                con.commit();
            } catch (Exception ex) {
                con.rollback();
                if (ex instanceof LibraryException) {
                    throw (LibraryException) ex;
                }
                if (ex instanceof SQLException) {
                    throw (SQLException) ex;
                }
                throw new SQLException("Could not return book.", ex);
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    public List<com.library.model.Loan> getAllLoans() throws SQLException {
        return loanDAO.getAllLoans();
    }
}
