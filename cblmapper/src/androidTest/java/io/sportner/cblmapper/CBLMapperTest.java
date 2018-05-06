package io.sportner.cblmapper;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.couchbase.lite.Array;
import com.couchbase.lite.Blob;
import com.couchbase.lite.Dictionary;
import com.couchbase.lite.Document;
import com.couchbase.lite.MutableArray;
import com.couchbase.lite.MutableDictionary;
import com.couchbase.lite.MutableDocument;
import com.jakewharton.threetenabp.AndroidThreeTen;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.format.DateTimeFormatter;

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

    @Before
    public void setup() {
        Context instrumentationCtx = InstrumentationRegistry.getContext();
        AndroidThreeTen.init(instrumentationCtx);
    }

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
        cat.setDocumentID(PET_ID);

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
        assertEquals(PET_NAME, catDocument.getString(SimplePet.FIELD_NAME));
    }

    @Test
    public void testIntFieldSerialization() throws Exception {
        Car car = new Car();
        final int CAR_WHEELS = 2;
        car.setWheels(CAR_WHEELS);

        CBLMapper mapper = new CBLMapper();
        Document document = mapper.toDocument(car);

        assertNotNull(document);
        assertEquals(CAR_WHEELS, document.getInt(Car.FIELD_WHEELS));
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
        final ZonedDateTime zonedDateTime = ZonedDateTime.parse("2017-03-28T12:25:38.492+05:30[Asia/Calcutta]");

        BasicTypes basicTypes = new BasicTypes(aBlob,
                                               aBoolean,
                                               aDate,
                                               aDouble,
                                               aFloat,
                                               anInt,
                                               aLong,
                                               aNumber,
                                               aString,
                                               zonedDateTime);
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
        final ZonedDateTime zonedDateTime = ZonedDateTime.parse("2017-03-28T12:25:38.492+05:30[Asia/Calcutta]");

        final BasicTypes basicTypes = new BasicTypes(aBlob,
                                                     aBoolean,
                                                     aDate,
                                                     aDouble,
                                                     aFloat,
                                                     anInt,
                                                     aLong,
                                                     aNumber,
                                                     aString,
                                                     zonedDateTime);

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
        final ZonedDateTime zonedDateTime = ZonedDateTime.parse("2017-03-28T12:25:38.492+05:30[Asia/Calcutta]");

        final BasicTypes basicTypes = new BasicTypes(aBlob,
                                                     aBoolean,
                                                     aDate,
                                                     aDouble,
                                                     aFloat,
                                                     anInt,
                                                     aLong,
                                                     aNumber,
                                                     aString,
                                                     zonedDateTime);

        final BasicTypes basicTypes2 = new BasicTypes(aBlob,
                                                      aBoolean,
                                                      aDate,
                                                      aDouble,
                                                      aFloat,
                                                      anInt,
                                                      aLong,
                                                      aNumber,
                                                      aString,
                                                      zonedDateTime);

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
        assertEquals(dictionary.getString(BasicTypes.FIELD_ZONED_DATE), zonedDateTime.format(DateTimeFormatter.ISO_ZONED_DATE_TIME));
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
        MutableDocument document = new MutableDocument();

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
        MutableDocument document = new MutableDocument(id);

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

        MutableDocument document = new MutableDocument(id);
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

        MutableDictionary nestedDic = new MutableDictionary();
        nestedDic.setBlob(BasicTypes.FIELD_BLOB, aBlob);
        nestedDic.setBoolean(BasicTypes.FIELD_BOOLEAN, aBoolean);
        nestedDic.setDate(BasicTypes.FIELD_DATE, aDate);
        nestedDic.setDouble(BasicTypes.FIELD_DOUBLE, aDouble);
        nestedDic.setFloat(BasicTypes.FIELD_FLOAT, aFloat);
        nestedDic.setInt(BasicTypes.FIELD_INTEGER, anInt);
        nestedDic.setLong(BasicTypes.FIELD_LONG, aLong);
        nestedDic.setNumber(BasicTypes.FIELD_NUMBER, aNumber);
        nestedDic.setString(BasicTypes.FIELD_STRING, aString);

        MutableDocument document = new MutableDocument(aNestedID);
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

        MutableDictionary nestedDic = new MutableDictionary();
        nestedDic.setBlob(BasicTypes.FIELD_BLOB, aBlob);
        nestedDic.setBoolean(BasicTypes.FIELD_BOOLEAN, aBoolean);
        nestedDic.setDate(BasicTypes.FIELD_DATE, aDate);
        nestedDic.setDouble(BasicTypes.FIELD_DOUBLE, aDouble);
        nestedDic.setFloat(BasicTypes.FIELD_FLOAT, aFloat);
        nestedDic.setInt(BasicTypes.FIELD_INTEGER, anInt);
        nestedDic.setLong(BasicTypes.FIELD_LONG, aLong);
        nestedDic.setNumber(BasicTypes.FIELD_NUMBER, aNumber);
        nestedDic.setString(BasicTypes.FIELD_STRING, aString);

        MutableDocument document = new MutableDocument();
        MutableArray array = new MutableArray();
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

    @Test
    public void testEnumList() throws Exception {
        Box box = new Box();

        box.addSpecy(Species.Cat);
        box.addSpecy(Species.Dog);

        CBLMapper mapper = new CBLMapper();

        Document doc = mapper.toDocument(box);

        Box unserializedBox = mapper.fromDocument(doc, Box.class);

        assertEquals(2, unserializedBox.getSpecies().size());
        assertEquals(Species.Cat, unserializedBox.getSpecies().get(0));
        assertEquals(Species.Dog, unserializedBox.getSpecies().get(1));
    }

    @Test
    public void testSerializeForeignDoc() throws Exception {
        Song firstSong = new Song("aaa", "First song");
        Song secondSong = new Song("bbb", "Second song");

        List<Song> songList = new ArrayList<>();
        songList.add(firstSong);
        songList.add(secondSong);

        Album album = new Album("xxx", songList);

        CBLMapper mapper = new CBLMapper();
        Document doc = mapper.toDocument(album);

        assertNotNull(doc.getArray(Album.FIELD_SONG_LIST));
        assertEquals("aaa", doc.getArray(Album.FIELD_SONG_LIST).getString(0));
        assertEquals("bbb", doc.getArray(Album.FIELD_SONG_LIST).getString(1));

        album = mapper.fromDocument(doc, Album.class);

        assertNotNull(album);
        assertEquals("xxx", album.ID);
    }

    @Test
    public void testUnserializeForeignDoc() throws Exception {
        List<Object> songIDs = new ArrayList<>();
        songIDs.add("aaa");
        songIDs.add("bbb");

        MutableDocument doc = new MutableDocument();
        doc.setArray(Album.FIELD_SONG_LIST, new MutableArray(songIDs));

        CBLMapper mapper = new CBLMapper();
        Album album = mapper.fromDocument(doc, Album.class);

        assertNotNull(album.SongList);
        assertEquals("aaa", album.SongList.get(0).getDocumentID());
        assertEquals("bbb", album.SongList.get(1).getDocumentID());
    }

    @Test
    public void testZonedDatetime() throws Exception {

        final Blob aBlob = new Blob("text/plain", "Byte array test".getBytes());
        final boolean aBoolean = true;
        final Date aDate = new Date();
        final double aDouble = 4.456;
        final float aFloat = 34.34f;
        final int anInt = 15;
        final long aLong = 23456345;
        final Number aNumber = 4567;
        final String aString = "Test string";
//        final ZonedDateTime zonedDateTime = ZonedDateTime.parse("2017-03-28T12:25:38.492+05:30[Asia/Calcutta]");
        final ZonedDateTime zonedDateTime = null;

        final BasicTypes basicTypes = new BasicTypes(aBlob,
                                                     aBoolean,
                                                     aDate,
                                                     aDouble,
                                                     aFloat,
                                                     anInt,
                                                     aLong,
                                                     aNumber,
                                                     aString,
                                                     zonedDateTime);

        CBLMapper mapper = new CBLMapper();
        Document document = mapper.toDocument(basicTypes);

        assertEquals(document.getString(BasicTypes.FIELD_ZONED_DATE), null);

        BasicTypes output = mapper.fromDocument(document, BasicTypes.class);

        assertEquals(zonedDateTime, output.mZonedDateTime);
    }

    @Test
    public void testBoolean() throws Exception {
        BooleanType booleanType = new BooleanType(true);

        CBLMapper mapper = new CBLMapper();

        Document document = mapper.toDocument(booleanType);

        BooleanType output = mapper.fromDocument(document, BooleanType.class);

        assertEquals(booleanType.mBoolean, output.mBoolean);
    }

    public static class BooleanType extends CBLDocument {
        public static final String FIELD_BOOLEAN = "boolean";

        @DocumentField(fieldName = FIELD_BOOLEAN)
        Boolean mBoolean;

        public BooleanType(){}

        public BooleanType(boolean aBoolean){
            mBoolean = aBoolean;
        }
    }

    public static class BasicTypes extends CBLDocument {

        public static final String FIELD_BLOB = "blob";
        public static final String FIELD_BOOLEAN = "boolean";
        public static final String FIELD_DATE = "date";
        public static final String FIELD_ZONED_DATE = "zoned_date";
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

        @DocumentField(fieldName = FIELD_ZONED_DATE)
        ZonedDateTime mZonedDateTime;

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
                          String aString,
                          ZonedDateTime zonedDateTime) {
            mBlob = aBlob;
            mBoolean = aBoolean;
            mDate = aDate;
            mDouble = aDouble;
            mFloat = aFloat;
            mInt = anInt;
            mLong = aLong;
            mNumber = aNumber;
            mString = aString;
            mZonedDateTime = zonedDateTime;
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

        @NestedDocument()
        @DocumentField(fieldName = FIELD_BASIC_TYPES)
        BasicTypes mBasicTypes;

        public NestedType() {}

        public NestedType(String id, BasicTypes aBasics) {
            setDocumentID(id);
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

        @NestedDocument
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
            setDocumentID(id);
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

    public static class Box extends CBLDocument {

        public static final String FIELD_SPECIES = "species";

        @DocumentField(fieldName = FIELD_SPECIES)
        public List<Species> mSpecies;

        public Box() {
            mSpecies = new ArrayList<>();
        }

        public void addSpecy(Species specy) {
            mSpecies.add(specy);
        }

        public List<Species> getSpecies() {
            return mSpecies;
        }
    }

    public static class Album extends CBLDocument {

        public static final String FIELD_ID = "id";
        public static final String FIELD_SONG_LIST = "songList";

        @DocumentField(fieldName = FIELD_ID, ID = true)
        public String ID;

        @DocumentField(fieldName = FIELD_SONG_LIST)
        public List<Song> SongList;

        public Album() {
            SongList = new ArrayList<>();
        }

        public Album(String docID, List<Song> songList) {
            setDocumentID(docID);
            SongList = songList;
        }
    }

    public static class Song extends CBLDocument {

        public static final String FIELD_ID = "id";
        public static final String FIELD_TITLE = "title";

        @DocumentField(fieldName = FIELD_ID, ID = true)
        public String ID;

        @DocumentField(fieldName = FIELD_TITLE)
        public String Title;


        public Song() { }

        public Song(String docId, String title) {
            setDocumentID(docId);
            Title = title;
        }
    }

}