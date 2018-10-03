package io.sportner.cblmapper

import com.couchbase.lite.CouchbaseLiteException
import com.couchbase.lite.Document
import io.sportner.cblmapper.annotations.DocumentField
import io.sportner.cblmapper.exceptions.UnsupportedIDFieldTypeException
import io.sportner.cblmapper.util.FieldHelper
import java.lang.reflect.Field

open class CBLDocument {

    @Transient
    private var mDocument: Document? = null

    @Transient
    var cblMapper: CBLMapper? = null

    @Transient
    var documentID: String? = null
        set(documentID) {
            field = documentID
            this.mDocument = null
            val var2 = FieldHelper.getFieldsUpTo(this.javaClass, Any::class.java).iterator()

            while (var2.hasNext()) {
                val field = var2.next() as Field
                val documentFieldAnnotation = field.getAnnotation(DocumentField::class.java) as DocumentField?
                if (documentFieldAnnotation != null && documentFieldAnnotation?.ID) {
                    if (field.type != String::class.java) {
                        throw UnsupportedIDFieldTypeException(this.javaClass)
                    }

                    try {
                        val isPrivate = !field.isAccessible
                        if (isPrivate) {
                            field.isAccessible = true
                        }

                        field.set(this, documentID)
                        if (isPrivate) {
                            field.isAccessible = false
                        }
                    } catch (var6: IllegalAccessException) {
                    }

                    break
                }
            }

        }

    var document: Document?
        get() = this.mDocument
        set(document) {
            this.documentID = document?.id
            this.mDocument = document
        }

    constructor()

    constructor(documentID: String?) {
        this.init(documentID, null as CBLMapper?)
    }

    constructor(cblMapper: CBLMapper?) {
        this.init(null as String?, cblMapper)
    }

    constructor(documentID: String?, cblMapper: CBLMapper?) {
        this.init(documentID, cblMapper)
    }

    private fun init(documentID: String?, cblMapper: CBLMapper?) {
        this.cblMapper = cblMapper
        this.documentID = documentID
    }

    @Throws(CouchbaseLiteException::class)
    fun delete() {
        this.cblMapper?.database?.let { db ->
            if (this.mDocument == null) {
                this.mDocument = db.getDocument(this.documentID!!)
            }
            if (this.mDocument != null) {
                db.delete(this.mDocument)
            }
        } ?: run {
            throw IllegalStateException("You must attach a mapper with an opened database connection to this document")
        }
    }

    fun load() {
        this.cblMapper!!.load(this)
    }

    @Throws(CouchbaseLiteException::class)
    fun save() {
        this.save(true)
    }

    @Throws(CouchbaseLiteException::class)
    fun save(saveChildren: Boolean) {
        this.cblMapper?.let {
            it.save(this@CBLDocument)
        } ?: run {
            throw IllegalStateException("You must attach a mapper with a opened database connection to this document")
        }
    }
}
