package io.sportner.cblmapper;

import org.junit.Test;

import java.lang.reflect.Field;

import io.sportner.cblmapper.annotations.DocumentField;
import io.sportner.cblmapper.common.SimplePet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * DocumentField annotation unit test
 */
public class DocumentFieldTest {

    @Test
    public void annotation_isPresent() throws Exception {

        SimplePet cat = new SimplePet();

        Field field = cat.getClass().getDeclaredField("name");

        assertTrue(field.isAnnotationPresent(DocumentField.class));
    }

    @Test
    public void annotation_isFieldNameCorrect() throws Exception {
        SimplePet cat = new SimplePet();

        Field nameField = cat.getClass().getDeclaredField("name");
        DocumentField annotation = nameField.getAnnotation(DocumentField.class);

        assertEquals(annotation.fieldName(), SimplePet.FIELD_NAME);
    }

    @Test
    public void annotation_isIDCorrect() throws Exception {
        SimplePet cat = new SimplePet();

        Field nameField = cat.getClass().getDeclaredField("name");
        DocumentField nameFieldAnnotation = nameField.getAnnotation(DocumentField.class);

        assertFalse(nameFieldAnnotation.ID());

        Field idField = cat.getClass().getDeclaredField("ID");
        DocumentField IDFieldAnnotation = idField.getAnnotation(DocumentField.class);

        assertTrue(IDFieldAnnotation.ID());
    }
}