package com.library.util;

import com.library.model.Loan;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class FileReportExporter {
    private FileReportExporter() {}

    public static Path exportLoans(List<Loan> loans) throws IOException {
        Path file = Path.of("library_transactions.txt");
        try (BufferedWriter writer = Files.newBufferedWriter(file)) {
            writer.write("LIBRARY MANAGEMENT SYSTEM - TRANSACTION REPORT");
            writer.newLine();
            writer.write("==============================================");
            writer.newLine();
            for (Loan loan : loans) {
                writer.write("Issue ID: " + loan.getIssueId()
                        + " | Book: " + loan.getBookTitle()
                        + " | Student: " + loan.getStudentName()
                        + " | Issue date: " + loan.getIssueDate()
                        + " | Due date: " + loan.getDueDate()
                        + " | Return date: " + loan.getReturnDate()
                        + " | Status: " + loan.getStatus());
                writer.newLine();
            }
        }
        return file.toAbsolutePath();
    }
}
