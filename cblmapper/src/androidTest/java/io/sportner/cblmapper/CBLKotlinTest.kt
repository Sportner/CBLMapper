package io.sportner.cblmapper

import android.support.test.runner.AndroidJUnit4
import com.couchbase.lite.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CBLKotlinTest {

    private val mapper = CBLMapper()

    @Test
    fun testPrimitiveBooleanEncode() {
        val instance = SimplePrimitiveBoolean()
        instance.value = true

        val document = mapper.toDocument(instance)

        assertTrue(document.getBoolean("value"))
    }

    @Test
    fun testPrimitiveBooleanDecode() {
        val doc = MutableDocument()
        doc.setBoolean("value", true)

        val instance = mapper.fromDocument(doc, SimplePrimitiveBoolean::class)

        assertTrue(instance!!.value)
    }

    @Test
    fun testPrimitiveIntEncode() {
        val instance = SimplePrimitiveInt()
        instance.value = 21

        val document = mapper.toDocument(instance)

        assertEquals(21, document.getInt("value"))
    }

    @Test
    fun testPrimitiveInte() {
        val doc = MutableDocument()
        doc.setInt("value", 25)

        val instance = mapper.fromDocument(doc, SimplePrimitiveInt::class)

        assertEquals(25, instance!!.value)
    }

    @Test
    fun testPrimitiveDoubleEncode() {
        val instance = SimplePrimitiveDouble()
        instance.value = 21.4

        val document = mapper.toDocument(instance)

        assertEquals(21.4, document.getDouble("value"), 0.1)
    }

    @Test
    fun testPrimitiveDoubleDecode() {
        val doc = MutableDocument()
        doc.setDouble("value", 25.4)

        val instance = mapper.fromDocument(doc, SimplePrimitiveDouble::class)

        assertEquals(25.4, instance!!.value, 0.1)
    }

    @Test
    fun testPrimitiveFloatEncode() {
        val instance = SimplePrimitiveFloat()
        instance.value = -20.4f

        val document = mapper.toDocument(instance)

        assertEquals(-20.4f, document.getFloat("value"))
    }

    @Test
    fun testPrimitiveFloatDecode() {
        val doc = MutableDocument()
        doc.setFloat("value", 25.34f)

        val instance = mapper.fromDocument(doc, SimplePrimitiveFloat::class)

        assertEquals(25.34f, instance!!.value)
    }

    @Test
    fun testPrimitiveCharEncode() {
        val instance = SimplePrimitiveChar()
        instance.value = 'f'

        val document = mapper.toDocument(instance)

        assertEquals("f", document.getString("value"))
    }

    @Test
    fun testPrimitiveCharDecode() {
        val doc = MutableDocument()
        doc.setValue("value", 't'.toString())

        val instance = mapper.fromDocument(doc, SimplePrimitiveChar::class)

        assertEquals('t', instance!!.value)
    }

    @Test
    fun testPrimitiveStringEncode() {
        val instance = SimplePrimitiveString()
        instance.value = "This is a test String"

        val document = mapper.toDocument(instance)

        assertEquals("This is a test String", document.getString("value"))
    }

    @Test
    fun testPrimitiveStringDecode() {
        val doc = MutableDocument()
        doc.setString("value", "This is a test String")

        val instance = mapper.fromDocument(doc, SimplePrimitiveString::class)

        assertEquals("This is a test String", instance!!.value)
    }

    @Test
    fun testPrimitiveBlobEncode() {
        val instance = SimpleBlob()
        instance.value = Blob("text/html", "<b>Just some html<b>".toByteArray())

        val document = mapper.toDocument(instance)

        assertEquals(Blob("text/html", "<b>Just some html<b>".toByteArray()), document.getBlob("value"))
    }

    @Test
    fun testPrimitiveBlobDecode() {
        val doc = MutableDocument()
        doc.setBlob("value", Blob("text/html", "<b>Just some html<b>".toByteArray()))

        val instance = mapper.fromDocument(doc, SimpleBlob::class)

        assertEquals(Blob("text/html", "<b>Just some html<b>".toByteArray()), instance!!.value)
    }

    @Test
    fun testPrimitiveEnumEncode() {
        val instance = SimpleEnum()
        instance.value = Animals.MOUSE

        val document = mapper.toDocument(instance)

        assertEquals(Animals.MOUSE.name, document.getString("value"))
    }

    @Test
    fun testPrimitiveEnumDecode() {
        val doc = MutableDocument()
        doc.setString("value", Animals.MOUSE.name)

        val instance = mapper.fromDocument(doc, SimpleEnum::class)

        assertEquals(Animals.MOUSE, instance!!.value)
    }

    @Test
    fun testEnumListEncode() {
        val instance = SimpleEnumList()
        instance.addAnimal(Animals.DOGS).addAnimal(Animals.CAT).addAnimal(Animals.MOUSE)

        val doc = mapper.toDocument(instance)

        val array = doc.getArray("value")
        assertNotNull(array)

        assertEquals(Animals.DOGS.name, array.getString(0))
        assertEquals(Animals.CAT.name, array.getString(1))
        assertEquals(Animals.MOUSE.name, array.getString(2))
    }

    @Test
    fun testEnumListDecode() {
        val array = MutableArray()
        array.addString(Animals.DOGS.name)
        array.addString(Animals.CAT.name)
        array.addString(Animals.MOUSE.name)

        val doc = MutableDocument()
        doc.setArray("value", array)


        val instance = mapper.fromDocument(doc, SimpleEnumList::class)


        assertNotNull(instance!!.value)

        assertEquals(Animals.DOGS, instance!!.value[0])
        assertEquals(Animals.CAT, instance!!.value[1])
        assertEquals(Animals.MOUSE, instance!!.value[2])
    }

    @Test
    fun testHashMapEncode() {

        val instance = SimpleMap()

        instance.map["test"] = "value"
        instance.map["test2"] = "value2"

        val doc = mapper.toDocument(instance)

        val map = doc.getDictionary("map") as Dictionary

        assertEquals("value", map.getString("test"))
        assertEquals("value2", map.getString("test2"))

    }

    @Test
    fun testHashMapDecode() {
        val map = MutableDictionary()
        map.setString("test", "value")
        map.setString("test2", "value2")

        val doc = MutableDocument()
        doc.setDictionary("map", map)

        val instance = mapper.fromDocument(doc, SimpleMap::class) as SimpleMap // Remove nullable type

        assertEquals("value", instance.map["test"])
        assertEquals("value2", instance.map["test2"])
    }

    @Test
    fun testSerializeCBLDocument() {

    }
}