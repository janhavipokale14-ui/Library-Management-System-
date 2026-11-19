package com.library.model;

// Abstraction: common user information is defined here, while subclasses provide their role.
public abstract class User {
    private int id;
    private String name;

    public User(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public abstract String displayRole();
}
