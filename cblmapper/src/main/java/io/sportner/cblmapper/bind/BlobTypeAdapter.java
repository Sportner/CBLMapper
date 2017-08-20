package io.sportner.cblmapper.bind;

import com.couchbase.lite.Blob;
import com.couchbase.lite.Dictionary;
import com.couchbase.lite.Document;

/**
 * Created by alblanc on 20/08/2017.
 */

public class BlobTypeAdapter implements TypeAdapter<Blob> {

    @Override
    public void writeDocument(Document document, String fieldName, Blob value) {
        document.setBlob(fieldName, value);
    }

    @Override
    public void writeDocument(Dictionary dictionary, String fieldName, Blob value) {
        dictionary.setBlob(fieldName, value);
    }

    @Override
    public Blob readDocument(Document document, String fieldName) {
        return document.getBlob(fieldName);
    }

    @Override
    public Blob readDocument(Dictionary dictionary, String fieldName) {
        return dictionary.getBlob(fieldName);
    }
}
