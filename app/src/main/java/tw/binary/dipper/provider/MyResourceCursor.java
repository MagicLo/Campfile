package tw.binary.dipper.provider;/* Created by eason on 2015/5/22. */

import android.database.AbstractCursor;

import java.util.ArrayList;

import tw.binary.dipper.api.myResourceApi.model.MyResource;

public class MyResourceCursor extends AbstractCursor {
    private ArrayList<MyResource> mObjects;
    int position;

    public MyResourceCursor(ArrayList<MyResource> pObjects) {

        mObjects = pObjects;
    }

    public MyResource getObject(int position) {
        return mObjects.get(position);
    }

    public void addObject(MyResource data) {
        /*mObjects = mContentResolver.query(GAEContentProvider.CONTENT_URI_MYRESOURCE, null,
                null, null, null);*/
        mObjects.add(data);
    }

    @Override
    public int getCount() {
        return mObjects.size();
    }

    @Override
    public String[] getColumnNames() {
        return new String[0];
    }

    @Override
    public String getString(int i) {
        return null;
    }

    @Override
    public short getShort(int i) {
        return 0;
    }

    @Override
    public int getInt(int i) {
        return 0;
    }

    @Override
    public long getLong(int i) {
        return 0;
    }

    @Override
    public float getFloat(int i) {
        return 0;
    }

    @Override
    public double getDouble(int i) {
        return 0;
    }

    @Override
    public boolean isNull(int i) {
        return false;
    }
}
