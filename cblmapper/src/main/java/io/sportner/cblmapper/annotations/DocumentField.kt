package io.sportner.cblmapper.annotations

@Retention
@Target(AnnotationTarget.FIELD)
annotation class DocumentField(val value: String = "")
