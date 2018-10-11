package io.sportner.cblmapper.mappers

import com.couchbase.lite.internal.utils.DateUtils
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KTypeProjection

class DateDefaultTypeAdapter : CBLMTypeAdapter<Date> {

    override fun decode(value: Any?, typeOfT: KClass<Date>, typesParameter: List<KTypeProjection>?, context: CBLMapperDecoderContext): Date? {
        return DateUtils.fromJson(value as String?)
    }

    override fun encode(value: Any, typeOfT: KClass<Date>, typesParameter: List<KTypeProjection>?, context: CBLMapperEncoderContext): Any? {
        return DateUtils.toJson(value as Date)
    }
}
