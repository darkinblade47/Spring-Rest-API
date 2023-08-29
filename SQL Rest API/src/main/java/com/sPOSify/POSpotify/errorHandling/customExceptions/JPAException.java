package com.sPOSify.POSpotify.errorHandling.customExceptions;

public class JPAException extends RuntimeException {

    public JPAException(String text) {
        super(text);
    }
}
