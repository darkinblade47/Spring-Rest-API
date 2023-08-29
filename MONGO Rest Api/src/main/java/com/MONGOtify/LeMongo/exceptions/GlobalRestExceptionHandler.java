package com.MONGOtify.LeMongo.exceptions;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Component
@ControllerAdvice
public class GlobalRestExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = {PlaylistNotFound.class, ProfileNotFound.class, SongsAlbumsException.class})
    public ResponseEntity<Object> handleNotFound(RuntimeException ex, WebRequest request) {

        String notFound;
        if (ex.getClass().getSimpleName().equals("PlaylistNotFound"))
            notFound = "playlist?";
        else if (ex.getClass().getSimpleName().equals("ProfileNotFound"))
            notFound = "profile?";
        else
            notFound = "song?";

        ApiErrorResponse apiResponse = new ApiErrorResponse
                .ApiErrorResponseBuilder()
                .withDetail(ex.getMessage())
                .withMessage("Where " + notFound)
                .withError_code("404")
                .withStatus(HttpStatus.NOT_FOUND)
                .atTime(LocalDateTime.now(ZoneOffset.UTC))
                .build();
        return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = {UnprocessableEntityException.class})
    public ResponseEntity<Object> handleBadParameter(UnprocessableEntityException ex, WebRequest request) {
        ApiErrorResponse apiResponse = new ApiErrorResponse
                .ApiErrorResponseBuilder()
                .withDetail(ex.getMessage())
                .withMessage("Bad parameter")
                .withError_code("422")
                .withStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                .atTime(LocalDateTime.now(ZoneOffset.UTC))
                .build();
        return new ResponseEntity<>(apiResponse, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(value = {ProfileDuplicate.class})
    public ResponseEntity<Object> handleDuplicateConflicts(ProfileDuplicate ex, WebRequest request) {
        ApiErrorResponse apiResponse = new ApiErrorResponse
                .ApiErrorResponseBuilder()
                .withDetail(ex.getMessage())
                .withMessage("Conflict")
                .withError_code("409")
                .withStatus(HttpStatus.CONFLICT)
                .atTime(LocalDateTime.now(ZoneOffset.UTC))
                .build();
        return new ResponseEntity<>(apiResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(value = {UnauthorizedException.class})
    public ResponseEntity<Object> handleNotAuthorized(UnauthorizedException ex, WebRequest request) {
        ApiErrorResponse apiResponse = new ApiErrorResponse
                .ApiErrorResponseBuilder()
                .withDetail(ex.getMessage())
                .withMessage("Unauthorized")
                .withError_code("401")
                .withStatus(HttpStatus.UNAUTHORIZED)
                .atTime(LocalDateTime.now(ZoneOffset.UTC))
                .build();
        return new ResponseEntity<>(apiResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(value = {ForbiddenException.class})
    public ResponseEntity<Object> handleForbidden(ForbiddenException ex, WebRequest request) {
        ApiErrorResponse apiResponse = new ApiErrorResponse
                .ApiErrorResponseBuilder()
                .withDetail(ex.getMessage())
                .withMessage("Forbidden")
                .withError_code("403")
                .withStatus(HttpStatus.FORBIDDEN)
                .atTime(LocalDateTime.now(ZoneOffset.UTC))
                .build();
        return new ResponseEntity<>(apiResponse, HttpStatus.FORBIDDEN);
    }


}