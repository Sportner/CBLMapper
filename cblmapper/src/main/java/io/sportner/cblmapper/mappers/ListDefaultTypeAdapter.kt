package io.sportner.cblmapper.mappers

import io.sportner.cblmapper.exceptions.CBLMapperClassException
import kotlin.reflect.KClass
import kotlin.reflect.KTypeProjection
import kotlin.reflect.jvm.jvmErasure

class ListDefaultTypeAdapter : CBLMTypeAdapter<List<*>> {

    override fun decode(list: Any?, typeOfT: KClass<List<*>>, typesParameter: List<KTypeProjection>?, context: CBLMapperDecoderContext): List<*>? {
        if (typesParameter == null || typesParameter.size != 1) {
            throw CBLMapperClassException(typeOfT.java, "Why the fuck the list has not types parameter ?")
        }

        if (list == null) {
            return null
        }

        list as List<*>
        val decodedList = ArrayList<Any?>()

        for (value in list) {
            decodedList.add(context.decode(value, typesParameter[0].type!!.jvmErasure, typesParameter[0].type!!.arguments))
        }

        return decodedList
    }

    override fun encode(value: Any, typeOfT: KClass<List<*>>, typesParameter: List<KTypeProjection>?, context: CBLMapperEncoderContext): Any? {
        if (typesParameter == null || typesParameter.size != 1) {
            throw CBLMapperClassException(typeOfT.java, "Why the fuck the list has not types parameter ?")
        }
        value as List<Any>
        val encodedList = ArrayList<Any?>(value.size)
        value.forEach { encodedList.add(context.encode(it, typesParameter[0].type!!.jvmErasure, typesParameter[0].type!!.arguments)) }
        return encodedList
    }

}