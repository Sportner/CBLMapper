package io.sportner.cblmapper.mappers

import io.sportner.cblmapper.CBLMapper
import kotlin.reflect.KClass
import kotlin.reflect.KTypeProjection

class CBLMapperEncoderContext(private val mapper: CBLMapper) {
    fun encode(value: Any?, typeOfT: KClass<*>, typesParameter: List<KTypeProjection>?): Any? = mapper.encode(value, typeOfT, typesParameter)
}

class CBLMapperDecoderContext(private val mapper: CBLMapper) {
    fun decode(value: Any?, typeOfT: KClass<*>, typesParameter: List<KTypeProjection>?): Any? = mapper.decode(value, typeOfT, typesParameter)
}

interface CBLMTypeAdapter<T : Any> {
    fun decode(value: Any?, typeOfT: KClass<T>, typesParameter: List<KTypeProjection>?, context: CBLMapperDecoderContext): T?
    fun encode(value: Any, typeOfT: KClass<T>, typesParameter: List<KTypeProjection>?, context: CBLMapperEncoderContext): Any?
}