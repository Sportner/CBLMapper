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
import com.couchbase.lite.MutableDocument;
import com.couchbase.lite.Result;
import com.couchbase.lite.internal.utils.DateUtils;
import io.sportner.cblmapper.annotations.CBLEnumValue;
import io.sportner.cblmapper.annotations.DocumentField;
import io.sportner.cblmapper.annotations.NestedDocument;
import io.sportner.cblmapper.exceptions.CBLMapperClassException;
import io.sportner.cblmapper.exceptions.UnsupportedIDFieldTypeException;
import io.sportner.cblmapper.util.FieldHelper;
import io.sportner.cblmapper.util.Primitives;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.format.DateTimeFormatter;

public class CBLMapper {
    private static CBLMapper INSTANCE;
    private Database mDatabase;

    public CBLMapper() {
    }

    public CBLMapper(Database database) {
        this();
        this.mDatabase = database;
    }

    public static CBLMapper configureDefaultInstance(@Nullable Database db) {
        INSTANCE = new CBLMapper(db);
        return INSTANCE;
    }

    public static CBLMapper getDefaultInstance() {
        if (INSTANCE == null) {
            throw new IllegalStateException("You must configure default instance before using it");
        } else {
            return INSTANCE;
        }
    }

    public Database getDatabase() {
        return this.mDatabase;
    }

    public void save(CBLDocument cblDocument) throws CBLMapperClassException, CouchbaseLiteException {
        if (this.mDatabase == null) {
            throw new IllegalStateException("You must attach a database in order to save documents");
        } else {
            this.mDatabase.save(this.toDocument(cblDocument));
        }
    }

    public MutableDocument toDocument(@NonNull CBLDocument object) throws CBLMapperClassException {
        Document doc = object.getDocument();
        if (doc == null && this.mDatabase != null) {
            doc = this.mDatabase.getDocument(object.getDocumentID());
        }

        MutableDocument mutableDocument;
        if (doc == null) {
            mutableDocument = new MutableDocument(object.getDocumentID());
        } else {
            mutableDocument = doc.toMutable();
        }

        mutableDocument.setData((Map)this.encode(object, true));
        return mutableDocument;
    }

    public <T extends CBLDocument> void load(T instanceOfT) throws CBLMapperClassException {
        Document doc = instanceOfT.getDocument();
        if (doc == null) {
            doc = this.mDatabase.getDocument(instanceOfT.getDocumentID());
            instanceOfT.setDocument(doc);
        }

        if (doc != null) {
            this.decodeObject(doc.toMap(), (Object)instanceOfT);
        }

    }

    public <T extends CBLDocument> T load(@NonNull String docID, @NonNull Class<T> typeOfT) throws CBLMapperClassException {
        return this.fromDocument(this.mDatabase.getDocument(docID), typeOfT);
    }

    public <T extends CBLDocument> T fromDocument(@Nullable Document dictionary, @NonNull Class<T> typeOfT) throws CBLMapperClassException {
        if (dictionary == null) {
            return null;
        } else {
            T object = null;
            object = (T)this.decode(dictionary.toMap(), typeOfT, true, (NestedDocument)null);
            if (object != null && dictionary instanceof Document) {
                object.setDocument(dictionary);
                this.setCBLDocumentID(object, dictionary.getId());
            }

            return object;
        }
    }

    public <T> T fromDocument(@Nullable Dictionary dictionary, @NonNull Class<T> typeOfT) throws CBLMapperClassException {
        return dictionary != null ? (T)this.decode(dictionary.toMap(), typeOfT, true, (NestedDocument)null) : null;
    }

    public <T> T fromDocument(@Nullable Result dictionary, @NonNull Class<T> typeOfT) throws CBLMapperClassException {
        return dictionary != null ? (T)this.decode(dictionary.toMap(), typeOfT, true, (NestedDocument)null) : null;
    }

    private void setCBLDocumentID(@NonNull CBLDocument cblDocument, @NonNull String docID) {
        Iterator var3 = FieldHelper.getFieldsUpTo(cblDocument.getClass(), Object.class).iterator();

        while(var3.hasNext()) {
            Field field = (Field)var3.next();
            DocumentField documentFieldAnnotation = (DocumentField)field.getAnnotation(DocumentField.class);
            if (documentFieldAnnotation != null && documentFieldAnnotation.ID()) {
                if (field.getType() != String.class) {
                    throw new UnsupportedIDFieldTypeException(cblDocument.getClass());
                }

                try {
                    if (!field.isAccessible()) {
                        field.setAccessible(true);
                    }

                    field.set(cblDocument, docID);
                } catch (IllegalAccessException var7) {
                    ;
                }
                break;
            }
        }

    }

    private Object encode(@Nullable Object value, boolean isRoot) throws CBLMapperClassException {
        return this.encode(value, (NestedDocument)null, isRoot);
    }

    private Object encode(@Nullable Object value, @Nullable NestedDocument annotation) {
        return this.encode(value, annotation, false);
    }

