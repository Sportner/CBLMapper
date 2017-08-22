package io.sportner.cblmapper.bind.primitives;

import com.couchbase.lite.Dictionary;
import com.couchbase.lite.Document;

/**
 * Created by alblanc on 20/08/2017.
 */
@Deprecated
public class DoubleTypeAdapter implements TypeAdapter<Double> {

    @Override
    public void writeDocument(Document document, String fieldName, Double value) {
        document.setDouble(fieldName, value);
    }

    @Override
    public void writeDocument(Dictionary dictionary, String fieldName, Double value) {
        dictionary.setDouble(fieldName, value);
    }

    @Override
    public Double readDocument(Document document, String fieldName) {
        return document.getDouble(fieldName);
    }

    @Override
    public Double readDocument(Dictionary dictionary, String fieldName) {
        return dictionary.getDouble(fieldName);
    }
}
