package org.brapi.usecasechecker.exceptions;

public class UseCaseCheckerException extends Exception {
    public UseCaseCheckerException(String message) {
        super(message);
    }

    public UseCaseCheckerException(Exception e) {
        super(e);
    }
}
