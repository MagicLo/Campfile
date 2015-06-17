package tw.binary.dipper.provider;/* Created by eason on 2015/5/22. */

import android.content.AsyncTaskLoader;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

public class MyResourceLoader extends AsyncTaskLoader<Cursor> {
    private String[] mLocalIds;
    private Context mContext;
    private Cursor mMyResourceCursor = null;

    /*public MyResourceLoader(Context context) {
        super(context);
    }*/

    public MyResourceLoader(Context context, String[] ids) {
        super(context);

        mContext = context;
        mLocalIds = ids;
    }

    @Override
    protected void onStartLoading() {
        if (mMyResourceCursor != null) {
            // Deliver any previously loaded data immediately.
            deliverResult(mMyResourceCursor);
        }

        // Begin monitoring the underlying data source.
       /* if (mObserver == null) {
            mObserver = new SampleObserver();
            // TODO: register the observer
        }*/

        if (takeContentChanged() || mMyResourceCursor == null) {
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    public Cursor loadInBackground() {
        Log.i("TAG", "Loader loadInBackground!");
        ContentResolver contentResolver = mContext.getContentResolver();  //获取ContentResolver
        Cursor cursor = contentResolver.query(GAEContentProvider.CONTENT_URI_MYRESOURCE,
                null,
                null,
                mLocalIds,
                null);
        return cursor;
    }

    @Override
    public void deliverResult(Cursor data) {

        if (isReset()) {
            releaseResources(data);
            return;
        }

        Cursor oldData = mMyResourceCursor;
        mMyResourceCursor = data;
        // If the Loader is in a started state, deliver the results to the
        // client. The superclass method does this for us.
        if (isStarted()) {
            super.deliverResult(data);
            Log.i("TAG", "Loader deliverResult!");
        }

        if (oldData != null && oldData != data) {
            releaseResources(oldData);
        }

    }


    private void releaseResources(Cursor data) {
        // For a simple List, there is nothing to do. For something like a Cursor, we
        // would close it in this method. All resources associated with the Loader
        // should be released here.
    }

    @Override
    protected void onReset() {
        // Ensure the loader has been stopped.
        onStopLoading();

        if (mMyResourceCursor != null) {
            releaseResources(mMyResourceCursor);
            mMyResourceCursor = null;
        }

        // The Loader is being reset, so we should stop monitoring for changes.
        /*if (mObserver != null) {
            // TODO: unregister the observer
            mObserver = null;
        }*/
    }

    @Override
    public void onCanceled(Cursor data) {
        super.onCanceled(data);
        // The load has been canceled, so we should release the resources
        // associated with 'data'.
        releaseResources(data);
    }
}
