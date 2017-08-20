package io.sportner.cblmapper;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;

import com.couchbase.lite.Blob;
import com.couchbase.lite.Dictionary;
import com.couchbase.lite.Document;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

import io.sportner.cblmapper.annotations.DocumentField;
import io.sportner.cblmapper.annotations.NestedDocument;
import io.sportner.cblmapper.bind.BlobTypeAdapter;
import io.sportner.cblmapper.bind.BooleanTypeAdapter;
import io.sportner.cblmapper.bind.DateTypeAdapter;
import io.sportner.cblmapper.bind.DoubleTypeAdapter;
import io.sportner.cblmapper.bind.FloatTypeAdapter;
import io.sportner.cblmapper.bind.IntegerTypeAdapter;
import io.sportner.cblmapper.bind.LongTypeAdapter;
import io.sportner.cblmapper.bind.NumberTypeAdapter;
import io.sportner.cblmapper.bind.StringTypeAdapter;
import io.sportner.cblmapper.bind.TypeAdapter;
import io.sportner.cblmapper.exceptions.CBLMapperClassException;
import io.sportner.cblmapper.exceptions.UnhandledTypeException;
import io.sportner.cblmapper.exceptions.UnsupportedIDFieldTypeException;
import io.sportner.cblmapper.util.FieldHelper;
import io.sportner.cblmapper.util.Primitives;

/**
 * Created by alblanc on 19/08/2017.
 */

public class CBLMapper {

    public static final String TAG = "CBLMapper";

    private Map<Class, TypeAdapter> mTypeAdapters;

    public CBLMapper() {
        mTypeAdapters = new ArrayMap<>();

        registerTypeAdapter(String.class, new StringTypeAdapter());
        registerTypeAdapter(Integer.class, new IntegerTypeAdapter());
        registerTypeAdapter(Boolean.class, new BooleanTypeAdapter());
        registerTypeAdapter(Long.class, new LongTypeAdapter());
        registerTypeAdapter(Double.class, new DoubleTypeAdapter());
        registerTypeAdapter(Date.class, new DateTypeAdapter());
        registerTypeAdapter(Float.class, new FloatTypeAdapter());
        registerTypeAdapter(Number.class, new NumberTypeAdapter());
        registerTypeAdapter(Blob.class, new BlobTypeAdapter());
    }

    public void registerTypeAdapter(@NonNull Class newType, @NonNull TypeAdapter adapter) {
        mTypeAdapters.put(newType, adapter);
    }

    private String findDocumentID(@NonNull Object object) throws UnsupportedIDFieldTypeException {
        for (Field field : FieldHelper.getFieldsUpTo(object.getClass(), Object.class)) {
            DocumentField documentFieldAnnotation = field.getAnnotation(DocumentField.class);
            if (documentFieldAnnotation != null && documentFieldAnnotation.ID()) {
                if (field.getType() == String.class) {
                    try {
                        if (!field.isAccessible()) {
                            field.setAccessible(true);
                        }
                        return (String) field.get(object);
                    } catch (IllegalAccessException e) {
                        // Ignore as it can't happen
                    }
                    break;
                } else {
                    throw new UnsupportedIDFieldTypeException(object.getClass());
                }
            }
        }
        return null;
    }

    public Document toDocument(@Nullable Object object) throws CBLMapperClassException {
        if (object == null) {
            return new Document();
        }

        Document document = new Document(findDocumentID(object));

        readObject(object, document);
        return document;
    }

    public Document toDocument(@Nullable Object object, String documentID) throws CBLMapperClassException {
        Document document = new Document(documentID);
        readObject(object, document);
        return document;
    }

    public Document readObject(@Nullable Object object, Document document) throws UnhandledTypeException {
        for (Field field : FieldHelper.getFieldsUpTo(object.getClass(), Object.class)) {
            DocumentField documentFieldAnnotation = field.getAnnotation(DocumentField.class);
            if (documentFieldAnnotation != null && !documentFieldAnnotation.ID()) {
                // User fieldName as document field name if not specified as DocumentField.fieldName parameter
                String fieldName = TextUtils.isEmpty(documentFieldAnnotation.fieldName()) ? field.getName() : documentFieldAnnotation.fieldName();

                // Force access private members
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }

                // Cast primitive to their Object equivalent
                // eg: int -> Integer
                Class fieldClass = field.getType();
                if (Primitives.isPrimitive(fieldClass)) {
                    fieldClass = Primitives.wrap(fieldClass);
                }

                NestedDocument nestedFieldAnnotation = field.getAnnotation(NestedDocument.class);
                if (nestedFieldAnnotation != null) {
                    try {
                        document.setDictionary(fieldName,
                                               readObject(fieldClass.cast(field.get(object)),
                                                          new Dictionary(),
                                                          nestedFieldAnnotation));
                        continue;
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }

                TypeAdapter typeAdapter = mTypeAdapters.get(fieldClass);
                if (typeAdapter == null) {
                    throw new UnhandledTypeException(fieldClass);
                }
                try {
                    typeAdapter.writeDocument(document, fieldName, fieldClass.cast(field.get(object)));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }

            }
        }
        return document;
    }