    private Object encode(@Nullable Object value, @Nullable NestedDocument annotation, boolean isRoot) throws CBLMapperClassException {
        if (value != null && !(value instanceof String) && !(value instanceof Number) && !(value instanceof Boolean) && !(value instanceof Blob)) {
            if (value instanceof CBLDocument && !isRoot && annotation == null) {
                return ((CBLDocument)value).getDocumentID();
            } else if (value != null && value.getClass().isEnum()) {
                return this.encodeEnumValue((Enum)value);
            } else if (value instanceof Dictionary) {
                return value;
            } else if (value instanceof Array) {
                return value;
            } else if (value instanceof Map) {
                return this.encodeMap((Map)value, annotation);
            } else if (value instanceof List) {
                return this.encodeList((List)value, annotation);
            } else if (value instanceof Date) {
                return DateUtils.toJson((Date)value);
            } else {
                return value instanceof ZonedDateTime ? ((ZonedDateTime)value).format(DateTimeFormatter.ISO_ZONED_DATE_TIME) : this.encodeObject(value, annotation);
            }
        } else {
            return value;
        }
    }

    private <T extends Enum<T>> String encodeEnumValue(T enumValue) {
        try {
            Field field = enumValue.getClass().getField(enumValue.name());
            CBLEnumValue annotation = (CBLEnumValue)field.getAnnotation(CBLEnumValue.class);
            return annotation == null ? enumValue.name() : annotation.value();
        } catch (NoSuchFieldException var4) {
            return enumValue.name();
        }
    }

    private <T> T decode(@Nullable Object value, Field field) throws CBLMapperClassException {
        Class typeOfT = field.getType();
        Object result;
        if (value == null) {
            result = null;
        } else if (List.class.isAssignableFrom(typeOfT)) {
            result = this.decodeCBLList((List)value, field);
        } else if (CBLDocument.class.isAssignableFrom(typeOfT)) {
            NestedDocument nestedAnnotation = (NestedDocument)field.getAnnotation(NestedDocument.class);
            if (nestedAnnotation != null) {
                result = this.decodeObject((Map)value, typeOfT);
            } else {
                result = this.instanciateObject(typeOfT);
                ((CBLDocument)result).setDocumentID((String)value);
            }

            ((CBLDocument)result).setCBLMapper(this);
        } else if (Enum.class.isAssignableFrom(typeOfT)) {
            result = this.decodeEnum((String)value, typeOfT);
        } else if (typeOfT.equals(Date.class)) {
            result = typeOfT.cast(DateUtils.fromJson((String)value));
        } else if (typeOfT.equals(ZonedDateTime.class)) {
            result = ZonedDateTime.parse((String)value, DateTimeFormatter.ISO_ZONED_DATE_TIME);
        } else if (!Primitives.isPrimitive(typeOfT) && !(value instanceof String) && !(value instanceof Number) && !(value instanceof Boolean) && !(value instanceof Blob)) {
            result = this.decodeObject((Map)value, typeOfT);
        } else {
            result = value;
        }

        return (T)result;
    }

    private Enum decodeEnum(String value, Class<Enum> fieldType) {
        Enum[] var3 = (Enum[])fieldType.getEnumConstants();
        int var4 = var3.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            Enum enumValue = var3[var5];

            try {
                CBLEnumValue enumAnnotation = (CBLEnumValue)fieldType.getField(enumValue.name()).getAnnotation(CBLEnumValue.class);
                if (enumAnnotation != null) {
                    if (enumAnnotation.value().equals(value)) {
                        return enumValue;
                    }
                } else if (enumValue.name().equals(value)) {
                    return enumValue;
                }
            } catch (NoSuchFieldException var8) {
                var8.printStackTrace();
            }
        }

