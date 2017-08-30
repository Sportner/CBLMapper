package io.sportner.cblmapper.exceptions;

/**
 * Created by alblanc on 22/08/2017.
 */

public class NotCBLDocumentException extends CBLMapperClassException {

    String mMessage;

    public NotCBLDocumentException(Class unhandledClass) {
        super(unhandledClass);
        mMessage = String.format("Class '%s' is not a document. You muse use @CBLDocument annotation", unhandledClass.getName());
    }

    @Override
    public String getMessage() {
        return mMessage;
    }

}
