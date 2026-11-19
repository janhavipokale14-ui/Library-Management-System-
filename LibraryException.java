package com.library.exception;

// A custom checked exception gives library-rule errors a meaningful type.
public class LibraryException extends Exception {
    public LibraryException(String message) {
        super(message);
    }
}
