package io.sportner.cblmapper.annotations

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

@Retention(RetentionPolicy.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class DocumentField(val fieldName: String = "", val ID: Boolean = false)
