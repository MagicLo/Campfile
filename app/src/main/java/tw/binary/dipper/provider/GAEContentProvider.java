package tw.binary.dipper.provider;/* Created by eason on 2015/5/20. */

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;
import com.google.gson.Gson;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;

import tw.binary.dipper.api.myResourceApi.MyResourceApi;
import tw.binary.dipper.api.myResourceApi.model.CollectionResponseMyResource;
import tw.binary.dipper.api.myResourceApi.model.MyResource;

/**
 * Created by eason on 2015/5/7.
 */
public class GAEContentProvider extends ContentProvider {
    public static final Uri CONTENT_URI_MYRESOURCE = Uri.parse("content://tw.binary.dipper.api/myresource");

    private static final int MYRESOURCE_ALLROWS = 1;
    private static final int MYRESOURCE_ONEROW = 2;
    private static final UriMatcher uriMatcher;

    private MyResourceApi myApiService = null;
    private String nextPageToken = "";

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI("tw.binary.dipper.api", "myresource", MYRESOURCE_ALLROWS);
        uriMatcher.addURI("tw.binary.dipper.api", "myresource/#", MYRESOURCE_ONEROW);
    }

    @Override
    public boolean onCreate() {
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
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        ArrayList<MyResource> myResources;
        ArrayList<MyResource> myMergeResources = new ArrayList<>();
        MatrixCursor myResourceCursor = null;

        switch (uriMatcher.match(uri)) {
            case MYRESOURCE_ALLROWS:
                try {
                    for (String selectionArg : selectionArgs) {
                        MyResourceApi.ListByUser api = myApiService.listByUser(selectionArg);
                        //api.setCursor(nextPageToken);
                        int pageLimit = 100;
                        api.setLimit(pageLimit);    //設定最大讀取筆數
                        CollectionResponseMyResource feed = api.execute();
                        myResources = (ArrayList<MyResource>) feed.getItems();
                        nextPageToken = feed.getNextPageToken();    //TODO 是否可再續查?
                        if (myResources != null)
                            myMergeResources.addAll(myResources);   //結果Merge在一起
                    }
                } catch (IOException e) {
                    e.getMessage();
                }
                break;
            case MYRESOURCE_ONEROW:
                //Log.i("TAG", "selectionArgs = " + selectionArgs[0]);
                //讀取資料
                try {
                    MyResourceApi.ListByUser api = myApiService.listByUser(uri.getLastPathSegment());
                    api.setCursor(nextPageToken);
                    int pageLimit = 100;
                    api.setLimit(pageLimit);    //設定最大讀取筆數
                    CollectionResponseMyResource feed = api.execute();
                    myMergeResources = (ArrayList<MyResource>) feed.getItems();
                    nextPageToken = feed.getNextPageToken();
                } catch (IOException e) {
                    e.getMessage();
                }
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        //產生Cursor所需欄位
        //這個cursor只有兩個欄位，一個是_id，一個是Json String
        String[] columns = new String[]{"_id", "myresource"};//ListView需要一個固定欄位_id
        //Field[] fields = MyResource.class.getDeclaredFields();
        //利用class的Attribute作為欄位
        /*for (Field field : fields) {
            columns.add(field.getName());
            //field.getType().getSimpleName()
        }*/
        //產生值
        //Param1 欄位資料, Param2 row的筆數
        if (myMergeResources != null) {
            //設定欄位名稱
            //一個固定Object欄位名稱，看有幾筆Object
            myResourceCursor = new MatrixCursor(columns);//, myMergeResources.size());

            for (int i = 0; i < myMergeResources.size(); i++) {
                myResourceCursor.addRow(new Object[]{i + 1, new Gson().toJson(myMergeResources.get(i))});
            }
            myResourceCursor.setNotificationUri(getContext().getContentResolver(), uri);
            return myResourceCursor;
        } else {
            return null;
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Field[] fields = MyResource.class.getDeclaredFields();
        MyResource myResource = new MyResource();
        //產生ObjectRelationMapping所需資料
        for (Field field : fields) {
            myResource.put(field.getName(), values.get(field.getName()));
        }

        switch (uriMatcher.match(uri)) {
            //多筆更新
            case MYRESOURCE_ALLROWS:
                //單筆更新
            case MYRESOURCE_ONEROW:
                try {
                    MyResourceApi.Insert api = myApiService.insert(myResource);
                    api.execute();
                } catch (IOException e) {
                    e.getMessage();
                }
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        Uri insertUri = ContentUris.withAppendedId(uri, 0);
        getContext().getContentResolver().notifyChange(insertUri, null);
        return insertUri;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        Field[] fields = MyResource.class.getDeclaredFields();
        MyResource myResource = new MyResource();
        MyResource result = null;
        //產生ObjectRelationMapping所需資料
        for (Field field : fields) {
            myResource.put(field.getName(), values.get(field.getName()));
        }

        switch (uriMatcher.match(uri)) {
            //多筆更新
            case MYRESOURCE_ALLROWS:
                try {
                    MyResourceApi.Update api = myApiService.update(selectionArgs[0], myResource);
                    result = api.execute();
                } catch (IOException e) {
                    e.getMessage();
                }
                break;
            //單筆更新
            case MYRESOURCE_ONEROW:
                try {
                    MyResourceApi.Update api = myApiService.update(uri.getLastPathSegment(), myResource);
                    result = api.execute();
                } catch (IOException e) {
                    e.getMessage();
                }
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        //一般程序
        getContext().getContentResolver().notifyChange(uri, null);
        return result != null ? 1 : 0;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        switch (uriMatcher.match(uri)) {
            case MYRESOURCE_ALLROWS:
                try {
                    for (String selectionArg : selectionArgs) {
                        MyResourceApi.Remove api = myApiService.remove(selectionArg);
                        api.execute();
                    }
                } catch (IOException e) {
                    e.getMessage();
                }
                break;
            case MYRESOURCE_ONEROW:
                try {
                    MyResourceApi.Remove api = myApiService.remove(uri.getLastPathSegment());
                    api.execute();
                } catch (IOException e) {
                    e.getMessage();
                }
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return uriMatcher.match(uri) == MYRESOURCE_ALLROWS ? selectionArgs.length : 1;
    }

    /*private String getTableName(Uri uri) {
        switch(uriMatcher.match(uri)) {
            case MYRESOURCE_ALLROWS:
            case MYRESOURCE_ONEROW:
                return "MyResource";
        }
        return null;
    }*/

    @Override
    public String getType(Uri uri) {
        return null;
    }

}


/*

String[] columns = new String[] { "_id", "item", "description" };

MatrixCursor matrixCursor= new MatrixCursor(columns);
startManagingCursor(matrixCursor);  //调用这个方法,就是将获得的Cursor对象交与Activity 来管理，这样Cursor对象的生命周期便能与当前的Activity自动同步

matrixCursor.addRow(new Object[] { 1, "Item A", "...." });

SimpleCursorAdapter adapter =
        new SimpleCursorAdapter(this, R.layout.layout_row, matrixCursor, ...);

setListAdapter(adapter);

 */