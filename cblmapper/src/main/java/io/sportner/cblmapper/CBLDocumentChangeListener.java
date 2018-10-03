package io.sportner.cblmapper;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.couchbase.lite.Database;
import com.couchbase.lite.DocumentChange;
import com.couchbase.lite.DocumentChangeListener;
import io.sportner.cblmapper.exceptions.CBLMapperClassException;

public abstract class CBLDocumentChangeListener<T extends CBLDocument> implements DocumentChangeListener {
    private final CBLMapper mCBLMapper;
    private final Database mDatabase;
    private final Class<T> mType;

    public CBLDocumentChangeListener(@NonNull Database database, @NonNull CBLMapper mapper, @NonNull Class<T> type) {
        this.mDatabase = database;
        this.mCBLMapper = mapper;
        this.mType = type;
    }

    public void changed(DocumentChange change) {
        try {
            this.onDocumentChange(this.mCBLMapper.fromDocument(this.mDatabase.getDocument(change.getDocumentID()), this.mType));
        } catch (CBLMapperClassException var3) {
            var3.printStackTrace();
        }

    }

    public abstract void onDocumentChange(@Nullable T var1);
}
