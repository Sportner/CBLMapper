package io.sportner.cblmapper.exceptions;

/**
 * Created by alblanc on 19/08/2017.
 */

public class CBLMapperClassException extends Exception {

    Class mUnhandledClass;

    public CBLMapperClassException(Class unhandledClass) {
        mUnhandledClass = unhandledClass;
    }

    public Class getExceptionClass() {
        return mUnhandledClass;
    }
}
