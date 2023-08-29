package com.sPOSify.POSpotify.errorHandling.customExceptions;

public class EnumException extends RuntimeException {

    public EnumException(String value, String enumType) {
        super(String.format("%s unknown! Please try again with one of these values: %s", enumType, value));
    }
}
