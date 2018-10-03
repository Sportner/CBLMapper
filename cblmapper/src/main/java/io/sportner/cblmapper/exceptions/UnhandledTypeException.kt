package io.sportner.cblmapper.exceptions

class UnhandledTypeException(unhandledClass: Class<*>) : CBLMapperClassException(unhandledClass, String.format("Class '%s' has no type adapter", unhandledClass.name))
