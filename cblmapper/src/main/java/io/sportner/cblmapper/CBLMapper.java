package io.sportner.cblmapper;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.couchbase.lite.Array;
import com.couchbase.lite.Blob;
import com.couchbase.lite.Dictionary;
import com.couchbase.lite.Document;
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

import io.sportner.cblmapper.annotations.CBLDocument;
import io.sportner.cblmapper.annotations.DocumentField;
import io.sportner.cblmapper.annotations.NestedDocument;
import io.sportner.cblmapper.exceptions.CBLMapperClassException;
import io.sportner.cblmapper.exceptions.NotCBLDocumentException;
import io.sportner.cblmapper.exceptions.UnhandledTypeException;
import io.sportner.cblmapper.exceptions.UnsupportedIDFieldTypeException;
import io.sportner.cblmapper.util.FieldHelper;
import io.sportner.cblmapper.util.Primitives;

/**
 * Created by alblanc on 22/08/2017.
 */

public class CBLMapper {

    public Document toDocument(@NonNull Object object) throws CBLMapperClassException {
        return toDocument(object, findDocumentID(object));
    }

    private Document toDocument(@NonNull Object object, @Nullable String documentID) throws CBLMapperClassException {
        if (object.getClass().getAnnotation(CBLDocument.class) == null) {
            throw new NotCBLDocumentException(object.getClass());
        }
        return new Document(documentID, (Map<String, Object>) encode(object));
    }

    public <T> T fromDocument(@Nullable Document document, @NonNull Class<T> typeOfT) throws CBLMapperClassException {
        if (document == null) {
            return null;
        }

        T object = null;

        object = decode(document.toMap(), typeOfT);

        // Attach ID to object
        if (object != null) {
            for (Field field : FieldHelper.getFieldsUpTo(object.getClass(), Object.class)) {
                DocumentField documentFieldAnnotation = field.getAnnotation(DocumentField.class);
                if (documentFieldAnnotation != null && documentFieldAnnotation.ID()) {
                    if (field.getType() == String.class) {
                        try {
                            if (!field.isAccessible()) {
                                field.setAccessible(true);
                            }
                            field.set(object, document.getId());
                        } catch (IllegalAccessException e) {
                            // Ignore as it can't happen
                        }
                        break;
                    } else {
                        throw new UnsupportedIDFieldTypeException(object.getClass());
                    }
                }
            }
        }

        return object;
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

    private Object encode(@Nullable Object value) throws CBLMapperClassException {
        return encode(value, null);
    }

    private Object encode(@Nullable Object value, @Nullable NestedDocument annotation) throws CBLMapperClassException {

        if (value != null && value.getClass().getAnnotation(CBLDocument.class) != null) {
            return encodeCBLDocument(value, annotation);
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
        } else {
            if (!(value == null ||
                  value == RemovedValue.INSTANCE ||
                  value instanceof String ||
                  value instanceof Number ||
                  value instanceof Boolean ||
                  value instanceof Blob)) {
                throw new UnhandledTypeException(value.getClass());
            }
        }
        return value;
    }

    private <T> T decode(@Nullable Object value, Field field) throws CBLMapperClassException {
        Object result;
        Class typeOfT = field.getType();
        if (List.class.isAssignableFrom(typeOfT)) {
            result = decodeCBLList((List) value, field);
        } else if (value != null && typeOfT.getAnnotation(CBLDocument.class) != null) {
            result = decodeCBLDocument((Map<String, Object>) value, typeOfT);
        } else if (typeOfT.equals(Date.class)) {
            result = (T) typeOfT.cast(DateUtils.fromJson((String) value));
        } else if (value == null || value == RemovedValue.INSTANCE) {
            result = null;
        }
        else if (Primitives.isPrimitive(value.getClass()) ||
            value instanceof String ||
            value instanceof Number ||
            value instanceof Boolean ||
            value instanceof Blob) {
            result = value;
        } else {
            throw new UnhandledTypeException(value.getClass());
        }
        return (T)result;
    }

    private <T> T decode(@Nullable Object value, @NonNull Class<T> typeOfT) throws CBLMapperClassException {
        if (value != null && typeOfT.getAnnotation(CBLDocument.class) != null) {
            return decodeCBLDocument((Map<String, Object>) value, typeOfT);
        }
        if (typeOfT.equals(Date.class)) {
            return typeOfT.cast(DateUtils.fromJson((String) value));
        }
        if (value == null || value == RemovedValue.INSTANCE) {
            return null;
        }
        if (Primitives.isPrimitive(value.getClass()) ||
            value instanceof String ||
            value instanceof Number ||
            value instanceof Boolean ||
            value instanceof Blob) {
            return (T) value;
        }
        throw new UnhandledTypeException(value.getClass());
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
            result.add(decode(item, itemClass));
        }
        return (T) result;
    }

    private <T> T decodeCBLDocument(@Nullable Map<String, Object> value, @NonNull Class<T> typeOfT) throws CBLMapperClassException {
        if (value == null) {
            return null;
        }

        T object = null;
        try {
            object = (T) typeOfT.newInstance();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }

        for (Field field : FieldHelper.getFieldsUpTo(object.getClass(), Object.class)) {
            DocumentField documentFieldAnnotation = field.getAnnotation(DocumentField.class);
            if (documentFieldAnnotation != null && !documentFieldAnnotation.ID()) {
                String fieldName = TextUtils.isEmpty(documentFieldAnnotation.fieldName()) ? field.getName() : documentFieldAnnotation.fieldName();

                // Force access private members
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }

                try {
                    field.set(object, decode(value.get(fieldName), field));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        return object;
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


    private Map<String, Object> encodeCBLDocument(@Nullable Object value) throws CBLMapperClassException {
        return encodeCBLDocument(value, null);
    }

    private Map<String, Object> encodeCBLDocument(@Nullable Object value, @Nullable NestedDocument parentAnnotation) throws CBLMapperClassException {
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
