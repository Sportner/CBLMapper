package io.sportner.cblmapper.bind.primitives;

import com.couchbase.lite.Dictionary;
import com.couchbase.lite.Document;

/**
 * Created by alblanc on 20/08/2017.
 */
@Deprecated
public class NumberTypeAdapter implements TypeAdapter<Number> {

    @Override
    public void writeDocument(Document document, String fieldName, Number value) {
        document.setNumber(fieldName, value);
    }

    @Override
    public void writeDocument(Dictionary dictionary, String fieldName, Number value) {
        dictionary.setNumber(fieldName, value);
    }

    @Override
    public Number readDocument(Document document, String fieldName) {
        return document.getNumber(fieldName);
    }

    @Override
    public Number readDocument(Dictionary dictionary, String fieldName) {
        return dictionary.getNumber(fieldName);
    }
}
