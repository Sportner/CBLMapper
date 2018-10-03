package io.sportner.cblmapper.exceptions;

public class UnhandledTypeException extends CBLMapperClassException {
    String mMessage;

    public UnhandledTypeException(Class unhandledClass) {
        super(unhandledClass);
        this.mMessage = String.format("Class '%s' has no type adapter", unhandledClass.getName());
    }

    public String getMessage() {
        return this.mMessage;
    }
}
