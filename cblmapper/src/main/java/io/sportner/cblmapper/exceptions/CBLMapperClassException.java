package io.sportner.cblmapper.exceptions;

/**
 * Created by alblanc on 19/08/2017.
 */

public class CBLMapperClassException extends RuntimeException {

    Class mUnhandledClass;
    String mMessage;

    public CBLMapperClassException(Class unhandledClass) {
        mUnhandledClass = unhandledClass;
    }

    public CBLMapperClassException(Class unhandledClass, String message) {
        this(unhandledClass);
        mMessage = message;
    }

    @Override
    public String toString() {
        return String.format("CBLMapperClassException for class '%s': %s", mUnhandledClass, mMessage);
    }

    public Class getExceptionClass() {
        return mUnhandledClass;
    }
}
