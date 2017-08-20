package io.sportner.cblmapper.bind;

import com.couchbase.lite.Dictionary;
import com.couchbase.lite.Document;

/**
 * Created by alblanc on 20/08/2017.
 */

public class LongTypeAdapter implements TypeAdapter<Long> {

    @Override
    public void writeDocument(Document document, String fieldName, Long value) {
        document.setLong(fieldName, value);
    }

    @Override
    public void writeDocument(Dictionary dictionary, String fieldName, Long value) {
        dictionary.setLong(fieldName, value);
    }

    @Override
    public Long readDocument(Document document, String fieldName) {
        return document.getLong(fieldName);
    }

    @Override
    public Long readDocument(Dictionary dictionary, String fieldName) {
        return dictionary.getLong(fieldName);
    }
}
