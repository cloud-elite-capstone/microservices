package com.retasify.orderservice.exception;

public class InvalidOrderUpdateException extends RuntimeException {

    public InvalidOrderUpdateException(String message) {
        super(message);
    }
}
