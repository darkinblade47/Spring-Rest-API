package com.MONGOtify.LeMongo.exceptions;

public class AccessDenied extends RuntimeException {
    public AccessDenied(String ex) {
        super(ex);
    }
}
