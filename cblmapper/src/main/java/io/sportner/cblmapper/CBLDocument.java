package io.sportner.cblmapper;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;

import io.sportner.cblmapper.exceptions.CBLMapperClassException;

/**
 * Created by alblanc on 15/09/2017.
 */

public class CBLDocument {

    private Document mDocument;

    public CBLDocument() {}

    public void save(Database database) throws CouchbaseLiteException, CBLMapperClassException {
        save(database, new CBLMapper());
    }

    public void save(Database database, CBLMapper cblMapper) throws CBLMapperClassException, CouchbaseLiteException {
        database.save(cblMapper.toDocument(this));
    }

    public Document getDocument() {
        return mDocument;
    }

    public void setDocument(Document document) {
        mDocument = document;
    }
}
