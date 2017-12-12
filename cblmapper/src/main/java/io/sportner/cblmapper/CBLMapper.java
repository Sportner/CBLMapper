package io.sportner.cblmapper;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.couchbase.lite.Array;
import com.couchbase.lite.Blob;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Dictionary;
import com.couchbase.lite.Document;
import com.couchbase.lite.ReadOnlyDictionary;
import com.couchbase.lite.internal.document.RemovedValue;
import com.couchbase.lite.internal.support.DateUtils;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.sportner.cblmapper.annotations.CBLEnumValue;
import io.sportner.cblmapper.annotations.DocumentField;
import io.sportner.cblmapper.annotations.NestedDocument;
import io.sportner.cblmapper.exceptions.CBLMapperClassException;
import io.sportner.cblmapper.exceptions.UnhandledTypeException;
import io.sportner.cblmapper.exceptions.UnsupportedIDFieldTypeException;
import io.sportner.cblmapper.util.FieldHelper;
import io.sportner.cblmapper.util.Primitives;

/**
 * Created by alblanc on 22/08/2017.
 */

public class CBLMapper {

    static private CBLMapper INSTANCE;

    private Database mDatabase;

    public CBLMapper() {
    }

    public CBLMapper(Database database) {
        this();
        mDatabase = database;
    }

    static public CBLMapper configureDefaultInstance(@Nullable Database db) {
        INSTANCE = new CBLMapper(db);
        return INSTANCE;
    }

    static public CBLMapper getDefaultInstance() {
        if (INSTANCE == null) {
            throw new IllegalStateException("You must configure default instance before using it");
        }
        return INSTANCE;
    }

    public Database getDatabase() {
        return mDatabase;
    }

    public void save(CBLDocument cblDocument) throws CBLMapperClassException, CouchbaseLiteException {
        if (mDatabase == null) {
            throw new IllegalStateException("You must attach a database in order to save documents");
        }
        mDatabase.save(toDocument(cblDocument));
    }

    public Document toDocument(@NonNull CBLDocument object) throws CBLMapperClassException {
        Document doc = object.getDocument();

        if (doc == null && mDatabase != null) {
            doc = mDatabase.getDocument(object.getDocumentID());
        }

        if (doc == null) {
            doc = new Document(object.getDocumentID());
        }

        doc.set((Map<String, Object>) encode(object, true));
        return doc;
    }

    public <T extends CBLDocument> void load(T instanceOfT) throws CBLMapperClassException {
        Document doc = instanceOfT.getDocument();
        if (doc == null) {
            doc = mDatabase.getDocument(instanceOfT.getDocumentID());
            instanceOfT.setDocument(doc);
        }

        if (doc != null) {
            decodeObject(doc.toMap(), instanceOfT);
        }
    }

    public <T extends CBLDocument> T load(@NonNull String docID, @NonNull Class<T> typeOfT) throws CBLMapperClassException {
        return fromDocument(mDatabase.getDocument(docID), typeOfT);
    }

    public <T extends CBLDocument> T fromDocument(@Nullable ReadOnlyDictionary dictionary, @NonNull Class<T> typeOfT) throws CBLMapperClassException {
        if (dictionary == null) {
            return null;
        }

        T object = null;

        object = decode(dictionary.toMap(), typeOfT, true, null);

        // Attach ID & Document to object
        if (object != null && dictionary instanceof Document) {
            Document document = (Document) dictionary;
            object.setDocument(document);
            setCBLDocumentID(object, document.getId());
        }

        return object;
    }

    private void setCBLDocumentID(@NonNull CBLDocument cblDocument, @NonNull String docID) {
        for (Field field : FieldHelper.getFieldsUpTo(cblDocument.getClass(), Object.class)) {
            DocumentField documentFieldAnnotation = field.getAnnotation(DocumentField.class);
            if (documentFieldAnnotation != null && documentFieldAnnotation.ID()) {
                if (field.getType() == String.class) {
                    try {
                        if (!field.isAccessible()) {
                            field.setAccessible(true);
                        }
                        field.set(cblDocument, docID);
                    } catch (IllegalAccessException e) {
                        // Ignore as it can't happen
                    }
                    break;
                } else {
                    throw new UnsupportedIDFieldTypeException(cblDocument.getClass());
                }
            }
        }
    }

    //    public String findDocumentID(@NonNull CBLDocument object) throws UnsupportedIDFieldTypeException {
    //        Document document = object.getDocument();
    //        if (document != null && !TextUtils.isEmpty(document.getId())) {
    //            return document.getId();
    //        }
    //        for (Field field : FieldHelper.getFieldsUpTo(object.getClass(), Object.class)) {
    //            DocumentField documentFieldAnnotation = field.getAnnotation(DocumentField.class);
    //            if (documentFieldAnnotation != null && documentFieldAnnotation.ID()) {
    //                if (field.getType() == String.class) {
    //                    try {
    //                        if (!field.isAccessible()) {
    //                            field.setAccessible(true);
    //                        }
    //                        return (String) field.get(object);
    //                    } catch (IllegalAccessException e) {
    //                        // Ignore as it can't happen
    //                    }
    //                    break;
    //                } else {
    //                    throw new UnsupportedIDFieldTypeException(object.getClass());
    //                }
    //            }
    //        }
    //        return null;
    //    }