    public Dictionary readObject(@Nullable Object object, @NonNull Dictionary dictionary, @Nullable NestedDocument parentAnnotation) throws
                                                                                                                                     UnhandledTypeException {
        if (parentAnnotation != null) {
            Arrays.sort(parentAnnotation.omitFields());
        }

        for (Field field : FieldHelper.getFieldsUpTo(object.getClass(), Object.class)) {
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

                // Cast primitive to their Object equivalent
                // eg: int -> Integer
                Class fieldClass = field.getType();
                if (Primitives.isPrimitive(fieldClass)) {
                    fieldClass = Primitives.wrap(fieldClass);
                }

                NestedDocument nestedFieldAnnotation = field.getAnnotation(NestedDocument.class);
                if (nestedFieldAnnotation != null) {
                    try {
                        dictionary.setDictionary(fieldName,
                                                 readObject(fieldClass.cast(field.get(object)),
                                                            new Dictionary(),
                                                            nestedFieldAnnotation));
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }

                TypeAdapter typeAdapter = mTypeAdapters.get(fieldClass);
                if (typeAdapter == null) {
                    throw new UnhandledTypeException(fieldClass);
                }

                try {
                    typeAdapter.writeDocument(dictionary, fieldName, fieldClass.cast(field.get(object)));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return dictionary;
    }

    public <T> T fromDocument(Document document, Class<T> typeOfT) throws CBLMapperClassException {

        if (document == null) {
            return null;
        }

        T object = null;
        try {
            object = (T) typeOfT.newInstance();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            Log.e(TAG, "Constructor must be public");
        } catch (InstantiationException e) {
            e.printStackTrace();
        }

        if (object == null) {
            return null;
        }

        for (Field field : FieldHelper.getFieldsUpTo(object.getClass(), Object.class)) {
            DocumentField documentFieldAnnotation = field.getAnnotation(DocumentField.class);
            if (documentFieldAnnotation != null) {
                String fieldName = TextUtils.isEmpty(documentFieldAnnotation.fieldName()) ? field.getName() : documentFieldAnnotation.fieldName();

                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }

                if (documentFieldAnnotation.ID()) {
                    try {
                        field.set(object, document.getId());
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                        // Should not happen
                    }
                    continue;
                }

                NestedDocument nestedDocumentAnnotation = field.getAnnotation(NestedDocument.class);
                if (nestedDocumentAnnotation != null) {
                    try {
                        field.set(object, fromDocument(document.getDictionary(fieldName), field.getType(), nestedDocumentAnnotation));
                        continue;
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                        // Should not happen
                    }
                }

                // Cast primitive to their Object equivalent
                // eg: int -> Integer
                Class fieldClass = field.getType();
                if (Primitives.isPrimitive(fieldClass)) {
                    fieldClass = Primitives.wrap(fieldClass);
                }

                TypeAdapter typeAdapter = mTypeAdapters.get(fieldClass);
                if (typeAdapter == null) {
                    throw new UnhandledTypeException(fieldClass);
                }

                try {
                    field.set(object, typeAdapter.readDocument(document, fieldName));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    // Should not happen
                }
            }
        }

        return object;
    }

    public <T> T fromDocument(Dictionary document, Class<T> typeOfT, NestedDocument parentNestedAnnotation) throws CBLMapperClassException {
        if (document == null) {
            return null;
        }

        T object = null;
        try {
            object = (T) typeOfT.newInstance();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            Log.e(TAG, "Constructor must be public");
        } catch (InstantiationException e) {
            e.printStackTrace();
        }

        if (object == null) {
            return null;
        }

        for (Field field : FieldHelper.getFieldsUpTo(object.getClass(), Object.class)) {
            DocumentField documentFieldAnnotation = field.getAnnotation(DocumentField.class);
            if (documentFieldAnnotation != null) {
                String fieldName = TextUtils.isEmpty(documentFieldAnnotation.fieldName()) ? field.getName() : documentFieldAnnotation.fieldName();

                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }

                NestedDocument nestedDocumentAnnotation = field.getAnnotation(NestedDocument.class);
                if (nestedDocumentAnnotation != null) {
                    try {
                        field.set(object, fromDocument(document, field.getType(), nestedDocumentAnnotation));
                        continue;
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                        // Should not happen
                    }
                }

                // Cast primitive to their Object equivalent
                // eg: int -> Integer
                Class fieldClass = field.getType();
                if (Primitives.isPrimitive(fieldClass)) {
                    fieldClass = Primitives.wrap(fieldClass);
                }

                TypeAdapter typeAdapter = mTypeAdapters.get(fieldClass);
                if (typeAdapter == null) {
                    throw new UnhandledTypeException(fieldClass);
                }

                try {
                    field.set(object, typeAdapter.readDocument(document, fieldName));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    // Should not happen
                }
            }
        }
        return object;
    }
}
