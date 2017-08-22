package io.sportner.cblmapper.bind.primitives;

import com.couchbase.lite.Dictionary;
import com.couchbase.lite.Document;

/**
 * Created by alblanc on 20/08/2017.
 */
@Deprecated
public class FloatTypeAdapter implements TypeAdapter<Float> {

    @Override
    public void writeDocument(Document document, String fieldName, Float value) {
        document.setFloat(fieldName, value);
    }

    @Override
    public void writeDocument(Dictionary dictionary, String fieldName, Float value) {
        dictionary.setFloat(fieldName, value);
    }

    @Override
    public Float readDocument(Document document, String fieldName) {
        return document.getFloat(fieldName);
    }

    @Override
    public Float readDocument(Dictionary dictionary, String fieldName) {
        return dictionary.getFloat(fieldName);
    }
}