    private Object encode(@Nullable Object value, boolean isRoot) throws CBLMapperClassException {
        return encode(value, null, isRoot);
    }

    private Object encode(@Nullable Object value, @Nullable NestedDocument annotation) {
        return encode(value, annotation, false);
    }

    private Object encode(@Nullable Object value, @Nullable NestedDocument annotation, boolean isRoot) throws CBLMapperClassException {
        if (value == null ||
            value == RemovedValue.INSTANCE ||
            value instanceof String ||
            value instanceof Number ||
            value instanceof Boolean ||
            value instanceof Blob) {
            return value;
        }
        if (value == null || value == RemovedValue.INSTANCE) {
            return null;
        } else if (value instanceof CBLDocument && !isRoot && annotation == null) {
            return ((CBLDocument) value).getDocumentID();
        } else if (value != null && value.getClass().isEnum()) {
            return encodeEnumValue((Enum) value);
        } else if (value instanceof Dictionary) {
            return value;
        } else if (value instanceof Array) {
            return value;
        } else if (value instanceof Map) {
            return encodeMap((Map<String, Object>) value, annotation);
        } else if (value instanceof List) {
            return encodeList((List) value, annotation);
        } else if (value instanceof Date) {
            return DateUtils.toJson((Date) value);
        }
        return encodeObject(value, annotation);
    }

    private <T extends Enum<T>> String encodeEnumValue(T enumValue) {
        Field field = null;
        try {
            field = enumValue.getClass().getField(enumValue.name());
            CBLEnumValue annotation = field.getAnnotation(CBLEnumValue.class);
            return annotation == null ? enumValue.name() : annotation.value();
        } catch (NoSuchFieldException e) {
            return enumValue.name();
        }
    }

    private <T> T decode(@Nullable Object value, Field field) throws CBLMapperClassException {
        Object result;
        Class typeOfT = field.getType();
        if (value == null || value == RemovedValue.INSTANCE) {
            result = null;
        } else if (List.class.isAssignableFrom(typeOfT)) {
            result = decodeCBLList((List) value, field);
        } else if (CBLDocument.class.isAssignableFrom(typeOfT)) {
            NestedDocument nestedAnnotation = field.getAnnotation(NestedDocument.class);
            if (nestedAnnotation != null) {
                result = decodeObject((Map<String, Object>) value, typeOfT);
            } else {
                result = instanciateObject(typeOfT);
                ((CBLDocument) result).setDocumentID((String) value);
            }
            ((CBLDocument) result).setCBLMapper(this);
        } else if (Enum.class.isAssignableFrom(typeOfT)) {
            result = decodeEnum((String) value, typeOfT);
        } else if (typeOfT.equals(Date.class)) {
            result = (T) typeOfT.cast(DateUtils.fromJson((String) value));
        } else if (Primitives.isPrimitive(value.getClass()) ||
                   value instanceof String ||
                   value instanceof Number ||
                   value instanceof Boolean ||
                   value instanceof Blob) {
            result = value;
        } else {
            throw new UnhandledTypeException(value.getClass());
        }
        return (T) result;
    }

