package io.sportner.cblmapper.common;

import io.sportner.cblmapper.annotations.DocumentField;

/**
 * Created by alblanc on 19/08/2017.
 */

public class Car {

    public static final String FIELD_WHEELS = "wheels";

    @DocumentField(fieldName = FIELD_WHEELS)
    int wheels;

    public int getWheels() {
        return wheels;
    }

    public void setWheels(int wheels) {
        this.wheels = wheels;
    }
}
