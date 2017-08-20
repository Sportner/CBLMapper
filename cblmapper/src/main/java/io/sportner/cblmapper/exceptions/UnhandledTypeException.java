package io.sportner.cblmapper.exceptions;

/**
 * Created by alblanc on 19/08/2017.
 */

public class UnhandledTypeException extends CBLMapperClassException {

    String mMessage;

    public UnhandledTypeException(Class unhandledClass) {
        super(unhandledClass);
        mMessage = String.format("Class '%s' has no type adapter", unhandledClass.getName());
    }

    @Override
    public String getMessage() {
        return mMessage;
    }

}
