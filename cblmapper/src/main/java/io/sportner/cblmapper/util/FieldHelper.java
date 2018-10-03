package io.sportner.cblmapper.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FieldHelper {
    public FieldHelper() {
    }

    public static Iterable<Field> getFieldsUpTo(@NonNull Class<?> startClass, @Nullable Class<?> exclusiveParent) {
        List<Field> currentClassFields = new ArrayList(Arrays.asList(startClass.getDeclaredFields()));
        Class<?> parentClass = startClass.getSuperclass();
        if (parentClass != null && (exclusiveParent == null || !parentClass.equals(exclusiveParent))) {
            List<Field> parentClassFields = (List)getFieldsUpTo(parentClass, exclusiveParent);
            currentClassFields.addAll(parentClassFields);
        }

        return currentClassFields;
    }
}