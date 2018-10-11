package io.sportner.cblmapper.mappers

import io.sportner.cblmapper.exceptions.CBLMapperClassException
import kotlin.reflect.KClass
import kotlin.reflect.KTypeProjection
import kotlin.reflect.jvm.jvmErasure

class MapDefaultTypeAdapter : CBLMTypeAdapter<Map<*, *>> {
    override fun decode(map: Any?, typeOfT: KClass<Map<*, *>>, typesParameter: List<KTypeProjection>?, context: CBLMapperDecoderContext): Map<*, *>? {

        if (typesParameter == null || typesParameter.size != 2) {
            throw CBLMapperClassException(typeOfT.java, "Why the fuck the map has not types parameter ?")
        }

        map as Map<String, Any?>
        val decodedMap = HashMap<Any, Any?>()

        for (entry in map.entries) {
            val key = context.decode(entry.key, typesParameter[0].type!!.jvmErasure, typesParameter[0].type!!.arguments)
                    ?: throw CBLMapperClassException(typesParameter[0].type!!.jvmErasure.java, "Map key cannot be null")
            val value = context.decode(entry.value, typesParameter[1].type!!.jvmErasure, typesParameter[1].type!!.arguments)
            decodedMap[key] = value
        }
        return decodedMap
    }

    override fun encode(value: Any, typeOfT: KClass<Map<*, *>>, typesParameter: List<KTypeProjection>?, context: CBLMapperEncoderContext): Any? {
        if (typesParameter == null || typesParameter.size != 2) {
            throw CBLMapperClassException(typeOfT.java, "Why the fuck the map has not types parameter ?")
        }
        value as Map<Any, Any?>
        val encodedMap = HashMap<String, Any?>()
        value.forEach { entry ->
            val key = context.encode(entry.key, typesParameter[0].type!!.jvmErasure, typesParameter[0].type!!.arguments)
                    ?: throw CBLMapperClassException(typesParameter[0].type!!.jvmErasure.java, "Map key cannot be null")
            key as? String
                    ?: throw CBLMapperClassException(typesParameter[0].type!!.jvmErasure.java, "Map key must be a String")

            val value = context.encode(entry.value, typesParameter[1].type!!.jvmErasure, typesParameter[1].type!!.arguments)
            encodedMap[key] = value
        }
        return encodedMap
    }

}