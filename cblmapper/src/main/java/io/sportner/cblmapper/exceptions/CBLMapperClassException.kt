package io.sportner.cblmapper.exceptions

open class CBLMapperClassException(protected val exceptionClass: Class<*>,
                                   protected val description: String) : RuntimeException() {

    override fun toString(): String {
        return String.format("CBLMapperClassException for class '%s': %s", this.exceptionClass, this.description)
    }
}
