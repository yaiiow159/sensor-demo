package com.example.aquarkdemo.exception;

public class ThresHoldException extends RuntimeException {

    public ThresHoldException(String message) {
        super(message);
    }

    public ThresHoldException(String message, Throwable cause) {
        super(message, cause);
    }
}
