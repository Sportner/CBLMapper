package io.sportner.cblmapper

import com.couchbase.lite.Document

interface ICBLDocument {
    var documentID: String?
    var document: Document?
}

open class CBLDocument : ICBLDocument {
    @Transient
    override var documentID: String? = null

    override var document: Document? = null
        set(document) {
            this.documentID = document?.id
            field = document
        }
}
