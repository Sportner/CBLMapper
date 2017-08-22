package io.sportner.cblmapper.bind.primitives;

import com.couchbase.lite.Dictionary;
import com.couchbase.lite.Document;

/**
 * Created by alblanc on 20/08/2017.
 */
@Deprecated
public class BooleanTypeAdapter implements TypeAdapter<Boolean> {

    @Override
    public void writeDocument(Document document, String fieldName, Boolean value) {
        document.setBoolean(fieldName, value);
    }

    @Override
    public void writeDocument(Dictionary dictionary, String fieldName, Boolean value) {
        dictionary.setBoolean(fieldName, value);
    }

    @Override
    public Boolean readDocument(Document document, String fieldName) {
        return document.getBoolean(fieldName);
    }

    @Override
    public Boolean readDocument(Dictionary dictionary, String fieldName) {
        return dictionary.getBoolean(fieldName);
    }
}
