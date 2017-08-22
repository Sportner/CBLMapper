package io.sportner.cblmapper.bind.lists;

import com.couchbase.lite.Dictionary;
import com.couchbase.lite.Document;

import io.sportner.cblmapper.CBLMapper;

/**
 * Created by alblanc on 21/08/2017.
 */
@Deprecated
public interface ListTypeAdapter<T> {

    void writeDocument(CBLMapper mapper, Document document, String fieldName, T value);

    void writeDocument(CBLMapper mapper, Dictionary dictionary, String fieldName, T value);
}
