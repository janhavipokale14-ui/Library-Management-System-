package com.library.dao;

import com.library.model.Loan;
import com.library.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LoanDAO {
    public List<Loan> getAllLoans() throws SQLException {
        List<Loan> loans = new ArrayList<>();
        String sql = "SELECT l.issue_id, l.book_id, l.student_id, b.title AS book_title, " +
                "s.name AS student_name, l.issue_date, l.due_date, l.return_date, l.status " +
                "FROM loans l JOIN books b ON l.book_id=b.book_id " +
                "JOIN students s ON l.student_id=s.student_id ORDER BY l.issue_id DESC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                loans.add(new Loan(rs.getInt("issue_id"), rs.getInt("book_id"),
                        rs.getInt("student_id"), rs.getString("book_title"),
                        rs.getString("student_name"), rs.getDate("issue_date"),
                        rs.getDate("due_date"), rs.getDate("return_date"),
                        rs.getString("status")));
            }
        }
        return loans;
    }

    public List<String> getOverdueDescriptions() throws SQLException {
        List<String> rows = new ArrayList<>();
        String sql = "SELECT l.issue_id, b.title, s.name, l.due_date " +
                "FROM loans l JOIN books b ON l.book_id=b.book_id " +
                "JOIN students s ON l.student_id=s.student_id " +
                "WHERE l.status='ISSUED' AND l.due_date < CURDATE()";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                rows.add("Issue ID " + rs.getInt("issue_id") + ": \"" +
                        rs.getString("title") + "\" borrowed by " + rs.getString("name") +
                        ", due " + rs.getDate("due_date"));
            }
        }
        return rows;
    }
}
