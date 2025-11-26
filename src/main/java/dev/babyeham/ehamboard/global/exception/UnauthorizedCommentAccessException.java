package dev.babyeham.ehamboard.global.exception;

public class UnauthorizedCommentAccessException extends RuntimeException {
    public UnauthorizedCommentAccessException(String message) {
        super(message);
    }
}

