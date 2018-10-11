package io.sportner.cblmapper.exceptions

import java.lang.reflect.Type
import kotlin.reflect.KClass

class UnhandledTypeException(unhandledType: Type) : CBLMapperClassException(unhandledType, String.format("Class '%s' has no type adapter", unhandledType.toString())) {
    constructor(unhandledKClass: KClass<*>): this(unhandledKClass.java)
    constructor(unhandledClass: Class<*>): this(unhandledClass as Type)
}
