package io.sportner.cblmapper.bind.lists;

import com.couchbase.lite.Array;
import com.couchbase.lite.Dictionary;
import com.couchbase.lite.Document;

import java.util.ArrayList;

import io.sportner.cblmapper.CBLMapper;

/**
 * Created by alblanc on 21/08/2017.
 */
@Deprecated
public class ArrayListTypeAdapter implements ListTypeAdapter<ArrayList> {

    @Override
    public void writeDocument(CBLMapper mapper, Document document, String fieldName, ArrayList value) {
        Array array = new Array();
        for (Object object: value){


        }
    }

    @Override
    public void writeDocument(CBLMapper mapper, Dictionary dictionary, String fieldName, ArrayList value) {

    }
}
