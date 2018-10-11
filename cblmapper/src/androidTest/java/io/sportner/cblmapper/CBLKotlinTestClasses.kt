package io.sportner.cblmapper

import com.couchbase.lite.Blob
import io.sportner.cblmapper.annotations.DocumentField


class SimplePrimitiveBoolean : CBLDocument() {

    @DocumentField("value")
    var value: Boolean = false
}

class SimplePrimitiveInt : CBLDocument() {

    @DocumentField("value")
    var value: Int = 0
}

class SimplePrimitiveDouble : CBLDocument() {

    @DocumentField("value")
    var value: Double = 0.0
}

class SimplePrimitiveFloat : CBLDocument() {

    @DocumentField("value")
    var value: Float = 0.0f
}

class SimplePrimitiveChar : CBLDocument() {

    @DocumentField("value")
    var value: Char = 'a'
}

class SimplePrimitiveString : CBLDocument() {

    @DocumentField("value")
    var value: String = ""
}

class SimpleBlob : CBLDocument() {

    @DocumentField("value")
    var value: Blob = Blob("octet/stream", "RandomValueAsWeDontTestNullValueHere".toByteArray())
}


class SimpleNullable : CBLDocument() {
    @DocumentField("value")
    var value: String? = null
}

enum class Animals {
    CAT, DOGS, MOUSE;
}

class SimpleEnum : CBLDocument() {
    var value = Animals.CAT
}

class SimpleEnumList : CBLDocument() {
    var value: MutableList<Animals> = ArrayList()

    fun addAnimal(animal: Animals): SimpleEnumList {
        value.add(animal)
        return this
    }
}

class SimpleMap : CBLDocument() {
    var map = HashMap<String, String>()
}


