package io.sportner.cblmapper

import android.text.TextUtils
import com.couchbase.lite.*
import com.couchbase.lite.Array
import com.couchbase.lite.Dictionary
import com.couchbase.lite.internal.utils.DateUtils
import io.sportner.cblmapper.annotations.CBLEnumValue
import io.sportner.cblmapper.annotations.DocumentField
import io.sportner.cblmapper.annotations.NestedDocument
import io.sportner.cblmapper.exceptions.CBLMapperClassException
import io.sportner.cblmapper.exceptions.UnsupportedIDFieldTypeException
import io.sportner.cblmapper.util.FieldHelper
import io.sportner.cblmapper.util.Primitives
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter
import java.lang.reflect.Field
import java.lang.reflect.ParameterizedType
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.Map.Entry

class CBLMapper() {

    internal var database: Database? = null

    constructor(db: Database) : this() {
        database = db
    }

    @Throws(CBLMapperClassException::class, CouchbaseLiteException::class)
    fun save(cblDocument: CBLDocument) {
        this.database?.let { db ->
            db.save(this.toDocument(cblDocument))
        } ?: run {
            throw IllegalStateException("You must attach a database in order to save documents")
        }
    }

    @Throws(CBLMapperClassException::class)
    fun toDocument(cblDoc: CBLDocument): MutableDocument {
        var doc = cblDoc.document
        if (doc == null) {
            doc = this.database?.getDocument(cblDoc.documentID!!)
        }

        val mutableDocument: MutableDocument
        mutableDocument = if (doc == null) MutableDocument(cblDoc.documentID) else {
            doc.toMutable()
        }
        this.encode(cblDoc, true)?.let {
            mutableDocument.setData(it as Map<String, Any>)
        }
        return mutableDocument
    }

    @Throws(CBLMapperClassException::class)
    fun <T : CBLDocument> load(instanceOfT: T) {
        var doc = instanceOfT.document
        if (doc == null) {
            doc = this.database!!.getDocument(instanceOfT.documentID!!)
            instanceOfT.document = doc
        }

        if (doc != null) {
            this.decodeObject(doc.toMap(), instanceOfT)
        }

    }

    @Throws(CBLMapperClassException::class)
    fun <T : CBLDocument> load(docID: String, typeOfT: Class<T>): T? {
        return this.fromDocument(this.database?.getDocument(docID), typeOfT)
    }

    @Throws(CBLMapperClassException::class)
    fun <T : CBLDocument> fromDocument(dictionary: Document?, typeOfT: Class<T>): T? {
        if (dictionary == null) {
            return null
        } else {
            var `object`: T?
            `object` = this.decode<Any>(dictionary.toMap(), typeOfT, true, null as NestedDocument?) as T
            if (`object` != null && dictionary is Document) {
                `object`.document = dictionary
                this.setCBLDocumentID(`object`, dictionary.id)
            }

            return `object`
        }
    }

    @Throws(CBLMapperClassException::class)
    fun <T> fromDocument(dictionary: Dictionary?, typeOfT: Class<T>): T? {
        return if (dictionary != null) this.decode<Any>(dictionary.toMap(), typeOfT, true, null as NestedDocument?) as T? else null
    }

    @Throws(CBLMapperClassException::class)
    fun <T> fromDocument(dictionary: Result?, typeOfT: Class<T>): T? {
        return if (dictionary != null) this.decode<Any>(dictionary.toMap(), typeOfT, true, null as NestedDocument?) as T? else null
    }

    private fun setCBLDocumentID(cblDocument: CBLDocument, docID: String) {
        val var3 = FieldHelper.getFieldsUpTo(cblDocument.javaClass, Any::class.java).iterator()

        while (var3.hasNext()) {
            val field = var3.next() as Field
            val documentFieldAnnotation = field.getAnnotation(DocumentField::class.java)
            if (documentFieldAnnotation != null && documentFieldAnnotation.ID) {
                if (field.type != String::class.java) {
                    throw UnsupportedIDFieldTypeException(cblDocument.javaClass)
                }

                try {
                    if (!field.isAccessible) {
                        field.isAccessible = true
                    }

                    field.set(cblDocument, docID)
                } catch (var7: IllegalAccessException) {
                }

                break
            }
        }

    }

    @Throws(CBLMapperClassException::class)
    private fun encode(value: Any?, isRoot: Boolean): Any? {
        return this.encode(value, null as NestedDocument?, isRoot)
    }

    private fun encode(value: Any?, annotation: NestedDocument?): Any? {
        return this.encode(value, annotation, false)
    }

