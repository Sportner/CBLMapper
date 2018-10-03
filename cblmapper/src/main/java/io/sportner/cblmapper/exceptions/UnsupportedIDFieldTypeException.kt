package io.sportner.cblmapper.exceptions

class UnsupportedIDFieldTypeException(unhandledClass: Class<*>) : CBLMapperClassException(unhandledClass, String.format("'%s' must use a String as ID field", unhandledClass)) {
}
