package io.sportner.cblmapper

import com.couchbase.lite.Database
import com.couchbase.lite.DocumentChange
import com.couchbase.lite.DocumentChangeListener
import io.sportner.cblmapper.exceptions.CBLMapperClassException

abstract class CBLDocumentChangeListener<T : CBLDocument>(private val mDatabase: Database, private val mCBLMapper: CBLMapper, private val mType: Class<T>) : DocumentChangeListener {

    override fun changed(change: DocumentChange) {
        try {
            this.onDocumentChange(this.mCBLMapper.fromDocument(this.mDatabase.getDocument(change.documentID), this.mType))
        } catch (var3: CBLMapperClassException) {
            var3.printStackTrace()
        }

    }

    abstract fun onDocumentChange(var1: T?)
}
