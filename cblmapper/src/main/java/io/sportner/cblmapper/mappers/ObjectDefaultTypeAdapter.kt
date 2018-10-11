package io.sportner.cblmapper.mappers

import io.sportner.cblmapper.annotations.DocumentField
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty
import kotlin.reflect.KTypeProjection
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.jvmErasure

class ObjectDefaultTypeAdapter : CBLMTypeAdapter<Any> {

    override fun decode(value: Any?, typeOfT: KClass<Any>, typesParameter: List<KTypeProjection>?, context: CBLMapperDecoderContext): Any? {
        return if (value == null) null else {
            val map = value as Map<String, Any?>
            val instanceOfT = typeOfT.createInstance()

            for (property in typeOfT.memberProperties) {
                if (property.findAnnotation<Transient>() != null) {
                    continue
                }
                (property as? KMutableProperty1<Any, Any>)?.let { mutableProperty ->
                    context.decode(map[getPropertySerializedName(property)], property.returnType.jvmErasure, property.returnType.arguments)?.let { propertyValue ->
                        mutableProperty.set(instanceOfT, propertyValue)
                    }
                }
                property.returnType.arguments
            }

            instanceOfT
        }
    }

    override fun encode(value: Any, typeOfT: KClass<Any>, typesParameter: List<KTypeProjection>?, context: CBLMapperEncoderContext): Any? {
        val map = HashMap<String, Any>()

        for (property in value.javaClass.kotlin.memberProperties) {
            if (property.findAnnotation<Transient>() != null) {
                continue
            }
            context.encode(property.get(value), property.returnType.jvmErasure, property.returnType.arguments)?.let {
                map[getPropertySerializedName(property)] = it
            }
        }

        return map
    }

    private fun getPropertySerializedName(property: KProperty<*>): String {
        val documentFieldAnnotation = property.findAnnotation<DocumentField>()
        return if (documentFieldAnnotation != null && documentFieldAnnotation.value.isNotBlank()) documentFieldAnnotation.value else property.name
    }
}
