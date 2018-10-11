package io.sportner.cblmapper.exceptions

import java.lang.reflect.Type

open class CBLMapperClassException(protected val exceptionClass: Type,
                                   protected val description: String) : RuntimeException() {

    override fun toString(): String {
        return String.format("CBLMapperClassException for class '%s': %s", this.exceptionClass.toString(), this.description)
    }
}
