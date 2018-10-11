package io.sportner.cblmapper.annotations

@Retention
@Target(AnnotationTarget.FIELD)
annotation class CBLEnumValue(val value: String = "")
