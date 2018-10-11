package io.sportner.cblmapper.mappers

import kotlin.reflect.KClass
import kotlin.reflect.KTypeProjection

class EnumDefaultTypeAdapter : CBLMTypeAdapter<Enum<*>> {
    override fun decode(value: Any?, typeOfT: KClass<Enum<*>>, typesParameter: List<KTypeProjection>?, context: CBLMapperDecoderContext): Enum<*>? {
        if (value == null) return null

        // TODO: Find a way to iterate enum without using java class ?
        return typeOfT.java.enumConstants.find { it.name == value }
    }

    override fun encode(value: Any, typeOfT: KClass<Enum<*>>, typesParameter: List<KTypeProjection>?, context: CBLMapperEncoderContext): Any? {
        return (value as Enum<*>).name
    }
}