    @Throws(CBLMapperClassException::class)
    private fun encode(value: Any?, annotation: NestedDocument?, isRoot: Boolean): Any? {
        return if (value != null && value !is String && value !is Number && value !is Boolean && value !is Blob) {
            if (value is CBLDocument && !isRoot && annotation == null) {
                value.documentID
            } else if (value != null && value.javaClass.isEnum) {
                this.encodeEnumValue(value as Enum<*>)
            } else value as? Dictionary ?: (value as? Array ?: if (value is Map<*, *>) {
                this.encodeMap(value as Map<String, Any>?, annotation)
            } else if (value is List<*>) {
                this.encodeList(value as List<Any>?, annotation)
            } else if (value is Date) {
                DateUtils.toJson(value as Date?)
            } else {
                if (value is ZonedDateTime) value.format(DateTimeFormatter.ISO_ZONED_DATE_TIME) else this.encodeObject(value, annotation)
            })
        } else {
            value
        }
    }

    private fun encodeEnumValue(enumValue: Enum<*>): String {
        return try {
            val field = enumValue.javaClass.getField(enumValue.name)
            val annotation = field.getAnnotation(CBLEnumValue::class.java) as CBLEnumValue
            if (annotation.value.isEmpty()) enumValue.name else annotation.value
        } catch (e: NoSuchFieldException) {
            enumValue.name
        }
    }

    @Throws(CBLMapperClassException::class)
    private fun <T> decode(value: Any?, field: Field): T? {
        val typeOfT = field.type
        val result: Any?
        if (value == null) {
            result = null
        } else if (List::class.java.isAssignableFrom(typeOfT)) {
            result = this.decodeCBLList<Any>(value as List<Any>, field)
        } else if (CBLDocument::class.java.isAssignableFrom(typeOfT)) {
            val nestedAnnotation = field.getAnnotation(NestedDocument::class.java) as NestedDocument
            if (nestedAnnotation != null) {
                result = this.decodeObject(value as Map<String, Any>?, typeOfT)
            } else {
                result = this.instanciateObject(typeOfT)
                (result as CBLDocument).documentID = value as String?
            }

            (result as CBLDocument).cblMapper = this
        } else if (Enum::class.java.isAssignableFrom(typeOfT)) {
            result = this.decodeEnum(value as String, typeOfT as Class<Enum<*>>)
        } else if (typeOfT == Date::class.java) {
            result = typeOfT.cast(DateUtils.fromJson(value as String?))
        } else if (typeOfT == ZonedDateTime::class.java) {
            result = ZonedDateTime.parse((value as String?)!!, DateTimeFormatter.ISO_ZONED_DATE_TIME)
        } else if (!Primitives.isPrimitive(typeOfT) && value !is String && value !is Number && value !is Boolean && value !is Blob) {
            result = this.decodeObject(value as Map<String, Any>?, typeOfT)
        } else {
            result = value
        }

        return result as T?
    }

    private fun decodeEnum(value: String, fieldType: Class<Enum<*>>): Enum<*>? {
        val var3 = fieldType.enumConstants

        for (item in var3) {
            val enumValue = item as Enum<*>

            try {
                val enumAnnotation = fieldType.getField(enumValue.name).getAnnotation(CBLEnumValue::class.java) as CBLEnumValue
                if (enumAnnotation != null) {
                    if (enumAnnotation.value == value) {
                        return enumValue
                    }
                } else if (enumValue.name == value) {
                    return enumValue
                }
            } catch (var8: NoSuchFieldException) {
                var8.printStackTrace()
            }

        }

        return null
    }

    @Throws(CBLMapperClassException::class)
    private fun <T> decode(value: Any?, typeOfT: Class<*>, isRoot: Boolean, nestedAnnotation: NestedDocument?): T? {
        val result: Any?
        if (value == null) {
            result = null
        } else if (CBLDocument::class.java.isAssignableFrom(typeOfT)) {
            if (!isRoot && nestedAnnotation == null) {
                result = this.instanciateObject(typeOfT)
                (result as CBLDocument).documentID = value as String?
            } else {
                result = this.decodeObject(value as Map<String, Any>?, typeOfT)
            }

            (result as CBLDocument).cblMapper = this
        } else if (Enum::class.java.isAssignableFrom(typeOfT)) {
            result = this.decodeEnum(value as String, typeOfT as Class<Enum<*>>)
        } else if (typeOfT == Date::class.java) {
            result = typeOfT.cast(DateUtils.fromJson(value as String?))
        } else if (!Primitives.isPrimitive(typeOfT) && value !is String && value !is Number && value !is Boolean && value !is Blob) {
            result = this.decodeObject(value as Map<String, Any>?, typeOfT)
        } else {
            result = value
        }

        return result as T?
    }

    @Throws(CBLMapperClassException::class)
    private fun <T> decodeCBLList(value: List<*>, typeOfT: Field): T? {
        val parameterizedType = typeOfT.genericType as ParameterizedType
        val itemClass = parameterizedType.actualTypeArguments[0] as Class<*>
        var result: List<*>? = null
        if (typeOfT.type == List::class.java) {
            result = ArrayList<Any>()
        } else {
            try {
                result = typeOfT.type.newInstance() as List<*>
            } catch (var8: InstantiationException) {
                var8.printStackTrace()
            } catch (var9: IllegalAccessException) {
                var9.printStackTrace()
            }

        }

        val var6 = value.iterator()

        while (var6.hasNext()) {
            val item = var6.next()
            (result as MutableList<Any?>).add(this.decode<Any>(item, itemClass, false, typeOfT.getAnnotation(NestedDocument::class.java)))
        }

        return result as T?
    }

