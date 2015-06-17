package tw.binary.dipper;/* Created by eason on 2015/5/21. */

import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.google.identitytoolkit.GitkitUser;
import com.google.identitytoolkit.IdToken;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionButton;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import tw.binary.dipper.provider.MyResourceLoader;
import tw.binary.dipper.util.Constants;
import tw.binary.dipper.util.LogUtils;

public class testGaeCursorAdapter extends DrawerBaseActivity implements
        LoaderManager.LoaderCallbacks<Cursor>,
        SimpleCursorAdapter.ViewBinder {

    private static final String TAG = LogUtils.makeLogTag(ResListActivityDrawer.class);
    private SimpleCursorAdapter mAdapter;
    private ListView lvCallerList;
    private TextView tvTitle, tvDesc, tvDay3L, tvPublishedTime;
    private ImageView ivImage;
    private String localId = "User1";
    private Cursor mMyResorces = null;
    private FloatingActionButton floatingActionButton;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Must be call before adding content
        //requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setProgressBarIndeterminateVisibility(true);
        findViewId();
        mAdapter = new SimpleCursorAdapter(
                getApplicationContext(),
                R.layout.activity_res_list_item,
                null,             //要預先提供一個Placeholder給Arrayadapter內部處理用(一定要提供)
                new String[]{"myresource",
                        "myresource",
                        "myresource"},
                new int[]{R.id.ivImage,
                        R.id.tvTitle,
                        R.id.tvDesc},
                0);
        //仿SimpleCursorAdapter 的 Interface(ViewBinder)
        mAdapter.setViewBinder(this);
        lvCallerList.setAdapter(mAdapter);

        //載入資料
        getLoaderManager().initLoader(0, null, this);
        floatingActionButton.setVisibility(View.VISIBLE);
        Log.i("TAG", "onCreate!");
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_callerlist;
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        Log.i("TAG", "onSaveInstanceState!");
        /*super.onSaveInstanceState(outState, outPersistentState);
        outState.putSerializable("MyResources", mMyResources); */
        Log.d("TAG", "onSaveInstanceState");
    }

    private void findViewId() {
        Log.i("TAG", "findViewId!");

        lvCallerList = (ListView) findViewById(R.id.lvCallerList);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        //localId = AccountUtils.getLocalId(getApplicationContext());
        //Plus button
        ImageView iv_PlusButton = new ImageView(this);
        iv_PlusButton.setImageResource(R.drawable.add_schedule_button_icon_unchecked);
        floatingActionButton = new FloatingActionButton
                .Builder(this)
                .setContentView(iv_PlusButton)
                .setBackgroundDrawable(R.drawable.btnadd)
                .build();
        floatingActionButton.setOnClickListener(this);
        floatingActionButton.setTag("btAdd");
        floatingActionButton.setVisibility(View.GONE);
    }

    protected void updateUi() {
        Log.i("TAG", "updateUi!");
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
        Log.i("TAG", "onClick!");
        super.onClick(v);

        switch (v.getTag().toString()) {
            case "btAdd":
                //startResourceDetail(this,null);
                break;
        }
    }

    // Menu 選單
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i("TAG", "onOptionsItemSelected!");
        switch (item.getItemId()) {
            case R.id.logout:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.i("TAG", "onCreateLoader!");
        mProgressBar.setVisibility(View.VISIBLE);
        return new MyResourceLoader(getApplicationContext(), new String[]{localId});
    }

    @Override
    public void onLoadFinished(Loader<Cursor> pLoader, Cursor pCursor) {
        switch (pLoader.getId()) {
            case 0:
                mAdapter.swapCursor(pCursor);
                Log.i("TAG", "onLoadFinished!");
                break;
        }
        mProgressBar.setVisibility(View.GONE);
        //setProgressBarIndeterminateVisibility(false);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> pLoader) {
        Log.i("TAG", "onLoaderReset!");
        mAdapter.swapCursor(null);
    }

    //SimpleCursorAdapter : Binds the Cursor column defined by the specified index to the specified view
    @Override
    public boolean setViewValue(View pView, Cursor pCursor, int i) {
        Log.i("TAG", "setViewValue!");

        JSONObject myResourceJsonObject = null;
        JSONArray imagesJsonArray = null;

        try {
            if (i > 0)
                myResourceJsonObject = new JSONObject(pCursor.getString(1));  //0:_id, 1:myresource欄位
            imagesJsonArray = myResourceJsonObject.getJSONArray("images");
            switch (pView.getId()) {
                case R.id.ivImage:
                    //取得整筆資料供後續明細頁處理
                    if (!myResourceJsonObject.isNull("id"))
                        pView.setTag(myResourceJsonObject);
                    if (!myResourceJsonObject.isNull("images")) {
                        /*String photoUrl = jsonObject.getString("imageFilename");*/
                        String photoUrl = imagesJsonArray.getJSONObject(0).getString("filename");
                        photoUrl = Constants.GCS_PUBLIC_BUCKET_URL + photoUrl;
                        if (!photoUrl.isEmpty()) {
                            Picasso.with(getApplicationContext())
                                    .load(photoUrl)
                                    .fit()
                                    /*.placeholder(R.drawable.placeholder)*/
                                    .into((ImageView) pView);
                        }
                        pView.setOnClickListener(new View.OnClickListener() {
                            //設定圖片點選後打開明細頁
                            @Override
                            public void onClick(View pView) {
                                Log.i("TAG", "Picture item Click!");
                                // 呼叫明細頁，要取得整個jsonObject
                                Intent mIntent = new Intent(getApplicationContext(), ResDetailActivity.class);
                                mIntent.putExtra("MyResource", pView.getTag().toString());
                                //開啟明細頁
                                startActivity(mIntent);
                            }
                        });
                    }
                    break;
                case R.id.tvTitle:
                    if (!myResourceJsonObject.isNull("title"))
                        ((TextView) pView).setText(myResourceJsonObject.getString("title"));
                    break;
                case R.id.tvDesc:
                    if (!myResourceJsonObject.isNull("desc"))
                        ((TextView) pView).setText(myResourceJsonObject.getString("desc"));
                    break;
                default:
                    return false;
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return true;
    }

    protected int getSelfNavDrawerItem() {
        return NAVDRAWER_ITEM_RES_LIST;
    }
}


/*
        For the application to work in offline you need to store the data in SQLLite as you are doing.
        To sync the data with your datastore, you can use SyndAdapter
        (http://developer.android.com/training/sync-adapters/creating-sync-adapter.html) and Cloud Endpoint
        to expose your datastore objects. To notify the client from the changes on the server you can use
        Google Cloud messaging (https://developer.android.com/google/gcm/index.html). */
