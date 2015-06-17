package tw.binary.dipper;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import tw.binary.dipper.api.myResourceApi.MyResourceApi;
import tw.binary.dipper.api.myResourceApi.model.MyResource;
import tw.binary.dipper.api.myResourceApi.model.ResourceImg;
import tw.binary.dipper.provider.GAEContentProvider;
import tw.binary.dipper.util.AccountUtils;
import tw.binary.dipper.util.Constants;
import tw.binary.dipper.util.MyUtils;
import tw.binary.dipper.util.MyUtilsDate;
import tw.binary.dipper.util.MyUtilsGcs;

public class ResDetailActivity extends ToolbarBaseActivity implements
        View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    private final static int EDIT_RES_PIC = 9000;
    private final static int EDIT_TITLE = 9001;
    private final static int EDIT_DESC = 9002;
    private final static int GET_MAP = 9003;
    private final static float MAPZOOM = 11f;
    private MyResource mMyResource;
    //private ArrayList<ResourceImg> mImages = new ArrayList<>();
    private JSONArray imagesJsonArray = null;
    private JSONObject myResourceJsonObject = null;
    private TextView mTitle, mDesc;
    private GoogleMap mMap;
    private ImageView mImageMain;
    private Marker mMarker = null;
    private Button btnSave;
    private ToggleButton tbPublished;
    private Switch swPublished;
    Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //requestWindowFeature(Window.FEATURE_PROGRESS);
        super.onCreate(savedInstanceState);
        findViewId();

        if (savedInstanceState == null) {
            Bundle mBundle = getIntent().getExtras();
            //取得參數資料
            getBundleResource(mBundle);
            //初次執行
            updateUI();
            btnSave.setEnabled(false);
        } else {
            //再次執行
            getBundleResource(savedInstanceState);
        }

        setUpMapIfNeeded(); //設定GoogleMap
        prepareImages();
    }

    private void updateUI() {
        //將mResource 和 Images update UI
        mTitle.setText(mMyResource.getTitle());
        mDesc.setText(mMyResource.getDesc());
        //Published ToggleButton
        String mPublishedTime = mMyResource.getPublishedTime();
        tbPublished.setChecked(mPublishedTime != null && !mPublishedTime.equals(""));
        swPublished.setChecked(mPublishedTime != null && !mPublishedTime.equals(""));
        Picasso.with(this)
                .load(Constants.GCS_PUBLIC_BUCKET_URL + mMyResource.getImageFilename())
                .placeholder(R.drawable.placeholder)
                .into(mImageMain);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        String extraResource;
        ArrayList<ResourceImg> images;

        if (mMyResource != null) {
            extraResource = MyUtils.objToJson(mMyResource);
            images = (ArrayList<ResourceImg>) mMyResource.getImages();
        } else {
            extraResource = "";
            images = null;
        }

        outState.putString("Resource", extraResource);
        outState.putSerializable("Images", images);
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_res_detail;
    }

    protected void findViewId() {
        //設定代表圖片
        mImageMain = (ImageView) findViewById(R.id.imgMain);
        mImageMain.setOnClickListener(this);
        //設定標題
        mTitle = (TextView) findViewById(R.id.tv_ResTitle);
        //設定描述
        mDesc = (TextView) findViewById(R.id.tv_ResDesc);
        //設定Publish Button
        btnSave = (Button) findViewById(R.id.btnSave);
        btnSave.setOnClickListener(this);
        (findViewById(R.id.tv_EditTitle)).setOnClickListener(this);
        (findViewById(R.id.tvGetMaps)).setOnClickListener(this);
        //Published ToggleButton
        tbPublished = ((ToggleButton) findViewById(R.id.tbPublished));
        tbPublished.setOnCheckedChangeListener(this);
        //Published Switch
        swPublished = ((Switch) findViewById(R.id.swPublished));
        swPublished.setOnCheckedChangeListener(this);
    }

    protected void getBundleResource(Bundle mBundle) {
        try {
            myResourceJsonObject = new JSONObject(mBundle.getString("MyResource"));
            mMyResource = gson.fromJson(myResourceJsonObject.toString(), MyResource.class);
            imagesJsonArray = myResourceJsonObject.getJSONArray("images");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //if UserId is null or space, Create empty resource
        if (mMyResource != null) {
            //ResourceImg mResourceImg = new ResourceImg();
            /*if(mMyResource.getImageFilename() != null)
                mResourceImg.setFilename(mMyResource.getImageFilename());
            if(mMyResource.getImageComment() != null)
                mResourceImg.setComment(mMyResource.getImageComment());
            mImages.add(mResourceImg);//儲存第一張照片*/
        } else {
            mMyResource = new MyResource();
            //初始化
            mMyResource.setId(MyUtils.uuid());
            mMyResource.setUserId(AccountUtils.getLocalId(this));
            mMyResource.setTitle("吸引人的標題");
            mMyResource.setDesc("清楚的說明文字");
            mMyResource.setLat(0d);
            mMyResource.setLng(0d);
            mMyResource.setImages(new ArrayList<ResourceImg>());
        }
    }

    private void prepareImages() {
        ReadImagesAsyncTask readImagesAsyncTask = new ReadImagesAsyncTask(this);
        readImagesAsyncTask.execute();
    }

    //處理所有的OnClickListener
    public void onClick(View v) {
        Intent mIntent;
        switch (v.getId()) {
            case R.id.imgMain:
                mIntent = new Intent(this, ResPicEditActivity.class);
                mIntent.putExtra("Images", imagesJsonArray.toString()); //改傳Json格式
                mIntent.putExtra("ResourceId", mMyResource.getId());
                startActivityForResult(mIntent, EDIT_RES_PIC);
                break;
            case R.id.tv_EditTitle:
                break;
            case R.id.tv_EditDesc:
                break;
            case R.id.tvGetMaps:
                mIntent = new Intent(this, GetMapsActivity.class);
                mIntent.putExtra("Lat", String.valueOf(mMyResource.getLat()));
                mIntent.putExtra("Lng", String.valueOf(mMyResource.getLng()));
                startActivityForResult(mIntent, GET_MAP);
                break;
            /*case R.id.btnSave:
                updateResource();
                break;*/
            case R.id.btnSave:
                mMyResource.setModifiedTime(MyUtilsDate.CurrentDateTime());
                /////////////////////////上傳GAE & GCS
                InsertMyResourceAsyncTask insertMyResource = new InsertMyResourceAsyncTask(this);
                insertMyResource.execute();
                //if(publishResource()) onBackPressed();
                break;
        }

    }

    private boolean deleteLocalResourceFiles() {
        //刪除Local Jason File
        if (!MyUtils.removeFile(this, mMyResource.getId() + ".res")) {
            Toast.makeText(this, getResources().getString(R.string.eraseResourceError), Toast.LENGTH_SHORT).show();
            return false;
        }
        //刪除Local Pictures
        for (int i = 0; i < imagesJsonArray.length(); i++) {
            try {
                if (!MyUtils.removeFile(this, imagesJsonArray.getJSONObject(i).get("filename").toString())) {
                    Toast.makeText(this, getResources().getString(R.string.eraseResourceError), Toast.LENGTH_SHORT).show();
                    return false;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
        switch (requestCode) {
            case EDIT_RES_PIC:
                if (responseCode == RESULT_OK) {
                    Bundle bundle = intent.getExtras();
                    try {
                        imagesJsonArray = new JSONArray(bundle.getString("Images"));
                        if (imagesJsonArray.length() > 0) {
                            //更新主照片
                            mImageMain.setImageBitmap(MyUtils.readImageFile(this, imagesJsonArray.getJSONObject(0).get("filename").toString()));
                        } else {
                            //沒照片放示意圖
                            mImageMain.setImageDrawable(getResources().getDrawable(R.drawable.placeholder));
                        }
                        //同步Images
                        mMyResource.setImages(gson.fromJson(imagesJsonArray.toString(), new ArrayList<ResourceImg>().getClass()));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    //ArrayList<HashMap<String, String>> listImgs = (ArrayList<HashMap<String, String>>) bundle.getSerializable("Images");
                    /*if (listImgs.size() > 0) {
                        mImageMain.setImageBitmap(MyUtils.readImageFile(this, listImgs.get(0).get("filename")));
                    } else {
                        mImageMain.setImageDrawable(getResources().getDrawable(R.drawable.camera_plus_icon2));
                    }
                    mImages.clear();*/
                    /*for (int i = 0; i < listImgs.size(); i++) {
                        ResourceImg mResourceImg = new ResourceImg();
                        mResourceImg.setFilename(listImgs.get(i).get("filename"));
                        mResourceImg.setComment(listImgs.get(i).get("comment"));
                        mImages.add(mResourceImg);
                    }
                    mMyResource.setImages(mImages); //同步Images*/
                }
                break;
            case EDIT_TITLE:
                if (responseCode == RESULT_OK) {
                }
                break;
            case EDIT_DESC:
                if (requestCode == RESULT_OK) {

                }
                break;
            case GET_MAP:
                if (responseCode == RESULT_OK) {
                    mMyResource.setLat(Double.parseDouble(intent.getStringExtra("Lat")));
                    mMyResource.setLng(Double.parseDouble(intent.getStringExtra("Lng")));
                    gotoLocation(mMyResource.getLat(), mMyResource.getLng(), MAPZOOM);
                }
                break;
        }
        btnSave.setEnabled(true);        //可發佈
        super.onActivityResult(requestCode, responseCode, intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.resource_detail_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.res_nav:
                resourceNavigation();
                break;
            case R.id.remove_resource:
                break;
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    private void resourceNavigation() {
        String toGPStr = String.valueOf(mMyResource.getLat()) + ","
                + String.valueOf(mMyResource.getLng());
        Uri uri = Uri.parse("google.navigation:q=" + toGPStr + "&mode=d");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        //intent.setPackage("com.google.android.apps.maps");    //直接使用Google map if needed
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(this, getResources().getString(R.string.navigationIsNotAvailable), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded() {
        if (mMap == null) {
            mMap = ((MapFragment) getFragmentManager()
                    .findFragmentById(R.id.map))
                    .getMap();
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {
        if (mMyResource.getLat() == null || mMyResource.getLng() == null) {
            LatLng latlng;
            latlng = getLocation();
            mMyResource.setLat(latlng.latitude);
            mMyResource.setLng(latlng.longitude);
        } else {
            gotoLocation(mMyResource.getLat(), mMyResource.getLng(), MAPZOOM);
        }
    }

    private LatLng getLocation() {
        double lat = 0d;
        double lng = 0d;

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // Create a criteria object to retrieve provider
        Criteria criteria = new Criteria();
        // Get the name of the best provider
        String provider = locationManager.getBestProvider(criteria, true);
        // Get current location
        Location location = locationManager.getLastKnownLocation(provider); // 設定定位資訊由 GPS提供

        if (location == null) {
            Toast.makeText(this, "Current location is not available", Toast.LENGTH_SHORT).show();
        } else {
            lat = location.getLatitude();  // 取得經度
            lng = location.getLongitude(); // 取得緯度
            // 定位到現在位置
            gotoLocation(lat, lng, MAPZOOM);
        }
        return new LatLng(lat, lng);
    }

    private void gotoLocation(double lat, double lng, float pMAPZOOM) {
        LatLng ll = new LatLng(lat, lng);
        if (mMarker != null)
            mMarker.remove();
        mMarker = mMap.addMarker(new MarkerOptions().position(ll).title("現在位置").draggable(true));
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(ll, pMAPZOOM);
        mMap.moveCamera(cameraUpdate);
        showMarkerInfo(mMarker);
    }

    private void showMarkerInfo(Marker pMarker) {
        pMarker.setSnippet("Lat:" + String.valueOf(pMarker.getPosition().latitude) +
                "  Lng:" + String.valueOf(pMarker.getPosition().longitude));
        Geocoder geocoder = new Geocoder(this);
        List<Address> addressList;
        LatLng position = pMarker.getPosition();
        try {
            addressList = geocoder.getFromLocation(position.latitude, position.longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        if (addressList.size() > 0) {
            Address address = addressList.get(0);
            pMarker.setTitle(address.getAddressLine(0));
            pMarker.showInfoWindow();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    //暫時沒用
    /*
    private boolean updateResource() {
        if (MyUtils.string2File(this, MyUtils.objToJson(mMyResource), mMyResource.getId() + ".res")) {
            Toast.makeText(this, getResources().getString(R.string.successfulUpdate), Toast.LENGTH_SHORT).show();
            return true;
        } else {
            Toast.makeText(this, getResources().getString(R.string.failureUpdate), Toast.LENGTH_SHORT).show();
            return false;
        }
    }
*/
    public boolean publishResource() {
        /////////////////////////上傳GAE & GCS
        InsertMyResourceAsyncTask insertMyResource = new InsertMyResourceAsyncTask(this);
        insertMyResource.execute();

        try {
            //////////取得AsyncTask執行結果
            if (insertMyResource.get()) {
                deleteLocalResourceFiles();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        btnSave.setEnabled(true);        //可發佈
        // Published ToggleButton
        if (isChecked) {
            mMyResource.setPublishedTime(MyUtilsDate.CurrentDateTime());
        } else {
            mMyResource.setPublishedTime("");
        }
    }

    // 資料上傳Google DataStore
    // Param 1: 輸入條件   Param2: Progress    Param3: 結果
    class InsertMyResourceAsyncTask extends AsyncTask<Void, Void, Boolean> {
        private MyResourceApi myApiService = null;
        private Context context;
        private ProgressDialog mProgressDialog;

        protected InsertMyResourceAsyncTask(Context context) {
            this.context = context;
            mProgressDialog = ProgressDialog.show(context, "", getString(R.string.system_processing));
        }

        //上傳至GAE
        @Override
        protected Boolean doInBackground(Void... params) {
            ///////////////////// Upload to Google Cloud Storage ///////////////////
            String uploadPath = context.getFilesDir().getAbsolutePath() + "/";
            for (int i = 0; i < imagesJsonArray.length(); i++) {
                try {
                    if (!imagesJsonArray.getJSONObject(i).isNull("filename"))
                        MyUtilsGcs.uploadFile(context, uploadPath + imagesJsonArray.getJSONObject(i).get("filename").toString());
                } catch (Exception e) {
                    e.getMessage();
                    return false;    //失敗
                }
            }
            ///////////////////// Upload to GAE /////////////////////////
            //用Content Provider 新增資料太麻煩了
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
                //通知說資料有異動
                MyResource result = myApiService.insert(mMyResource).execute();
                if (result != null)
                    getApplicationContext().getContentResolver().notifyChange(GAEContentProvider.CONTENT_URI_MYRESOURCE, null);
                return result != null;
            } catch (IOException e) {
                e.getMessage();
                return false;    //失敗
            }
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }


        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (mProgressDialog != null && mProgressDialog.isShowing()) mProgressDialog.dismiss();
            if (result) {
                btnSave.setEnabled(false);
                Toast.makeText(context, "儲存成功", Toast.LENGTH_LONG).show();
            } else {
                btnSave.setEnabled(true);
                Toast.makeText(context, "儲存失敗", Toast.LENGTH_LONG).show();
            }
        }
    }

    //資料下載Google DataStore
    // Param 1: 輸入條件   Param2: Progress    Param3: 結果
    class ReadImagesAsyncTask extends AsyncTask<Void, Void, Boolean> {
        private Context context;
        private ProgressDialog mProgressDialog;

        protected ReadImagesAsyncTask(Context context) {
            this.context = context;
            //暫時不用
            mProgressDialog = ProgressDialog.show(context, "", getString(R.string.system_processing));
        }

        //Download from GAE & Local
        //params:Arraylist<Images>
        //Return null
        @Override
        protected Boolean doInBackground(Void... params) {
            //ArrayList<ResourceImg> mImages = params[0];   //Pair 第二個值

            ///////////////////// Get images from Google Cloud Storage ///////////////////
            String downloaddPath = context.getFilesDir().getAbsolutePath() + "/";

            for (int i = 0; i < imagesJsonArray.length(); i++) {
                try {
                    String fileName = imagesJsonArray.getJSONObject(i).getString("filename");
                    //先檢查Local是否已有檔案
                    if (!MyUtils.isFileExist(context, fileName)) {
                        MyUtilsGcs.downloadMediaFile(context, fileName, downloaddPath);
                        Log.i("TAG", "Images downloaded from Google bucket!");
                    }
                } catch (Exception e) {
                    //Ignore No image
                    Log.i("TAG", "Images download fail! " + e.getMessage());
                }
            }
            Log.i("TAG", "Images Prepared!");
            return true;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        protected void onPostExecute(Boolean result) {
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
        }
    }
}