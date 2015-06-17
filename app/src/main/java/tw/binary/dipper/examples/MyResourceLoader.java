package tw.binary.dipper.examples;/* Created by eason on 2015/5/22. */

import android.content.AsyncTaskLoader;
import android.content.Context;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;

import java.io.IOException;
import java.util.ArrayList;

import tw.binary.dipper.api.myResourceApi.MyResourceApi;
import tw.binary.dipper.api.myResourceApi.model.CollectionResponseMyResource;
import tw.binary.dipper.api.myResourceApi.model.MyResource;

public class MyResourceLoader extends AsyncTaskLoader<ArrayList<MyResource>> {
    private String[] mLocalIds;
    private Context mContext;

    ArrayList<MyResource> myMergeResources = new ArrayList<>();
    private static MyResourceApi myApiService = null;

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
        forceLoad();
        super.onStartLoading();
    }

    @Override
    public ArrayList<MyResource> loadInBackground() {

        String nextPageToken = "";
        ArrayList<MyResource> mData;

        if (myApiService == null) {  // Only do this once
            MyResourceApi.Builder builder = new MyResourceApi.Builder(AndroidHttp.newCompatibleTransport(),
                    new AndroidJsonFactory(), null)
                    // options for running against local devappserver
                    // - 10.0.2.2:8080 is localhost's IP address in Android emulator
                    // - turn off compression when running against local devappserver
                    .setRootUrl("https://graceful-design-89523.appspot.com/_ah/api/")
                            //.setRootUrl("http://10.0.2.2:8080/_ah/api/")
                    .setGoogleClientRequestInitializer(new GoogleClientRequestInitializer() {
                        @Override
                        public void initialize(AbstractGoogleClientRequest<?> abstractGoogleClientRequest) throws IOException {
                            abstractGoogleClientRequest.setDisableGZipContent(true);
                        }
                    });
            myApiService = builder.build();
        }

        try {
            for (String mLocalId : mLocalIds) {
                MyResourceApi.ListByUser api = myApiService.listByUser(mLocalId);
                api.setCursor(nextPageToken);
                int pageLimit = 100;
                api.setLimit(pageLimit);    //設定最大讀取筆數
                CollectionResponseMyResource feed = api.execute();
                mData = (ArrayList<MyResource>) feed.getItems();
                nextPageToken = feed.getNextPageToken();    //TODO 是否可再續查?
                myMergeResources.addAll(mData);   //結果Merge在一起
            }
        } catch (IOException e) {
            e.getMessage();
        }

        return myMergeResources;
    }

    @Override
    public void deliverResult(ArrayList<MyResource> data) {

        if (isReset()) {
            releaseResources(data);
            return;
        }

        ArrayList<MyResource> oldData = myMergeResources;
        myMergeResources = data;

        if (isStarted()) {
            super.deliverResult(data);
        }

        if (oldData != null && oldData != data) {
            releaseResources(oldData);
        }

    }

    private void releaseResources(ArrayList<MyResource> data) {
        // For a simple List, there is nothing to do. For something like a Cursor, we
        // would close it in this method. All resources associated with the Loader
        // should be released here.
    }

}
