package io.sportner.cblmapper;

import com.couchbase.lite.Document;

import org.junit.Test;

import io.sportner.cblmapper.common.SimplePet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by alblanc on 19/08/2017.
 */

public class CBLMapperTest {

    @Test
    public void mapper_testSimpleDocumentField() throws Exception {
        SimplePet cat = new SimplePet();
        final String PET_NAME = "Nina";
        cat.setName(PET_NAME);

        CBLMapper mapper = new CBLMapper();
        Document catDocument = mapper.toDocument(cat);

        assertNotNull(catDocument);
        assertEquals(catDocument.getString(SimplePet.FIELD_NAME), PET_NAME);
    }
}