    private Enum decodeEnum(String value, Class<Enum> fieldType) {
        for (Enum enumValue : fieldType.getEnumConstants()) {
            try {
                CBLEnumValue enumAnnotation = fieldType.getField(enumValue.name()).getAnnotation(CBLEnumValue.class);
                if (enumAnnotation != null) {
                    if (enumAnnotation.value().equals(value)) {
                        return enumValue;
                    }
                } else if (enumValue.name().equals(value)) {
                    return enumValue;
                }
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private <T> T decode(@Nullable Object value, @NonNull Class typeOfT, boolean isRoot, @Nullable NestedDocument nestedAnnotation) throws
                                                                                                                                    CBLMapperClassException {

        Object result;
        if (value == null || value == RemovedValue.INSTANCE) {
            result = null;
        } else if (CBLDocument.class.isAssignableFrom(typeOfT)) {
            if (isRoot || nestedAnnotation != null) {
                result = decodeObject((Map<String, Object>) value, typeOfT);
            } else {
                result = instanciateObject(typeOfT);
                ((CBLDocument) result).setDocumentID((String) value);
            }
            ((CBLDocument) result).setCBLMapper(this);

        } else if (Enum.class.isAssignableFrom(typeOfT)) {
            result = decodeEnum((String) value, (Class<Enum>) typeOfT);
        } else if (typeOfT.equals(Date.class)) {
            result = (T) typeOfT.cast(DateUtils.fromJson((String) value));
        } else if (Primitives.isPrimitive(value.getClass()) ||
                   value instanceof String ||
                   value instanceof Number ||
                   value instanceof Boolean ||
                   value instanceof Blob) {
            result = value;
        } else {
            result = decodeObject((Map<String, Object>) value, typeOfT);
        }
        return (T) result;
    }

    private <T> T decodeCBLList(List value, Field typeOfT) throws CBLMapperClassException {
        ParameterizedType parameterizedType = (ParameterizedType) typeOfT.getGenericType();
        Class<?> itemClass = (Class<?>) parameterizedType.getActualTypeArguments()[0];

        List result = null;
        if (typeOfT.getType() == List.class) {
            result = new ArrayList();
        } else {
            try {
                result = (List) typeOfT.getType().newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        for (Object item : value) {
            result.add(decode(item, itemClass, false, typeOfT.getAnnotation(NestedDocument.class)));
        }
        return (T) result;
    }

    private Object instanciateObject(@NonNull Class typeOfT) {
        Object object = null;
        try {
            object = typeOfT.newInstance();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            throw new CBLMapperClassException(typeOfT, "Verify constructor with no argument is public");
        } catch (InstantiationException e) {
            e.printStackTrace();
            throw new CBLMapperClassException(typeOfT,
                                              "Type might be an abstract class, an interface, an array class, a primitive type, or void. " +
                                              "Verify class contains a public constructor with no argument");
        }
        return object;
    }

    private <T> T decodeObject(@Nullable Map<String, Object> value, @NonNull Class<T> typeOfT) throws CBLMapperClassException {
        T object = (T) instanciateObject(typeOfT);
        return decodeObject(value, object);
    }

    private <T> T decodeObject(@Nullable Map<String, Object> value, @NonNull T instanceOfT) throws CBLMapperClassException {
        if (value == null) {
            return null;
        }
        for (Field field : FieldHelper.getFieldsUpTo(instanceOfT.getClass(), Object.class)) {
            DocumentField documentFieldAnnotation = field.getAnnotation(DocumentField.class);
            if (documentFieldAnnotation != null && !documentFieldAnnotation.ID()) {
                String fieldName = TextUtils.isEmpty(documentFieldAnnotation.fieldName()) ? field.getName() : documentFieldAnnotation.fieldName();

                // Force access private members
                boolean isPrivate = !field.isAccessible();
                if (isPrivate) {
                    field.setAccessible(true);
                }

                try {
                    field.set(instanceOfT, decode(value.get(fieldName), field));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }

                if (isPrivate) {
                    field.setAccessible(false);
                }
            }
        }

        return instanceOfT;
    }

    private Object encodeList(@Nullable List<Object> value, @Nullable NestedDocument annotation) throws CBLMapperClassException {
        if (value == null) {
            return null;
        }
        List<Object> encodedValue = new ArrayList<>();
        for (Object entry : value) {
            encodedValue.add(encode(entry, annotation));
        }
        return encodedValue;
    }

    private Object encodeMap(@Nullable Map<String, Object> value, @Nullable NestedDocument annotation) throws CBLMapperClassException {
        if (value == null) {
            return null;
        }

        Map<String, Object> encodedValue = new HashMap<>();
        for (Map.Entry<String, Object> entry : value.entrySet()) {
            encodedValue.put(entry.getKey(), encode(entry.getValue(), annotation));
        }
        return encodedValue;
    }


    private Map<String, Object> encodeObject(@Nullable Object value) throws CBLMapperClassException {
        return encodeObject(value, null);
    }

    private Map<String, Object> encodeObject(@Nullable Object value, @Nullable NestedDocument parentAnnotation) throws CBLMapperClassException {
        if (value == null) {
            return null;
        }

        if (parentAnnotation != null) {
            // Prepare for binarySearch
            Arrays.sort(parentAnnotation.omitFields());
        }

        Map<String, Object> map = new HashMap<>();
        for (Field field : FieldHelper.getFieldsUpTo(value.getClass(), Object.class)) {
            DocumentField documentFieldAnnotation = field.getAnnotation(DocumentField.class);
            if (documentFieldAnnotation != null) {
                // User fieldName as document field name if not specified as DocumentField.fieldName parameter
                String fieldName = TextUtils.isEmpty(documentFieldAnnotation.fieldName()) ? field.getName() : documentFieldAnnotation.fieldName();

                if (parentAnnotation != null && Arrays.binarySearch(parentAnnotation.omitFields(), documentFieldAnnotation.fieldName()) >= 0) { // if exist
                    // Omit if specified in parent annotation (NestedDocument.omitFields)
                    continue;
                }

                // Force access private members
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }

                try {
                    map.put(fieldName, encode(field.get(value), field.getAnnotation(NestedDocument.class)));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return map;
    }
}
