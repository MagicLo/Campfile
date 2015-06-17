package tw.binary.dipper;/* Created by eason on 2015/5/21. */

import android.app.LoaderManager;
import android.content.Loader;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.identitytoolkit.GitkitUser;
import com.google.identitytoolkit.IdToken;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import tw.binary.dipper.api.myResourceApi.model.MyResource;
import tw.binary.dipper.examples.MyResourceLoader;
import tw.binary.dipper.provider.MyResourceArrayAdapter;
import tw.binary.dipper.util.Constants;
import tw.binary.dipper.util.LogUtils;

public class testGaeArrayAdapter extends DrawerBaseActivity implements
        LoaderManager.LoaderCallbacks<ArrayList<MyResource>>,
        AdapterView.OnItemClickListener,
        MyResourceArrayAdapter.ViewBinder {

    private static final String TAG = LogUtils.makeLogTag(ResListActivityDrawer.class);
    private MyResourceArrayAdapter mAdapter;
    private ListView lvCallerList;
    private TextView tvTitle, tvDesc, tvDay3L, tvPublishedTime;
    private ImageView ivImage;
    private String localId;
    private ArrayList<MyResource> mMyResorce;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mMyResorce = new ArrayList<MyResource>();
        findViewId();
        mAdapter = new MyResourceArrayAdapter(getApplicationContext(),
                R.layout.activity_res_list_item,
                mMyResorce,             //要預先提供一個Placeholder給Arrayadapter內部處理用(一定要提供)
                new int[]{R.id.ivImage,
                        R.id.tvTitle,
                        R.id.tvDesc});
        //仿SimpleCursorAdapter 的 Interface(ViewBinder)
        mAdapter.setViewBinder(this);

        lvCallerList.setAdapter(mAdapter);
        //載入資料
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_callerlist;
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        /*super.onSaveInstanceState(outState, outPersistentState);
        outState.putSerializable("MyResources", mMyResources);
        Log.d("TAG", "onSaveInstanceState");*/
    }

    private void findViewId() {
        lvCallerList = (ListView) findViewById(R.id.lvCallerList);
        lvCallerList.setOnItemClickListener(this);
        //localId = AccountUtils.getLocalId(getApplicationContext());
        localId = "User2";
    }

    protected void updateUi() {
    }


    @Override
    protected boolean isAuthNeeded() {
        return true;
    }

    @Override
    public void onAuthSuccess(IdToken idToken, GitkitUser gitkitUser) {
        super.onAuthSuccess(idToken, gitkitUser);
    }

    @Override
    public void onAuthFailure() {
        super.onAuthFailure();
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);

        switch (v.getTag().toString()) {
            case "btAdd":
                //startResourceDetail(this,null);
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> pAdapterView, View pView, int i, long l) {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public Loader<ArrayList<MyResource>> onCreateLoader(int id, Bundle args) {
        return new MyResourceLoader(getApplicationContext(),
                new String[]{localId});
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<MyResource>> pLoader, ArrayList<MyResource> pMyResources) {
        //下載完成更新Adapter
        mAdapter.swapData(pMyResources);
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<MyResource>> loader) {
        //清除Adapter
        mAdapter.swapData(null);
    }

    @Override
    public void setViewValue(View view, MyResource pMyResource) {
        switch (view.getId()) {
            case R.id.ivImage:
                String photoUrl = Constants.GCS_PUBLIC_BUCKET_URL + pMyResource.getImages().get(0).getFilename();
                if (photoUrl != null && !photoUrl.isEmpty()) {
                    Picasso.with(getApplicationContext())
                            .load(photoUrl)
                            .fit()
                            .placeholder(R.drawable.placeholder)
                            .into((ImageView) view);
                }
                break;
            case R.id.tvTitle:
                ((TextView) view).setText(pMyResource.getTitle());
                break;
            case R.id.tvDesc:
                ((TextView) view).setText(pMyResource.getDesc());
                break;
        }
    }
}


/*
        For the application to work in offline you need to store the data in SQLLite as you are doing.
        To sync the data with your datastore, you can use SyndAdapter
        (http://developer.android.com/training/sync-adapters/creating-sync-adapter.html) and Cloud Endpoint
        to expose your datastore objects. To notify the client from the changes on the server you can use
        Google Cloud messaging (https://developer.android.com/google/gcm/index.html). */
