package io.sportner.cblmapper;

import android.support.annotation.Nullable;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Document;

import java.lang.reflect.Field;

import io.sportner.cblmapper.annotations.DocumentField;
import io.sportner.cblmapper.exceptions.UnsupportedIDFieldTypeException;
import io.sportner.cblmapper.util.FieldHelper;

/**
 * Created by alblanc on 15/09/2017.
 */

public class CBLDocument {

    private transient Document mDocument;
    private transient CBLMapper mCBLMapper;
    private transient String mDocumentID;

    public CBLDocument() { }

    public CBLDocument(@Nullable String documentID) {
        init(documentID, null);
    }

    public CBLDocument(@Nullable CBLMapper cblMapper) {
        init(null, cblMapper);
    }

    public CBLDocument(@Nullable String documentID, @Nullable CBLMapper cblMapper) {
        init(documentID, cblMapper);
    }

    private void init(@Nullable String documentID, @Nullable CBLMapper cblMapper) {
        mCBLMapper = cblMapper;
        setDocumentID(documentID);
    }

    public Document getDocument() {
        return mDocument;
    }

    public void setDocument(Document document) {
        setDocumentID(document.getId());
        mDocument = document;
    }

    public void setCBLMapper(CBLMapper mapper) {
        mCBLMapper = mapper;
    }

    public CBLMapper getCBLMapper() {
        return mCBLMapper;
    }

    public void setDocumentID(String documentID) {
        mDocumentID = documentID;
        mDocument = null;
        // Inject Document ID into CBLDocument attributes with annotation @DocumentField(ID=true)
        for (Field field : FieldHelper.getFieldsUpTo(this.getClass(), Object.class)) {
            DocumentField documentFieldAnnotation = field.getAnnotation(DocumentField.class);
            if (documentFieldAnnotation != null && documentFieldAnnotation.ID()) {
                if (field.getType() == String.class) {
                    try {
                        boolean isPrivate = !field.isAccessible();
                        if (isPrivate) {
                            field.setAccessible(true);
                        }
                        field.set(this, documentID);
                        if (isPrivate) {
                            field.setAccessible(false);
                        }
                    } catch (IllegalAccessException e) {
                        // Ignore as it can't happen
                    }
                    break;
                } else {
                    throw new UnsupportedIDFieldTypeException(this.getClass());
                }
            }
        }
    }

    public String getDocumentID() {
        return mDocumentID;
    }

    public void delete() throws CouchbaseLiteException {
        if (mCBLMapper == null || mCBLMapper.getDatabase() == null) {
            throw new IllegalStateException("You must attach a mapper with an opened database connection to this document");
        }
        if (mDocument == null) {
            mDocument = getCBLMapper().getDatabase().getDocument(mDocumentID);
        }

        if (mDocument != null) {
            getCBLMapper().getDatabase().delete(mDocument);
        }
    }

    public void load() {
        getCBLMapper().load(this);
    }

    public void save() throws CouchbaseLiteException {
        save(true);
    }

    public void save(boolean saveChildren) throws CouchbaseLiteException {
        if (mCBLMapper == null || mCBLMapper.getDatabase() == null) {
            throw new IllegalStateException("You must attach a mapper with a opened database connection to this document");
        }

        // TODO: Traverse through children to save them before saving root instance (eg: this)

        mCBLMapper.save(this);
    }
}
