package io.sportner.cblmapper.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by alblanc on 19/08/2017.
 */

public class FieldHelper {

    // Thanks to: https://stackoverflow.com/a/16966699/1261036
    public static Iterable<Field> getFieldsUpTo(@NonNull Class<?> startClass,
                                                @Nullable Class<?> exclusiveParent) {

        // Arrays.asList returns an unmodifiable list. We need to create another extra list in order to be able
        // to add field from child classes
        List<Field> currentClassFields = new ArrayList<>(Arrays.asList(startClass.getDeclaredFields()));
        Class<?> parentClass = startClass.getSuperclass();

        if (parentClass != null &&
            (exclusiveParent == null || !(parentClass.equals(exclusiveParent)))) {
            List<Field> parentClassFields =
                    (List<Field>) getFieldsUpTo(parentClass, exclusiveParent);
            currentClassFields.addAll(parentClassFields);
        }

        return currentClassFields;
    }

}
