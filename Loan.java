package com.library.model;

import java.sql.Date;

public class Loan {
    private int issueId;
    private int bookId;
    private int studentId;
    private String bookTitle;
    private String studentName;
    private Date issueDate;
    private Date dueDate;
    private Date returnDate;
    private String status;

    public Loan(int issueId, int bookId, int studentId, String bookTitle, String studentName,
                Date issueDate, Date dueDate, Date returnDate, String status) {
        this.issueId = issueId;
        this.bookId = bookId;
        this.studentId = studentId;
        this.bookTitle = bookTitle;
        this.studentName = studentName;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.returnDate = returnDate;
        this.status = status;
    }

    public int getIssueId() { return issueId; }
    public int getBookId() { return bookId; }
    public int getStudentId() { return studentId; }
    public String getBookTitle() { return bookTitle; }
    public String getStudentName() { return studentName; }
    public Date getIssueDate() { return issueDate; }
    public Date getDueDate() { return dueDate; }
    public Date getReturnDate() { return returnDate; }
    public String getStatus() { return status; }
}