        return null;
    }

    private <T> T decode(@Nullable Object value, @NonNull Class typeOfT, boolean isRoot, @Nullable NestedDocument nestedAnnotation) throws CBLMapperClassException {
        Object result;
        if (value == null) {
            result = null;
        } else if (CBLDocument.class.isAssignableFrom(typeOfT)) {
            if (!isRoot && nestedAnnotation == null) {
                result = this.instanciateObject(typeOfT);
                ((CBLDocument)result).setDocumentID((String)value);
            } else {
                result = this.decodeObject((Map)value, typeOfT);
            }

            ((CBLDocument)result).setCBLMapper(this);
        } else if (Enum.class.isAssignableFrom(typeOfT)) {
            result = this.decodeEnum((String)value, typeOfT);
        } else if (typeOfT.equals(Date.class)) {
            result = typeOfT.cast(DateUtils.fromJson((String)value));
        } else if (!Primitives.isPrimitive(typeOfT) && !(value instanceof String) && !(value instanceof Number) && !(value instanceof Boolean) && !(value instanceof Blob)) {
            result = this.decodeObject((Map)value, typeOfT);
        } else {
            result = value;
        }

        return (T)result;
    }

    private <T> T decodeCBLList(List value, Field typeOfT) throws CBLMapperClassException {
        ParameterizedType parameterizedType = (ParameterizedType)typeOfT.getGenericType();
        Class<?> itemClass = (Class)parameterizedType.getActualTypeArguments()[0];
        List result = null;
        if (typeOfT.getType() == List.class) {
            result = new ArrayList();
        } else {
            try {
                result = (List)typeOfT.getType().newInstance();
            } catch (InstantiationException var8) {
                var8.printStackTrace();
            } catch (IllegalAccessException var9) {
                var9.printStackTrace();
            }
        }

        Iterator var6 = value.iterator();

        while(var6.hasNext()) {
            Object item = var6.next();
            ((List)result).add(this.decode(item, itemClass, false, (NestedDocument)typeOfT.getAnnotation(NestedDocument.class)));
        }

        return (T)result;
    }

    private Object instanciateObject(@NonNull Class typeOfT) {
        Object object = null;

        try {
            object = typeOfT.newInstance();
            return object;
        } catch (IllegalAccessException var4) {
            var4.printStackTrace();
            throw new CBLMapperClassException(typeOfT, "Verify constructor with no argument is public");
        } catch (InstantiationException var5) {
            var5.printStackTrace();
            throw new CBLMapperClassException(typeOfT, "Type might be an abstract class, an interface, an array class, a primitive type, or void. Verify class contains a public constructor with no argument");
        }
    }

    private <T> T decodeObject(@Nullable Map<String, Object> value, @NonNull Class<T> typeOfT) throws CBLMapperClassException {
        T object = (T)this.instanciateObject(typeOfT);
        return this.decodeObject(value, object);
    }

    private <T> T decodeObject(@Nullable Map<String, Object> value, @NonNull T instanceOfT) throws CBLMapperClassException {
        if (value == null) {
            return null;
        } else {
            Iterator var3 = FieldHelper.getFieldsUpTo(instanceOfT.getClass(), Object.class).iterator();

            while(var3.hasNext()) {
                Field field = (Field)var3.next();
                DocumentField documentFieldAnnotation = (DocumentField)field.getAnnotation(DocumentField.class);
                if (documentFieldAnnotation != null) {
                    String fieldName = TextUtils.isEmpty(documentFieldAnnotation.fieldName()) ? field.getName() : documentFieldAnnotation.fieldName();
                    boolean isPrivate = !field.isAccessible();
                    if (isPrivate) {
                        field.setAccessible(true);
                    }

                    try {
                        field.set(instanceOfT, this.decode(value.get(fieldName), field));
                    } catch (IllegalAccessException var9) {
                        var9.printStackTrace();
                    } catch (IllegalArgumentException var10) {
                        var10.printStackTrace();
                    }

                    if (isPrivate) {
                        field.setAccessible(false);
                    }
                }
            }

            return instanceOfT;
        }
    }

    private Object encodeList(@Nullable List<Object> value, @Nullable NestedDocument annotation) throws CBLMapperClassException {
        if (value == null) {
            return null;
        } else {
            List<Object> encodedValue = new ArrayList();
            Iterator var4 = value.iterator();

            while(var4.hasNext()) {
                Object entry = var4.next();
                encodedValue.add(this.encode(entry, annotation));
            }

            return encodedValue;
        }
    }

    private Object encodeMap(@Nullable Map<String, Object> value, @Nullable NestedDocument annotation) throws CBLMapperClassException {
        if (value == null) {
            return null;
        } else {
            Map<String, Object> encodedValue = new HashMap();
            Iterator var4 = value.entrySet().iterator();

            while(var4.hasNext()) {
                Entry<String, Object> entry = (Entry)var4.next();
                encodedValue.put(entry.getKey(), this.encode(entry.getValue(), annotation));
            }

            return encodedValue;
        }
    }

    private Map<String, Object> encodeObject(@Nullable Object value) throws CBLMapperClassException {
        return this.encodeObject(value, (NestedDocument)null);
    }

    private Map<String, Object> encodeObject(@Nullable Object value, @Nullable NestedDocument parentAnnotation) throws CBLMapperClassException {
        if (value == null) {
            return null;
        } else {
            if (parentAnnotation != null) {
                Arrays.sort(parentAnnotation.omitFields());
            }

            Map<String, Object> map = new HashMap();
            Iterator var4 = FieldHelper.getFieldsUpTo(value.getClass(), Object.class).iterator();

            while(true) {
                Field field;
                DocumentField documentFieldAnnotation;
                String fieldName;
                do {
                    do {
                        if (!var4.hasNext()) {
                            return map;
                        }

                        field = (Field)var4.next();
                        documentFieldAnnotation = (DocumentField)field.getAnnotation(DocumentField.class);
                    } while(documentFieldAnnotation == null);

                    fieldName = TextUtils.isEmpty(documentFieldAnnotation.fieldName()) ? field.getName() : documentFieldAnnotation.fieldName();
                } while(parentAnnotation != null && Arrays.binarySearch(parentAnnotation.omitFields(), documentFieldAnnotation.fieldName()) >= 0);

                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }

                try {
                    map.put(fieldName, this.encode(field.get(value), (NestedDocument)field.getAnnotation(NestedDocument.class)));
                } catch (IllegalAccessException var9) {
                    var9.printStackTrace();
                }
            }
        }
    }
}
