package io.sportner.cblmapper.bind;

import com.couchbase.lite.Dictionary;
import com.couchbase.lite.Document;

import java.util.Date;

/**
 * Created by alblanc on 20/08/2017.
 */

public class DateTypeAdapter implements TypeAdapter<Date> {

    @Override
    public void writeDocument(Document document, String fieldName, Date value) {
        document.setDate(fieldName, value);
    }

    @Override
    public void writeDocument(Dictionary dictionary, String fieldName, Date value) {
        dictionary.setDate(fieldName, value);
    }

    @Override
    public Date readDocument(Document document, String fieldName) {
        return document.getDate(fieldName);
    }

    @Override
    public Date readDocument(Dictionary dictionary, String fieldName) {
        return dictionary.getDate(fieldName);
    }
}
