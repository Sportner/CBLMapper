package io.sportner.cblmapper;

import android.support.test.runner.AndroidJUnit4;

import com.couchbase.lite.Array;
import com.couchbase.lite.Blob;
import com.couchbase.lite.Dictionary;
import com.couchbase.lite.Document;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.sportner.cblmapper.annotations.CBLEnumValue;
import io.sportner.cblmapper.annotations.DocumentField;
import io.sportner.cblmapper.annotations.NestedDocument;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Created by alblanc on 19/08/2017.
 */
// TODO: Swap all `assertEquals` parameters
@RunWith(AndroidJUnit4.class)
public class CBLMapperTest {

    public static class Car extends CBLDocument {

        public static final String FIELD_WHEELS = "wheels";

        @DocumentField(fieldName = FIELD_WHEELS)
        int wheels;

        public int getWheels() {
            return wheels;
        }

        public void setWheels(int wheels) {
            this.wheels = wheels;
        }
    }

    public static class SimplePet extends CBLDocument {

        public static final String FIELD_NAME = "name";

        @DocumentField(ID = true)
        private String ID;

        @DocumentField(fieldName = FIELD_NAME)
        private String name;

        public String getID() {
            return ID;
        }

        public void setID(String ID) {
            this.ID = ID;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @Test
    public void testIDSerialization() throws Exception {
        SimplePet cat = new SimplePet();
        final String PET_ID = "Nina";
        cat.setID(PET_ID);

        CBLMapper mapper = new CBLMapper();
        Document catDocument = mapper.toDocument(cat);

        assertNotNull(catDocument);
        assertEquals(catDocument.getId(), PET_ID);
    }

    @Test
    public void testStringFieldSerialization() throws Exception {
        SimplePet cat = new SimplePet();
        final String PET_NAME = "Nina";
        cat.setName(PET_NAME);

        CBLMapper mapper = new CBLMapper();
        Document catDocument = mapper.toDocument(cat);

        assertNotNull(catDocument);
        assertEquals(catDocument.getString(SimplePet.FIELD_NAME), PET_NAME);
    }

    @Test
    public void testIntFieldSerialization() throws Exception {
        Car car = new Car();
        final int CAR_WHEELS = 2;
        car.setWheels(CAR_WHEELS);

        CBLMapper mapper = new CBLMapper();
        Document document = mapper.toDocument(car);

        assertNotNull(document);
        assertEquals(document.getInt(Car.FIELD_WHEELS), CAR_WHEELS);
    }

    @Test
    public void testBasicDocumentFieldTypesSerialization() throws Exception {
        final Blob aBlob = new Blob("text/plain", "Byte array test".getBytes());
        final boolean aBoolean = true;
        final Date aDate = new Date();
        final double aDouble = 4.456;
        final float aFloat = 34.34f;
        final int anInt = 15;
        final long aLong = 23456345;
        final Number aNumber = 4567;
        final String aString = "Test string";

        BasicTypes basicTypes = new BasicTypes(aBlob,
                                               aBoolean,
                                               aDate,
                                               aDouble,
                                               aFloat,
                                               anInt,
                                               aLong,
                                               aNumber,
                                               aString);
        CBLMapper mapper = new CBLMapper();
        Document document = mapper.toDocument(basicTypes);

        assertEquals(document.getBlob(BasicTypes.FIELD_BLOB), aBlob);
        assertEquals(document.getBoolean(BasicTypes.FIELD_BOOLEAN), aBoolean);
        assertEquals(document.getDate(BasicTypes.FIELD_DATE), aDate);
        assertEquals(document.getDouble(BasicTypes.FIELD_DOUBLE), aDouble, 0.1);
        assertEquals(document.getFloat(BasicTypes.FIELD_FLOAT), aFloat, 0.1f);
        assertEquals(document.getInt(BasicTypes.FIELD_INTEGER), anInt);
        assertEquals(document.getLong(BasicTypes.FIELD_LONG), aLong);
        assertEquals(document.getNumber(BasicTypes.FIELD_NUMBER), aNumber);
        assertEquals(document.getString(BasicTypes.FIELD_STRING), aString);
    }

    @Test
    public void testNestedTypes() throws Exception {
        final String aNestedID = "23456789";

        final Blob aBlob = new Blob("text/plain", "Byte array test".getBytes());
        final boolean aBoolean = true;
        final Date aDate = new Date();
        final double aDouble = 4.456;
        final float aFloat = 34.34f;
        final int anInt = 15;
        final long aLong = 23456345;
        final Number aNumber = 4567;
        final String aString = "Test string";

        final BasicTypes basicTypes = new BasicTypes(aBlob,
                                                     aBoolean,
                                                     aDate,
                                                     aDouble,
                                                     aFloat,
                                                     anInt,
                                                     aLong,
                                                     aNumber,
                                                     aString);

        final NestedType nestedType = new NestedType(aNestedID, basicTypes);
        CBLMapper mapper = new CBLMapper();
        Document document = mapper.toDocument(nestedType);

        assertEquals(document.getId(), aNestedID);

        Dictionary dictionary = document.getDictionary(NestedType.FIELD_BASIC_TYPES);

        assertEquals(dictionary.getBlob(BasicTypes.FIELD_BLOB), aBlob);
        assertEquals(dictionary.getBoolean(BasicTypes.FIELD_BOOLEAN), aBoolean);
        assertEquals(dictionary.getDate(BasicTypes.FIELD_DATE), aDate);
        assertEquals(dictionary.getDouble(BasicTypes.FIELD_DOUBLE), aDouble, 0.1);
        assertEquals(dictionary.getFloat(BasicTypes.FIELD_FLOAT), aFloat, 0.1f);
        assertEquals(dictionary.getInt(BasicTypes.FIELD_INTEGER), anInt);
        assertEquals(dictionary.getLong(BasicTypes.FIELD_LONG), aLong);
        assertEquals(dictionary.getNumber(BasicTypes.FIELD_NUMBER), aNumber);
        assertEquals(dictionary.getString(BasicTypes.FIELD_STRING), aString);

    }

    @Test
    public void testOmitNestedTypeField() throws Exception {
        final String aNestedID = "23456789";

        final Blob aBlob = new Blob("text/plain", "Byte array test".getBytes());
        final boolean aBoolean = true;
        final Date aDate = new Date();
        final double aDouble = 4.456;
        final float aFloat = 34.34f;
        final int anInt = 15;
        final long aLong = 23456345;
        final Number aNumber = 4567;
        final String aString = "Test string";

        final BasicTypes basicTypes = new BasicTypes(aBlob,
                                                     aBoolean,
                                                     aDate,
                                                     aDouble,
                                                     aFloat,
                                                     anInt,
                                                     aLong,
                                                     aNumber,
                                                     aString);

        final BasicTypes basicTypes2 = new BasicTypes(aBlob,
                                                      aBoolean,
                                                      aDate,
                                                      aDouble,
                                                      aFloat,
                                                      anInt,
                                                      aLong,
                                                      aNumber,
                                                      aString);

        final OmitNestedTypeFields nestedType = new OmitNestedTypeFields(aNestedID, basicTypes, basicTypes2);
        CBLMapper mapper = new CBLMapper();
        Document document = mapper.toDocument(nestedType);

        assertEquals(document.getId(), aNestedID);

        Dictionary dictionary = document.getDictionary(OmitNestedTypeFields.FIELD_BASIC_TYPES_2);

        assertEquals(dictionary.getBlob(BasicTypes.FIELD_BLOB), aBlob);
        assertEquals(dictionary.getBoolean(BasicTypes.FIELD_BOOLEAN), aBoolean);
        assertNull(dictionary.getDate(BasicTypes.FIELD_DATE));
        assertEquals(dictionary.getDouble(BasicTypes.FIELD_DOUBLE), aDouble, 0.1);
        assertEquals(dictionary.getFloat(BasicTypes.FIELD_FLOAT), aFloat, 0.1f);
        assertEquals(dictionary.getInt(BasicTypes.FIELD_INTEGER), 0);
        assertEquals(dictionary.getLong(BasicTypes.FIELD_LONG), aLong);
        assertEquals(dictionary.getNumber(BasicTypes.FIELD_NUMBER), aNumber);
        assertEquals(dictionary.getString(BasicTypes.FIELD_STRING), aString);
    }

    @Test
    public void testOmitFieldName() throws Exception {
        final String customValue = "value";
        final OmitFieldName omitFieldName = new OmitFieldName(customValue);

        CBLMapper documentMapper = new CBLMapper();
        Document document = documentMapper.toDocument(omitFieldName);

        assertEquals(customValue, document.getString("mCustomField"));
    }

    @Test
    public void testUnserializeBasicTypes() throws Exception {
        Document document = new Document();

        final Blob aBlob = new Blob("text/plain", "Byte array test".getBytes());
        final boolean aBoolean = true;
        final Date aDate = new Date();
        final double aDouble = 4.456;
        final float aFloat = 34.34f;
        final int anInt = 15;
        final long aLong = 23456345;
        final Number aNumber = 4567;
        final String aString = "Test string";

        document.setBlob(BasicTypes.FIELD_BLOB, aBlob);
        document.setBoolean(BasicTypes.FIELD_BOOLEAN, aBoolean);
        document.setDate(BasicTypes.FIELD_DATE, aDate);
        document.setDouble(BasicTypes.FIELD_DOUBLE, aDouble);
        document.setFloat(BasicTypes.FIELD_FLOAT, aFloat);
        document.setInt(BasicTypes.FIELD_INTEGER, anInt);
        document.setLong(BasicTypes.FIELD_LONG, aLong);
        document.setNumber(BasicTypes.FIELD_NUMBER, aNumber);
        document.setString(BasicTypes.FIELD_STRING, aString);

        CBLMapper documentMapper = new CBLMapper();
        BasicTypes basicTypes = documentMapper.fromDocument(document, BasicTypes.class);

        assertNotNull(basicTypes);
        assertEquals(aBlob, basicTypes.getBlob());
    }

    @Test
    public void testUnserializeID() throws Exception {
        final String id = "testID";
        Document document = new Document(id);

        CBLMapper documentMapper = new CBLMapper();
        SimpleModelWithID simpleModelWithID = documentMapper.fromDocument(document, SimpleModelWithID.class);

        assertNotNull(simpleModelWithID);
        assertEquals(id, simpleModelWithID.getID());
    }

    @Test
    public void testUnserializedInheritedClass() throws Exception {
        final String id = "testID";
        final String name = "Julie";
        final int age = 27;

        Document document = new Document(id);
        document.setString(ChildClass.FIELD_NAME, name);
        document.setInt(ChildClass.FIELD_AGE, age);

        CBLMapper documentMapper = new CBLMapper();
        ChildClass simpleModelWithID = documentMapper.fromDocument(document, ChildClass.class);

        assertNotNull(simpleModelWithID);
        assertEquals(id, simpleModelWithID.getID());
        assertEquals(name, simpleModelWithID.getName());
        assertEquals(age, simpleModelWithID.getAge());
    }

    @Test
    public void testUnserializeNestedTypes() throws Exception {
        final String aNestedID = "23456789";

        final Blob aBlob = new Blob("text/plain", "Byte array test".getBytes());
        final boolean aBoolean = true;
        final Date aDate = new Date();
        final double aDouble = 4.456;
        final float aFloat = 34.34f;
        final int anInt = 15;
        final long aLong = 23456345;
        final Number aNumber = 4567;
        final String aString = "Test string";

        Dictionary nestedDic = new Dictionary();
        nestedDic.setBlob(BasicTypes.FIELD_BLOB, aBlob);
        nestedDic.setBoolean(BasicTypes.FIELD_BOOLEAN, aBoolean);
        nestedDic.setDate(BasicTypes.FIELD_DATE, aDate);
        nestedDic.setDouble(BasicTypes.FIELD_DOUBLE, aDouble);
        nestedDic.setFloat(BasicTypes.FIELD_FLOAT, aFloat);
        nestedDic.setInt(BasicTypes.FIELD_INTEGER, anInt);
        nestedDic.setLong(BasicTypes.FIELD_LONG, aLong);
        nestedDic.setNumber(BasicTypes.FIELD_NUMBER, aNumber);
        nestedDic.setString(BasicTypes.FIELD_STRING, aString);

        Document document = new Document(aNestedID);
        document.setDictionary(NestedType.FIELD_BASIC_TYPES, nestedDic);

        CBLMapper documentMapper = new CBLMapper();
        NestedType nestedDocument = documentMapper.fromDocument(document, NestedType.class);

        assertNotNull(nestedDocument);
        assertNotNull(nestedDocument.getBasicTypes());

        BasicTypes basicTypes = nestedDocument.getBasicTypes();

        assertEquals(aBlob, basicTypes.getBlob());
        assertEquals(aBoolean, basicTypes.getBoolean());
        assertEquals(aDate, basicTypes.getDate());
        assertEquals(aDouble, basicTypes.getDouble(), 0.1);
        assertEquals(aFloat, basicTypes.getFloat(), 0.1f);
        assertEquals(anInt, basicTypes.getInt());
        assertEquals(aLong, basicTypes.getLong());
        assertEquals(aNumber, basicTypes.getNumber());
        assertEquals(aString, basicTypes.getString());
    }

    @Test
    public void testEnum() throws Exception {
        Animal animal = new Animal();
        animal.mSpecy = Species.Mouse;

        CBLMapper cblMapper = new CBLMapper();
        Document document = cblMapper.toDocument(animal);

        assertEquals("mouse", document.getString(Animal.FIELD_SPECY));

        Animal animal2 = cblMapper.fromDocument(document, Animal.class);

        assertEquals(Species.Mouse.name(), animal2.mSpecy.name());
    }

    @Test
    public void testUnserializeArrayList() throws Exception {
        final Blob aBlob = new Blob("text/plain", "Byte array test".getBytes());
        final boolean aBoolean = true;
        final Date aDate = new Date();
        final double aDouble = 4.456;
        final float aFloat = 34.34f;
        final int anInt = 15;
        final long aLong = 23456345;
        final Number aNumber = 4567;
        final String aString = "Test string";

        Dictionary nestedDic = new Dictionary();
        nestedDic.setBlob(BasicTypes.FIELD_BLOB, aBlob);
        nestedDic.setBoolean(BasicTypes.FIELD_BOOLEAN, aBoolean);
        nestedDic.setDate(BasicTypes.FIELD_DATE, aDate);
        nestedDic.setDouble(BasicTypes.FIELD_DOUBLE, aDouble);
        nestedDic.setFloat(BasicTypes.FIELD_FLOAT, aFloat);
        nestedDic.setInt(BasicTypes.FIELD_INTEGER, anInt);
        nestedDic.setLong(BasicTypes.FIELD_LONG, aLong);
        nestedDic.setNumber(BasicTypes.FIELD_NUMBER, aNumber);
        nestedDic.setString(BasicTypes.FIELD_STRING, aString);

        Document document = new Document();
        Array array = new Array();
        array.addDictionary(nestedDic);

        document.setArray(ListMemberClass.FIELD_LIST, array);

        ListMemberClass listMemberClass = new CBLMapper().fromDocument(document, ListMemberClass.class);

        assertNotNull(listMemberClass);
        List<BasicTypes> basicTypesList = listMemberClass.getBasicTypesList();

        assertNotNull(basicTypesList);
        assertEquals(1, basicTypesList.size());

        Date date = basicTypesList.get(0).getDate();
        assertEquals(aDate, date);
    }

    public static class BasicTypes extends CBLDocument {

        public static final String FIELD_BLOB = "blob";
        public static final String FIELD_BOOLEAN = "boolean";
        public static final String FIELD_DATE = "date";
        public static final String FIELD_DOUBLE = "double";
        public static final String FIELD_FLOAT = "float";
        public static final String FIELD_INTEGER = "integer";
        public static final String FIELD_LONG = "long";
        public static final String FIELD_NUMBER = "number";
        public static final String FIELD_STRING = "string";

        @DocumentField(fieldName = FIELD_BLOB)
        Blob mBlob;

        @DocumentField(fieldName = FIELD_BOOLEAN)
        boolean mBoolean;

        @DocumentField(fieldName = FIELD_DATE)
        Date mDate;

        @DocumentField(fieldName = FIELD_DOUBLE)
        double mDouble;

        @DocumentField(fieldName = FIELD_FLOAT)
        float mFloat;

        @DocumentField(fieldName = FIELD_INTEGER)
        int mInt;

        @DocumentField(fieldName = FIELD_LONG)
        long mLong;

        @DocumentField(fieldName = FIELD_NUMBER)
        Number mNumber;

        @DocumentField(fieldName = FIELD_STRING)
        String mString;

        public BasicTypes() {
        }

        public BasicTypes(Blob aBlob,
                          boolean aBoolean,
                          Date aDate,
                          double aDouble,
                          float aFloat,
                          int anInt,
                          long aLong,
                          Number aNumber,
                          String aString) {
            mBlob = aBlob;
            mBoolean = aBoolean;
            mDate = aDate;
            mDouble = aDouble;
            mFloat = aFloat;
            mInt = anInt;
            mLong = aLong;
            mNumber = aNumber;
            mString = aString;
        }

        public Blob getBlob() {
            return mBlob;
        }

        public boolean isBoolean() {
            return mBoolean;
        }

        public Date getDate() {
            return mDate;
        }

        public double getDouble() {
            return mDouble;
        }

        public float getFloat() {
            return mFloat;
        }

        public int getInt() {
            return mInt;
        }

        public long getLong() {
            return mLong;
        }

        public Number getNumber() {
            return mNumber;
        }

        public String getString() {
            return mString;
        }

        public boolean getBoolean() {
            return mBoolean;
        }
    }

    public static class NestedType extends CBLDocument {

        public static final String FIELD_ID = "id";
        public static final String FIELD_BASIC_TYPES = "basics";

        @DocumentField(fieldName = FIELD_ID, ID = true)
        String mID;

        @NestedDocument
        @DocumentField(fieldName = FIELD_BASIC_TYPES)
        BasicTypes mBasicTypes;

        public NestedType() {}

        public NestedType(String id, BasicTypes aBasics) {
            mID = id;
            mBasicTypes = aBasics;
        }

        public String getID() {
            return mID;
        }

        public BasicTypes getBasicTypes() {
            return mBasicTypes;
        }
    }

    public static class OmitFieldName extends CBLDocument {

        @DocumentField()
        String mCustomField;

        public OmitFieldName(String customField) {
            mCustomField = customField;
        }

        public String getCustomField() {
            return mCustomField;
        }
    }

    public static class SimpleModelWithID extends CBLDocument {

        public static final String FIELD_NAME = "name";

        @DocumentField(ID = true)
        String mID;

        @DocumentField(fieldName = FIELD_NAME)
        String mName;

        public SimpleModelWithID() {
        }

        public String getID() {
            return mID;
        }

        public String getName() {
            return mName;
        }
    }

    public static class ChildClass extends SimpleModelWithID {

        public static final String FIELD_AGE = "age";

        @DocumentField(fieldName = FIELD_AGE)
        int mAge;

        public int getAge() {
            return mAge;
        }
    }

    public static class ListMemberClass extends CBLDocument {

        private static final String FIELD_LIST = "lists";

        @DocumentField(fieldName = FIELD_LIST)
        ArrayList<BasicTypes> mBasicTypesList;

        public ArrayList<BasicTypes> getBasicTypesList() {
            return mBasicTypesList;
        }
    }

    public class OmitNestedTypeFields extends CBLDocument {

        public static final String FIELD_ID = "id";
        public static final String FIELD_BASIC_TYPES = "basics";
        public static final String FIELD_BASIC_TYPES_2 = "basics_2";

        @DocumentField(fieldName = FIELD_ID, ID = true)
        String mID;

        @DocumentField(fieldName = FIELD_BASIC_TYPES)
        BasicTypes mBasicTypes;

        @NestedDocument(omitFields = {BasicTypes.FIELD_DATE, BasicTypes.FIELD_INTEGER})
        @DocumentField(fieldName = FIELD_BASIC_TYPES_2)
        BasicTypes mBasicTypes2;

        public OmitNestedTypeFields(String id, BasicTypes aBasics, BasicTypes aBasics2) {
            mID = id;
            mBasicTypes = aBasics;
            mBasicTypes2 = aBasics2;
        }

        public String getID() {
            return mID;
        }

        public BasicTypes getBasicTypes() {
            return mBasicTypes;
        }

        public BasicTypes getBasicTypes2() {
            return mBasicTypes2;
        }
    }

    public enum Species {
        @CBLEnumValue("dog")
        Dog,
        @CBLEnumValue("cat")
        Cat,
        @CBLEnumValue("mouse")
        Mouse;
    }

    public static class Animal extends CBLDocument {

        public static final String FIELD_SPECY = "specy";

        @DocumentField(fieldName = FIELD_SPECY)
        public Species mSpecy;
    }

}