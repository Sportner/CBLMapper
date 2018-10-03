package io.sportner.cblmapper;

import android.support.annotation.Nullable;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Document;
import io.sportner.cblmapper.annotations.DocumentField;
import io.sportner.cblmapper.exceptions.UnsupportedIDFieldTypeException;
import io.sportner.cblmapper.util.FieldHelper;
import java.lang.reflect.Field;
import java.util.Iterator;

public class CBLDocument {
    private transient Document mDocument;
    private transient CBLMapper mCBLMapper;
    private transient String mDocumentID;

    public CBLDocument() {
    }

    public CBLDocument(@Nullable String documentID) {
        this.init(documentID, (CBLMapper)null);
    }

    public CBLDocument(@Nullable CBLMapper cblMapper) {
        this.init((String)null, cblMapper);
    }

    public CBLDocument(@Nullable String documentID, @Nullable CBLMapper cblMapper) {
        this.init(documentID, cblMapper);
    }

    private void init(@Nullable String documentID, @Nullable CBLMapper cblMapper) {
        this.mCBLMapper = cblMapper;
        this.setDocumentID(documentID);
    }

    public Document getDocument() {
        return this.mDocument;
    }

    public void setDocument(Document document) {
        this.setDocumentID(document.getId());
        this.mDocument = document;
    }

    public void setCBLMapper(CBLMapper mapper) {
        this.mCBLMapper = mapper;
    }

    public CBLMapper getCBLMapper() {
        return this.mCBLMapper;
    }

    public void setDocumentID(String documentID) {
        this.mDocumentID = documentID;
        this.mDocument = null;
        Iterator var2 = FieldHelper.getFieldsUpTo(this.getClass(), Object.class).iterator();

        while(var2.hasNext()) {
            Field field = (Field)var2.next();
            DocumentField documentFieldAnnotation = (DocumentField)field.getAnnotation(DocumentField.class);
            if (documentFieldAnnotation != null && documentFieldAnnotation.ID()) {
                if (field.getType() != String.class) {
                    throw new UnsupportedIDFieldTypeException(this.getClass());
                }

                try {
                    boolean isPrivate = !field.isAccessible();
                    if (isPrivate) {
                        field.setAccessible(true);
                    }

                    field.set(this, documentID);
                    if (isPrivate) {
                        field.setAccessible(false);
                    }
                } catch (IllegalAccessException var6) {
                    ;
                }
                break;
            }
        }

    }

    public String getDocumentID() {
        return this.mDocumentID;
    }

    public void delete() throws CouchbaseLiteException {
        if (this.mCBLMapper != null && this.mCBLMapper.getDatabase() != null) {
            if (this.mDocument == null) {
                this.mDocument = this.getCBLMapper().getDatabase().getDocument(this.mDocumentID);
            }

            if (this.mDocument != null) {
                this.getCBLMapper().getDatabase().delete(this.mDocument);
            }

        } else {
            throw new IllegalStateException("You must attach a mapper with an opened database connection to this document");
        }
    }

    public void load() {
        this.getCBLMapper().load(this);
    }

    public void save() throws CouchbaseLiteException {
        this.save(true);
    }

    public void save(boolean saveChildren) throws CouchbaseLiteException {
        if (this.mCBLMapper != null && this.mCBLMapper.getDatabase() != null) {
            this.mCBLMapper.save(this);
        } else {
            throw new IllegalStateException("You must attach a mapper with a opened database connection to this document");
        }
    }
}