    private fun instanciateObject(typeOfT: Class<*>): Any {
        try {
            return typeOfT.newInstance()
        } catch (var4: IllegalAccessException) {
            var4.printStackTrace()
            throw CBLMapperClassException(typeOfT, "Verify constructor with no argument is public")
        } catch (var5: InstantiationException) {
            var5.printStackTrace()
            throw CBLMapperClassException(typeOfT, "Type might be an abstract class, an interface, an array class, a primitive type, or void. Verify class contains a public constructor with no argument")
        }
    }

    @Throws(CBLMapperClassException::class)
    private fun <T: Any> decodeObject(value: Map<String, Any>?, typeOfT: Class<T>): T? {
        return this.decodeObject(value, this.instanciateObject(typeOfT) as T)
    }

    @Throws(CBLMapperClassException::class)
    private fun <T: Any> decodeObject(value: Map<String, Any>?, instanceOfT: T): T? {
        if (value == null) {
            return null
        } else {
            val var3 = FieldHelper.getFieldsUpTo(instanceOfT.javaClass, Any::class.java).iterator()

            while (var3.hasNext()) {
                val field = var3.next() as Field
                val documentFieldAnnotation = field.getAnnotation(DocumentField::class.java)
                if (documentFieldAnnotation != null) {
                    val fieldName = if (TextUtils.isEmpty(documentFieldAnnotation.fieldName)) field.name else documentFieldAnnotation.fieldName
                    val isPrivate = !field.isAccessible
                    if (isPrivate) {
                        field.isAccessible = true
                    }

                    try {
                        field.set(instanceOfT, this.decode(value[fieldName], field))
                    } catch (var9: IllegalAccessException) {
                        var9.printStackTrace()
                    } catch (var10: IllegalArgumentException) {
                        var10.printStackTrace()
                    }

                    if (isPrivate) {
                        field.isAccessible = false
                    }
                }
            }

            return instanceOfT
        }
    }

    @Throws(CBLMapperClassException::class)
    private fun encodeList(value: List<Any>?, annotation: NestedDocument?): List<Any?>? {
        value?.let {
            val encodedValue = ArrayList<Any?>()
            val var4 = value.iterator()

            while (var4.hasNext()) {
                val entry = var4.next()
                encodedValue.add(this.encode(entry, annotation))
            }

            return encodedValue
        } ?: run {
            return null
        }
    }

    @Throws(CBLMapperClassException::class)
    private fun encodeMap(value: Map<String, Any>?, annotation: NestedDocument?): Any? {
        if (value == null) {
            return null
        } else {
            val encodedValue = HashMap<String, Any?>()
            val var4 = value.entries.iterator()

            while (var4.hasNext()) {
                val entry = var4.next() as Entry<String, *>
                encodedValue[entry.key] = this.encode(entry.value, annotation)
            }

            return encodedValue
        }
    }

    @Throws(CBLMapperClassException::class)
    private fun encodeObject(value: Any?): Map<String, Any>? {
        return this.encodeObject(value, null as NestedDocument?)
    }

    @Throws(CBLMapperClassException::class)
    private fun encodeObject(value: Any?, parentAnnotation: NestedDocument?): Map<String, Any>? {
        if (value == null) {
            return null
        } else {
            if (parentAnnotation != null) {
                Arrays.sort(parentAnnotation.omitFields)
            }

            val map = HashMap<String, Any>()
            val var4 = FieldHelper.getFieldsUpTo(value.javaClass, Any::class.java).iterator()

            while (true) {
                var field: Field
                var documentFieldAnnotation: DocumentField?
                var fieldName: String
                do {
                    do {
                        if (!var4.hasNext()) {
                            return map
                        }

                        field = var4.next() as Field
                        documentFieldAnnotation = field.getAnnotation(DocumentField::class.java)
                    } while (documentFieldAnnotation == null)

                    fieldName = if (TextUtils.isEmpty(documentFieldAnnotation.fieldName)) field.name else documentFieldAnnotation.fieldName
                } while (parentAnnotation != null && Arrays.binarySearch(parentAnnotation.omitFields, documentFieldAnnotation!!.fieldName) >= 0)

                if (!field.isAccessible) {
                    field.isAccessible = true
                }

                try {
                    this.encode(field.get(value), field.getAnnotation(NestedDocument::class.java))?.let {
                        map[fieldName] = it
                    }
                } catch (var9: IllegalAccessException) {
                    var9.printStackTrace()
                }

            }
        }
    }

    companion object {
        private lateinit var INSTANCE: CBLMapper

        fun configureDefaultInstance(db: Database): CBLMapper {
            INSTANCE = CBLMapper(db)
            return INSTANCE
        }

        val defaultInstance: CBLMapper
            get() = INSTANCE
    }
}
