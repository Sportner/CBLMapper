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

/**
 * Created by alblanc on 22/08/2017.
 */

public class CBLSerializer {

    public Document toDocument(@NonNull Object object) throws CBLMapperClassException {
        return toDocument(object, findDocumentID(object));
    }

    private Document toDocument(@NonNull Object object, @Nullable String documentID) throws CBLMapperClassException {
        if (object.getClass().getAnnotation(CBLDocument.class) == null) {
            throw new NotCBLDocumentException(object.getClass());
        }
        return new Document(documentID, (Map<String, Object>) encode(object));
    }

    public <T> T fromDocument(Document document, Class<T> typeOfT) throws CBLMapperClassException {
        return null;
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
