package com.minimonk.user.auth;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail validation(MethodArgumentNotValidException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Validation failed");
        problem.setDetail(ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .findFirst()
                .orElse("Request body is invalid"));
        return problem;
    }

    @ExceptionHandler(ResponseStatusException.class)
    ProblemDetail responseStatus(ResponseStatusException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(ex.getStatusCode());
        problem.setTitle(ex.getReason());
        problem.setDetail(ex.getReason());
        return problem;
    }
}
