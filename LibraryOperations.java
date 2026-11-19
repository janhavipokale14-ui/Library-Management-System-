package com.library.service;

import com.library.exception.LibraryException;
import java.sql.SQLException;

public interface LibraryOperations {
    void issueBook(int bookId, int studentId) throws LibraryException, SQLException;
    void returnBook(int issueId) throws LibraryException, SQLException;
}
