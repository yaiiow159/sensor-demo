package com.example.aquarkdemo.exception;

public class CaculateException extends RuntimeException{

    public CaculateException(String message) {
        super(message);
    }

    public CaculateException(String message, Throwable cause) {
        super(message, cause);
    }
}
