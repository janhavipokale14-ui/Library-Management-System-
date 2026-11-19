package com.library.model;

public class Student extends User {
    private String email;

    public Student(int id, String name, String email) {
        super(id, name);
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String displayRole() {
        return "Student";
    }
}
