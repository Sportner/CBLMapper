package io.sportner.cblmapper.common;

import io.sportner.cblmapper.annotations.DocumentField;

/**
 * Created by alblanc on 19/08/2017.
 */

public class SimplePet {

    public static final String FIELD_NAME = "name";

    @DocumentField(ID = true)
    private String ID;

    @DocumentField(fieldName = FIELD_NAME)
    private String name;

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}