package io.sportner.cblmapper.Stream;

import android.support.annotation.Nullable;

import com.couchbase.lite.Document;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by alblanc on 19/08/2017.
 */
@Deprecated
public class DocumentWriter {

    Map<String, Object> mEntries;
    String mCurrentKey;
    String mDocumentID;

    public DocumentWriter(@Nullable String documentID) {
        mEntries = new HashMap<>();
        mDocumentID = documentID;
    }

    public void key(String key) {
        mCurrentKey = key;
    }

    public void value(Object value) {
        mEntries.put(mCurrentKey, value);
        mCurrentKey = null;
    }

    public Document writeDocument() {
        Document document = new Document(mDocumentID);

        document.set(mEntries);

        return document;
    }
}
