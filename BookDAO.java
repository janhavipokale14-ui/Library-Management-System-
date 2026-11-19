package com.library.dao;

import com.library.model.Book;
import com.library.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookDAO {
    public void addBook(String title, String author, int totalCopies) throws SQLException {
        String sql = "INSERT INTO books(title, author, total_copies, available_copies) VALUES(?,?,?,?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setString(2, author);
            ps.setInt(3, totalCopies);
            ps.setInt(4, totalCopies);
            ps.executeUpdate();
        }
    }

    public List<Book> getAllBooks() throws SQLException {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT * FROM books ORDER BY book_id";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                books.add(new Book(
                        rs.getInt("book_id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getInt("total_copies"),
                        rs.getInt("available_copies")));
            }
        }
        return books;
    }

    public List<Book> searchBooks(String keyword) throws SQLException {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT * FROM books WHERE title LIKE ? OR author LIKE ? ORDER BY book_id";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            String search = "%" + keyword + "%";
            ps.setString(1, search);
            ps.setString(2, search);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    books.add(new Book(rs.getInt("book_id"), rs.getString("title"),
                            rs.getString("author"), rs.getInt("total_copies"),
                            rs.getInt("available_copies")));
                }
            }
        }
        return books;
    }

    public void updateBook(int id, String title, String author, int totalCopies) throws SQLException {
        String sql = "UPDATE books SET title=?, author=?, " +
                     "available_copies = available_copies + (? - total_copies), total_copies=? " +
                     "WHERE book_id=? AND available_copies + (? - total_copies) >= 0";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setString(2, author);
            ps.setInt(3, totalCopies);
            ps.setInt(4, totalCopies);
            ps.setInt(5, id);
            ps.setInt(6, totalCopies);
            int changed = ps.executeUpdate();
            if (changed == 0) {
                throw new SQLException("Book not found or total copies cannot be less than copies already issued.");
            }
        }
    }

    public void deleteBook(int id) throws SQLException {
        String sql = "DELETE FROM books WHERE book_id=? AND available_copies=total_copies";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            if (ps.executeUpdate() == 0) {
                throw new SQLException("Book not found or some copies are currently issued.");
            }
        }
    }
}
