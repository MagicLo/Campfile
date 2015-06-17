package tw.binary.dipper.message;

import android.app.LoaderManager;
import android.app.NotificationManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.google.identitytoolkit.GitkitUser;
import com.google.identitytoolkit.IdToken;
import com.makeramen.roundedimageview.RoundedTransformationBuilder;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import tw.binary.dipper.DrawerBaseActivity;
import tw.binary.dipper.R;
import tw.binary.dipper.util.LogUtils;
import tw.binary.dipper.util.MyUtilsDate;

public class CallerListActivity extends DrawerBaseActivity implements
        LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemClickListener {

    private static final String TAG = LogUtils.makeLogTag(CallerListActivity.class);
    private SimpleCursorAdapter mAdapter;
    private ListView lvCallerList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        findViewId();
        mAdapter = new SimpleCursorAdapter(getApplicationContext(),
                R.layout.caller_list_item,
                null,
                new String[]{MessageDbHelper.MESSAGE_COL_PHOTOURL,  //選擇對應的欄位
                        MessageDbHelper.MESSAGE_COL_DISPLAYNAME,
                        "_id",                                  //ListView需要一個_id欄位
                        MessageDbHelper.MESSAGE_AGG_COUNT,
                        MessageDbHelper.MESSAGE_AGG_SENT},
                new int[]{R.id.photo,
                        R.id.displayName,
                        R.id.fromLocalId,
                        R.id.count,
                        R.id.sentTime},
                0);
        mAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                switch (view.getId()) {
                    case R.id.photo:
                        String photoUrl = cursor.getString(columnIndex);
                        view.setTag(photoUrl);
                        Transformation transformation = new RoundedTransformationBuilder()
                                .borderWidthDp(0)
                                .cornerRadiusDp(50)
                                .oval(false)
                                .build();
                        if (photoUrl != null && !photoUrl.isEmpty()) {
                            Picasso.with(getApplicationContext())
                                    .load(photoUrl)
                                    .fit()
                                    .transform(transformation)
                                    .placeholder(R.drawable.person_image_empty_64)
                                    .into((ImageView) view);
                        }
                        return true;
                    case R.id.count:    //TODO 之後將count 改成最新訊息內容
                        int count = cursor.getInt(columnIndex);
                        if (count > 0) {
                            ((TextView) view).setText(String.format("%d 新訊息", count));
                        }
                        return true;
                    case R.id.fromLocalId:
                        String fromLocalId = cursor.getString(columnIndex);
                        ((TextView) view).setText(fromLocalId);
                        return true;
                    case R.id.sentTime:
                        TextView tv = (TextView) view;
                        tv.setText(MyUtilsDate.getDisplayTime(cursor.getString(columnIndex)));
                        return true;
                }
                return false;
            }
        });

        lvCallerList.setAdapter(mAdapter);
        //載入資料
        getLoaderManager().initLoader(0, null, this);
        cancelNotification();   //cancel notification bar
    }

    /*@Override
    protected void onStart() {
        super.onStart();

        cancelNotification();
        if (isAuthNeeded() && !mLoginAndAuthHelper.isUserLoggedIn()) {
            //TODO 確認是否還需再登入
            //啟動Google Identity Toolkit
            LogUtils.LOGD(TAG, "Creating and starting new Helper");
            mLoginAndAuthHelper.startSignIn();
        } else {
            updateUi();
        }
    }*/

    private void findViewId() {
        lvCallerList = (ListView) findViewById(R.id.lvCallerList);
        lvCallerList.setOnItemClickListener(this);
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_callerlist;
    }

    protected void updateUi() {
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_caller_list, menu);
        return true;
    }

    @Override
    protected boolean isAuthNeeded() {
        return true;
    }

    protected int getSelfNavDrawerItem() {
        return NAVDRAWER_ITEM_MESSAGE;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        return id == R.id.action_settings || super.onOptionsItemSelected(item);

    }

    @Override
    public void onItemClick(AdapterView<?> pAdapterView, View pView, int i, long l) {
        Intent intent = new Intent(this, ChatActivity.class);
        //找出寄件者的LocalId，給ChatActivity顯示交談內容用的
        TextView tvFromLocalId = (TextView) pView.findViewById(R.id.fromLocalId);
        intent.putExtra(MessageDbHelper.MESSAGE_COL_FROM, tvFromLocalId.getText()); //啟動對應Caller LocalId的Chat
        //DisplayName
        TextView tvDisplayName = (TextView) pView.findViewById(R.id.displayName);
        intent.putExtra(MessageDbHelper.MESSAGE_COL_DISPLAYNAME, tvDisplayName.getText()); //啟動對應Caller DisplayName的Chat
        //照片
        ImageView ivPhoto = (ImageView) pView.findViewById(R.id.photo);
        intent.putExtra(MessageDbHelper.MESSAGE_COL_PHOTOURL, ivPhoto.getTag().toString());

        startActivity(intent);
    }

    @Override
    public void onAuthSuccess(IdToken idToken, GitkitUser gitkitUser) {
        super.onAuthSuccess(idToken, gitkitUser);
        //載入資料
        getLoaderManager().initLoader(1, null, this);
    }

    @Override
    public void onAuthFailure() {
        super.onAuthFailure();
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        //****** ContentProvider沒有使用ProjectionMap的作法
        /*CursorLoader loader = new CursorLoader(
                this,
                MessageContentProvider.CONTENT_URI_MESSAGER,            //URI
                new String[]{MessageDbHelper.MESSAGE_COL_FROM + " AS _id ",
                            MessageDbHelper.MESSAGE_COL_DISPLAYNAME,   //select 欄位
                            MessageDbHelper.MESSAGE_COL_PHOTOURL,
                            "COUNT(senttime) AS count",
                            "MAX(senttime) AS senttime"},
                null,
                null,
                MessageDbHelper.MESSAGE_COL_SENT + " DESC");             //Order 排序*/

        //****** ContentProvider有使用ProjectionMap的作法
        /*CursorLoader loader = new CursorLoader(
                this,
                MessageContentProvider.CONTENT_URI_MESSAGER,        //URI
                new String[]{MessageDbHelper.MESSAGE_COL_FROM,      //select 欄位
                        MessageDbHelper.MESSAGE_COL_DISPLAYNAME,
                        MessageDbHelper.MESSAGE_COL_PHOTOURL,
                        MessageDbHelper.MESSAGE_AGG_COUNT,
                        MessageDbHelper.MESSAGE_AGG_SENT},
                MessageDbHelper.MESSAGE_COL_READ + " is null ",   //Where 條件
                null,                                             //Where 參數，有用時需對應where的?(place holder)符號使用
                MessageDbHelper.MESSAGE_COL_SENT + " DESC");*/

        return new CursorLoader(
                getApplicationContext(),
                MessageContentProvider.CONTENT_URI_MESSAGER,        //Uri
                null,                                               //select * 所有欄位
                MessageDbHelper.MESSAGE_COL_READ + " is null "      //Where 條件
                        + " and " +
                        MessageDbHelper.MESSAGE_COL_DISPLAYNAME + " is not null ",
                null,                                               //Where 參數，有用時需對應where的?(place holder)符號使用
                MessageDbHelper.MESSAGE_COL_SENT + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        //下載完成更新Adapter
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //清除Adapter
        mAdapter.swapCursor(null);
    }

    private void cancelNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(Integer.valueOf(getString(R.string.notification_id)));
    }

}
