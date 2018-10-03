package io.sportner.cblmapper.exceptions;

public class UnsupportedIDFieldTypeException extends CBLMapperClassException {
    public UnsupportedIDFieldTypeException(Class unhandledClass) {
        super(unhandledClass);
    }

    public String getMessage() {
        return String.format("'%s' must use a String as ID field", this.getExceptionClass());
    }
}
