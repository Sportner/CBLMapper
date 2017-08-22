package io.sportner.cblmapper.bind.primitives;

import com.couchbase.lite.Dictionary;
import com.couchbase.lite.Document;

/**
 * Created by alblanc on 19/08/2017.
 */
@Deprecated
public interface TypeAdapter<T> {

    void writeDocument(Document document, String fieldName, T value);

    void writeDocument(Dictionary dictionary, String fieldName, T value);

    T readDocument(Document document, String fieldName);

    T readDocument(Dictionary dictionary, String fieldName);
}
