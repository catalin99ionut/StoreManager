package com.myproject.storemanager.exception;

public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(Long id) {
        super("Product with ID " + id + " was not found");
    }

    public ProductNotFoundException(String message) {
        super(message);
    }
}
