package dev.shiftsad.core.exceptions;

public class ModuleDependencyException extends RuntimeException {
    public ModuleDependencyException(String message) {
        super(message);
    }

    public ModuleDependencyException(String message, Throwable cause) {
        super(message, cause);
    }
}