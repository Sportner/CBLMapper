package io.sportner.cblmapper.exceptions;

/**
 * Created by alblanc on 19/08/2017.
 */

public class UnsupportedIDFieldTypeException extends CBLMapperClassException {

    public UnsupportedIDFieldTypeException(Class unhandledClass) {
        super(unhandledClass);
    }

    @Override
    public String getMessage() {
        return String.format("'%s' must use a String as ID field", getExceptionClass());
    }
}
