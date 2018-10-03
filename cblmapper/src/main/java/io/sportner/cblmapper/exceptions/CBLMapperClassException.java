package io.sportner.cblmapper.exceptions;

public class CBLMapperClassException extends RuntimeException {
    Class mUnhandledClass;
    String mMessage;

    public CBLMapperClassException(Class unhandledClass) {
        this.mUnhandledClass = unhandledClass;
    }

    public CBLMapperClassException(Class unhandledClass, String message) {
        this(unhandledClass);
        this.mMessage = message;
    }

    public String toString() {
        return String.format("CBLMapperClassException for class '%s': %s", this.mUnhandledClass, this.mMessage);
    }

    public Class getExceptionClass() {
        return this.mUnhandledClass;
    }
}
