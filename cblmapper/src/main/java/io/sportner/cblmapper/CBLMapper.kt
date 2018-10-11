package io.sportner.cblmapper

import com.couchbase.lite.*
import io.sportner.cblmapper.exceptions.CBLMapperClassException
import io.sportner.cblmapper.exceptions.UnhandledTypeException
import io.sportner.cblmapper.mappers.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.reflect.KClass
import kotlin.reflect.KTypeProjection
import kotlin.reflect.full.cast
import kotlin.reflect.full.isSubclassOf

class CBLMapper() {

    private var database: Database? = null
    private var adapterMap = HashMap<KClass<*>, CBLMTypeAdapter<*>>()

    init {
        // Register default type adapters
        setTypeAdapter(DateDefaultTypeAdapter(), Date::class)
        setTypeAdapter(EnumDefaultTypeAdapter(), Enum::class)
        setTypeAdapter(ListDefaultTypeAdapter(), List::class)
        setTypeAdapter(ListDefaultTypeAdapter(), ArrayList::class)
        setTypeAdapter(ObjectDefaultTypeAdapter(), Any::class)
        setTypeAdapter(MapDefaultTypeAdapter(), Map::class)
        setTypeAdapter(MapDefaultTypeAdapter(), HashMap::class)
    }

    constructor(database: Database) : this() {
        this.database = database
    }

    /**
     * Register an adapter for a specific type
     * If a type is registered twice, the last adapter overrides the previous one.
     */
    fun setTypeAdapter(adapter: CBLMTypeAdapter<*>, type: KClass<*>) {
        adapterMap[type] = adapter
    }

    @Throws(CBLMapperClassException::class, CouchbaseLiteException::class)
    fun save(cblDocument: CBLDocument) {
        this.database?.save(this.toDocument(cblDocument))
                ?: run {
                    throw IllegalStateException("You must attach a database in order to save documents")
                }
    }

    @Suppress("UNCHECKED_CAST")
    @Throws(CBLMapperClassException::class)
    fun toDocument(cblDoc: ICBLDocument): MutableDocument {
        var doc = cblDoc.document
        if (doc == null && cblDoc.documentID != null) {
            doc = this.database?.getDocument(cblDoc.documentID)
        }

        val mutableDocument = if (doc == null) MutableDocument(cblDoc.documentID) else doc.toMutable()

        this.encode(cblDoc, cblDoc::class, null)?.let {
            mutableDocument.setData(it as Map<String, Any>)
        }
        return mutableDocument
    }

    @Throws(CBLMapperClassException::class)
    fun <T : ICBLDocument> fromDocument(document: Document?, classOfT: KClass<T>): T? {
        return if (document == null) null else {
            val mappedInstance = this.decode(document.toMap(), classOfT, null)

            if (mappedInstance != null) {
                mappedInstance.document = document
            }
            mappedInstance
        }
    }

    @Suppress("UNCHECKED_CAST")
    @Throws(CBLMapperClassException::class)
    internal fun encode(value: Any?, typeOfT: KClass<*>, typesParameter: List<KTypeProjection>?): Any? {
        if (value == null) return value
        return when {
            value::class == Boolean::class -> value
            value::class.isSubclassOf(Number::class) -> value
            value::class == String::class -> value
            value::class == Char::class -> value.toString()
            value::class == Date::class -> value
            value is String -> value
            value::class.isSubclassOf(Enum::class) -> (adapterMap[Enum::class.java.kotlin]!! as CBLMTypeAdapter<Any>).encode(value, typeOfT as KClass<Any>, typesParameter, CBLMapperEncoderContext(this))
            value::class.isSubclassOf(ICBLDocument::class) -> (adapterMap[Any::class.java.kotlin]!! as CBLMTypeAdapter<Any>).encode(value, typeOfT as KClass<Any>, typesParameter, CBLMapperEncoderContext(this))
            adapterMap.containsKey(value.javaClass.kotlin) -> {
                (adapterMap[value.javaClass.kotlin]!! as CBLMTypeAdapter<Any>).encode(value, typeOfT as KClass<Any>, typesParameter, CBLMapperEncoderContext(this))
            }
            value is Blob -> value
            else -> {
                throw UnhandledTypeException(value.javaClass)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    internal fun <T : Any> decode(value: Any?, typeOfT: KClass<T>, typesParameter: List<KTypeProjection>?): T? {
        val returnValue = when {
            value == null -> value
            typeOfT == Boolean::class -> value
            typeOfT.isSubclassOf(Number::class) -> value
            typeOfT == String::class -> value
            typeOfT == Char::class && (value as String).length > 0 -> value[0]
            typeOfT == Date::class -> value
            typeOfT.isSubclassOf(ICBLDocument::class) -> (adapterMap[Any::class.java.kotlin]!! as CBLMTypeAdapter<Any>).decode(value, typeOfT as KClass<Any>, typesParameter, CBLMapperDecoderContext(this))
            typeOfT.isSubclassOf(Enum::class) -> (adapterMap[Enum::class.java.kotlin]!! as CBLMTypeAdapter<Any>).decode(value, typeOfT as KClass<Any>, null, CBLMapperDecoderContext(this))
            adapterMap.containsKey(typeOfT) -> (adapterMap[typeOfT]!! as CBLMTypeAdapter<Any>).decode(value, typeOfT as KClass<Any>, typesParameter, CBLMapperDecoderContext(this))
            typeOfT == Blob::class -> value
            else -> throw  UnhandledTypeException(typeOfT)
        }

        return if (returnValue != null) typeOfT.cast(returnValue) else null
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
