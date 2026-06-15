package com.smartwealth.smartwealth_backend.exception.goal;

public class GoalNotFoundException extends RuntimeException {
    public GoalNotFoundException(String message) {
        super(message);
    }
}
