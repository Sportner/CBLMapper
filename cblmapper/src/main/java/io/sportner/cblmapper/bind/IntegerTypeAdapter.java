package io.sportner.cblmapper.bind;

import com.couchbase.lite.Dictionary;
import com.couchbase.lite.Document;

/**
 * Created by alblanc on 19/08/2017.
 */

public class IntegerTypeAdapter implements TypeAdapter<Integer> {

    @Override
    public void writeDocument(Document document, String fieldName, Integer value) {
        document.setInt(fieldName, value);
    }

    @Override
    public void writeDocument(Dictionary dictionary, String fieldName, Integer value) {
        dictionary.setInt(fieldName, value);
    }

    @Override
    public Integer readDocument(Document document, String fieldName) {
        return document.getInt(fieldName);
    }

    @Override
    public Integer readDocument(Dictionary dictionary, String fieldName) {
        return dictionary.getInt(fieldName);
    }
}
