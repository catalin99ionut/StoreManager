package com.myproject.storemanager.exception;

public class ProductCreateException extends RuntimeException {

    public ProductCreateException(String message) {
        super(message);
    }

    public ProductCreateException(String message, Throwable cause) {
        super(message, cause);
    }
}
