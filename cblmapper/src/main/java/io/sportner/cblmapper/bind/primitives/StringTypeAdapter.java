package io.sportner.cblmapper.bind.primitives;

import com.couchbase.lite.Dictionary;
import com.couchbase.lite.Document;

/**
 * Created by alblanc on 19/08/2017.
 */
@Deprecated
public class StringTypeAdapter implements TypeAdapter<String> {

    @Override
    public void writeDocument(Document document, String fieldName, String value) {
        document.setString(fieldName, value);
    }

    @Override
    public void writeDocument(Dictionary dictionary, String fieldName, String value) {
        dictionary.setString(fieldName, value);
    }

    @Override
    public String readDocument(Document document, String fieldName) {
        return document.getString(fieldName);
    }

    @Override
    public String readDocument(Dictionary dictionary, String fieldName) {
        return dictionary.getString(fieldName);
    }
}
