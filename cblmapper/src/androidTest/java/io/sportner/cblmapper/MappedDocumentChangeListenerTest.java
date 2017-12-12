package io.sportner.cblmapper;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.DatabaseConfiguration;
import com.couchbase.lite.DocumentChange;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by alblanc on 27/09/2017.
 */
@RunWith(AndroidJUnit4.class)
public class MappedDocumentChangeListenerTest {

    public static class Pet extends CBLDocument {
        String name;
    }

    private Context instrumentationCtx;
    private Database mDatabase;

    @Before
    public void setup() throws CouchbaseLiteException {
        instrumentationCtx = InstrumentationRegistry.getContext();
        DatabaseConfiguration config = new DatabaseConfiguration(instrumentationCtx);

        mDatabase = new Database("test", config);
    }

    @After
    public void clean() throws CouchbaseLiteException {
        if (mDatabase != null) {
            mDatabase.delete();
        }
    }

    @Test
    public void testAbstract() {
    }
}
