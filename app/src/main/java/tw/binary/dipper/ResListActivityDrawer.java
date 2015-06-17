package tw.binary.dipper;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;
import com.google.identitytoolkit.GitkitUser;
import com.google.identitytoolkit.IdToken;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionButton;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;

import tw.binary.dipper.api.myResourceApi.MyResourceApi;
import tw.binary.dipper.api.myResourceApi.model.CollectionResponseMyResource;
import tw.binary.dipper.api.myResourceApi.model.MyResource;
import tw.binary.dipper.api.myResourceApi.model.ResourceImg;
import tw.binary.dipper.util.AccountUtils;
import tw.binary.dipper.util.Constants;
import tw.binary.dipper.util.LogUtils;
import tw.binary.dipper.util.MyUtils;

// Created by eason on 2015/1/27.
public class ResListActivityDrawer extends DrawerBaseActivity implements SwipeRefreshLayout.OnRefreshListener {
    private static final String TAG = LogUtils.makeLogTag(ResListActivityDrawer.class);
    private ArrayList<MyResource> mMyResources;
    private String mUserId = "User2";   //測試用，之後用mCFUser的資料代替
    private RecyclerView mResourceListView;
    private ResListAdapter mResListAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private FloatingActionButton floatingActionButton;
    //private final static int RES_DETAIL = 9000;   //for onActivityResult()

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_res_list);

        //getActionBarToolbar();
        findViewId();
        floatingActionButton.setVisibility(View.VISIBLE);
        onRefresh();
        /*if (savedInstanceState != null) {
            mMyResources = (ArrayList<MyResource>) savedInstanceState.getSerializable("MyResources");
        } else {
            loadResources();
        }*/
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_res_list;
    }

    /*@Override
    protected void onStart() {
        super.onStart();

        if(isAuthNeeded() && !mLoginAndAuthHelper.isUserLoggedIn()){
            //啟動Google Identity Toolkit
            LogUtils.LOGD(TAG, "Creating and starting new Helper");
            mLoginAndAuthHelper.startSignIn();
        }else{
            updateUi();
        }
    }*/

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putSerializable("MyResources", mMyResources);
        Log.d("TAG", "onSaveInstanceState");
    }

    private void findViewId() {
        //ListView
        mResourceListView = (RecyclerView) findViewById(R.id.resourceListView);
        mResListAdapter = new ResListAdapter(this);
        mResourceListView.setAdapter(mResListAdapter);
        mResourceListView.setLayoutManager(new LinearLayoutManager(this));

        //SwipeLayout
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeLayout);
        mSwipeRefreshLayout.setOnRefreshListener(this);

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
        //floatingActionButton.setVisibility(View.VISIBLE);
        //onRefresh();
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        //Check by Id 暫時沒用到
        //switch (v.getId()) { }
        //Check by Tag
        switch (v.getTag().toString()) {
            case "btAdd":
                startResourceDetail(this, null);
                break;
        }
    }

    protected int getSelfNavDrawerItem() {
        return NAVDRAWER_ITEM_RES_LIST;
    }

    @Override
    protected boolean isAuthNeeded() {
        return true;
    }

    @Override
    public void onRefresh() {
        //Load Resources again, incase it's dirty
        loadResources();
        mResListAdapter.notifyDataSetChanged();
        if (mSwipeRefreshLayout.isRefreshing()) mSwipeRefreshLayout.setRefreshing(false);
    }

    private boolean loadResources() {
        //依據User Id 取得Cloud & LocalResources
        //call AsyncTask to get data and return it
        ReadMyResourceAsyncTask readMyResourceAsyncTask = new ReadMyResourceAsyncTask(this);
        //readMyResourceAsyncTask.execute(mCFUser.getLocalId());
        readMyResourceAsyncTask.execute(AccountUtils.getLocalId(this));
        try {
            return readMyResourceAsyncTask.get(); //取得AsyncTask執行結果
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
    }

    ///////////////////      資料上傳Google DataStore        /////////////////////
    // Param 1: 輸入條件   Param2: Progress    Param3: 結果
    class ReadMyResourceAsyncTask extends AsyncTask<String, Void, Boolean> {
        private MyResourceApi myApiService = null;
        private Context context;
        private ProgressDialog mProgressDialog;

        protected ReadMyResourceAsyncTask(Context context) {
            this.context = context;
            mProgressDialog = ProgressDialog.show(context, "", getString(R.string.system_processing));
        }

        //Download from GAE & Local
        //params:UserID
        //Return ArrayList<MyResource>
        @Override
        protected Boolean doInBackground(String... params) {
            String userID = params[0];   //Pair 第二個值
            String nextPageToken = "";
            int pageLimit = 100;

            ///////////////////// get from GAE /////////////////////////
            if (myApiService == null) {
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
                MyResourceApi.ListByUser api = myApiService.listByUser(userID);
                api.setCursor(nextPageToken);
                api.setLimit(pageLimit);    //設定最大讀取筆數
                CollectionResponseMyResource feed = api.execute();
                mMyResources = (ArrayList<MyResource>) feed.getItems();
                nextPageToken = feed.getNextPageToken();
            } catch (IOException e) {
                e.getMessage();
                return false;    //失敗
            }
            return mMyResources != null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        protected void onPostExecute(Boolean result) {
            if (mProgressDialog != null && mProgressDialog.isShowing()) mProgressDialog.dismiss();
            if (!result)
                Toast.makeText(context, "無資料", Toast.LENGTH_LONG).show();
        }
    }

    ///////////////       RecycleView class      //////////////////////
    class ResListAdapter extends RecyclerView.Adapter<ResListAdapter.MyViewHolder> {
        private Context context;
        private LayoutInflater inflater;
        //List<MyResource> mMyResources;

        public ResListAdapter(Context context) {
            this.inflater = LayoutInflater.from(context);
            this.context = context;
            //this.mMyResources = data;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            //layoutView 為最外層之Layout 目前是RelativeLayout
            View layoutView = inflater.inflate(R.layout.activity_res_list_item, parent, false);
            final MyViewHolder mMyViewHolder = new MyViewHolder(layoutView);
            return mMyViewHolder;
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            MyResource currentRes = mMyResources.get(position);
            if (currentRes != null) {
                Picasso.with(context)
                        .load(Constants.GCS_PUBLIC_BUCKET_URL + currentRes.getImages().get(0).getFilename())
                        .placeholder(R.drawable.placeholder)
                        .into(holder.mImage);
                //holder.mImage.setImageBitmap(MyUtils.readImageFile(context, currentRes.getImages().get(0).getFilename()));
                holder.mTitle.setText(currentRes.getTitle());
                holder.mDesc.setText(currentRes.getDesc());
                holder.mPublishedTimeTextView.setText(currentRes.getPublishedTime());
            }
        }

        @Override
        public int getItemCount() {
            if (mMyResources != null) {
                return mMyResources.size();
            } else {
                return 0;
            }
        }

        public void onDelete(int position) {
            mMyResources.remove(position);
            notifyItemRemoved(position);
        }

        public void onAppend(MyResource pMyResource) {
            if (mMyResources != null) {
                mMyResources.add(pMyResource);
                notifyItemInserted(mMyResources.size());
            }
        }

        class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            CardView mCardView;
            ImageView mImage;
            TextView mTitle, mDesc, mPublishedTimeTextView;

            MyViewHolder(View itemView) {
                //itemView是ViewHolder
                super(itemView);
                //itemView.setOnClickListener(this);
                mCardView = (CardView) itemView.findViewById(R.id.card_view);
                //CardView
                mCardView.setOnClickListener(this);
                mCardView.setCardElevation(15f);
                mCardView.setRadius(15f);
                mImage = (ImageView) itemView.findViewById(R.id.ivImage);
                //mImage.setOnClickListener(this);
                mTitle = (TextView) itemView.findViewById(R.id.tvTitle);
                mTitle.setTextColor(Color.parseColor("#ffffff"));   //背景腰帶透明黑
                mTitle.setBackgroundColor(Color.parseColor("#88000000"));   //白色
                //mTitle.setOnClickListener(this);
                mDesc = (TextView) itemView.findViewById(R.id.tvDesc);
            }

            @Override
            public void onClick(View v) {
                startResourceDetail(context, mMyResources.get(getPosition()));
            }
        }
    }//////////////////////////////////////////////////////////////////////////

    public void startResourceDetail(Context pContext, MyResource pMyResource) {
        String extraResource;
        ArrayList<ResourceImg> images;
        Intent mIntent = new Intent(pContext, ResDetailActivity.class);

        if (pMyResource != null) {
            extraResource = MyUtils.objToJson(pMyResource);
            images = (ArrayList<ResourceImg>) pMyResource.getImages();
        } else {
            extraResource = "";
            images = null;
        }

        mIntent.putExtra("Resource", extraResource);
        //mIntent.putExtra("Images", images);
        //開啟明細頁
        startActivity(mIntent);
    }

    @Override
    public void onAuthSuccess(IdToken idToken, GitkitUser gitkitUser) {
        super.onAuthSuccess(idToken, gitkitUser);
        mUserId = AccountUtils.getLocalId(this);
        onRefresh();
    }

    @Override
    public void onAuthFailure() {
        super.onAuthFailure();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
