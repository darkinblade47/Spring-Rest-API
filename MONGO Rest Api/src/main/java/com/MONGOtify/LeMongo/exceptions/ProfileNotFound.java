package com.MONGOtify.LeMongo.exceptions;

public class ProfileNotFound extends RuntimeException {
    public ProfileNotFound(String text) {
        super(text);
    }

}
