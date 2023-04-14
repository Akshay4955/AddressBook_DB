package com.bridgelabz;

public class AddressBookCustomException extends RuntimeException {
    enum ExceptionType {
        INVALID_INPUT_FORMAT, SQL_EXCEPTION
    }

    ExceptionType type;

    public AddressBookCustomException(ExceptionType type, String message) {
        super(message);
        this.type = type;
    }
}
