package io.sportner.cblmapper;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.couchbase.lite.Database;
import com.couchbase.lite.DocumentChange;
import com.couchbase.lite.DocumentChangeListener;

import io.sportner.cblmapper.exceptions.CBLMapperClassException;

/**
 * Created by alblanc on 27/09/2017.
 */

public abstract class MappedDocumentChangeListener<T extends CBLDocument> implements DocumentChangeListener{

    private final CBLMapper mCBLMapper;
    private final Database mDatabase;
    private final Class<T> mType;

    public MappedDocumentChangeListener(@NonNull Database database, @NonNull CBLMapper mapper, @NonNull Class<T> type){
        mDatabase = database;
        mCBLMapper = mapper;
        mType = type;
    }

    @Override
    public void changed(DocumentChange change) {
        try {
            onDocumentChange(change, mCBLMapper.fromDocument(mDatabase.getDocument(change.getDocumentID()), mType));
        } catch (CBLMapperClassException e) {
            e.printStackTrace();
        }
    }

    public abstract void onDocumentChange(@NonNull DocumentChange change, @Nullable T object);
}
