package tw.binary.dipper;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;

import tw.binary.dipper.api.myResourceApi.model.ResourceImg;
import tw.binary.dipper.util.MyUtils;
import tw.binary.dipper.util.MyUtilsGcs;
import tw.binary.dipper.util.YesNoDialogFrag;

public class ResPicEditActivity extends ToolbarBaseActivity implements YesNoDialogFrag.Communicator
        , View.OnClickListener, View.OnLongClickListener {
    private ImageView ivImgMain;
    private HorizontalScrollView hsv;
    private LinearLayout ll;
    private ActionMode mActionMode;
    private ArrayList<ResourceImg> mImages = new ArrayList<ResourceImg>();
    private String mResourceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            //Download all images for first timw
            //ReadImagesAsyncTask readImagesAsyncTask = new ReadImagesAsyncTask(this);
            //readImagesAsyncTask.execute(mImages);
        }
        findViewId();
        genImageViews();    //取得intent 傳的圖片
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //outState.putSerializable();
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_res_pic_edit;
    }

    protected void findViewId() {
        ll = (LinearLayout) findViewById(R.id.resource_pic_gallery);
        hsv = (HorizontalScrollView) findViewById(R.id.horizontalScrollView);
        ivImgMain = (ImageView) findViewById(R.id.imgMain); //主要圖片
    }

    private void genImageViews() {
        Bitmap bmp;
        JSONArray imagesJsonArray;

        ll.removeAllViewsInLayout();
        Bundle bundle = getIntent().getExtras();
        mResourceId = bundle.getString("ResourceId");
        try {
            imagesJsonArray = new JSONArray(bundle.getString("Images"));
            //ArrayList<HashMap<String,String>> listImgs =(ArrayList<HashMap<String,String>>)bundle.getSerializable("Images");
            //把HashMap的圖片轉到ArrayList
            for (int i = 0; i < imagesJsonArray.length(); i++) {
                //更新ViewImages
                bmp = MyUtils.readImageFile(this, imagesJsonArray.getJSONObject(i).get("filename").toString());
                addImageViews(bmp);
                //更新主視圖片
                if (i == 0)
                    ivImgMain.setImageBitmap(bmp);
                //轉到ArrayList
                ResourceImg mResourceImg = new ResourceImg();
                mResourceImg.setFilename(imagesJsonArray.getJSONObject(i).get("filename").toString());
                mResourceImg.setComment(imagesJsonArray.getJSONObject(i).get("comment").toString());
                mImages.add(mResourceImg);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void addImageViews(Bitmap bmp) {
        final int padding_in_dp = 10;
        final float scale = getResources().getDisplayMetrics().density;
        final int padding_in_px = (int) (padding_in_dp * scale + 0.5f);
        final ImageView iv_gallery = new ImageView(this);
        //要先產生Layout 後續設定才有用
        ll.addView(iv_gallery);
        iv_gallery.setScaleType(ImageView.ScaleType.FIT_CENTER);
        iv_gallery.getLayoutParams().width = MyUtils.dpToPx(this, 150);
        iv_gallery.getLayoutParams().height = MyUtils.dpToPx(this, 100);
        iv_gallery.setBackgroundColor(getResources().getColor(R.color.caldroid_gray));
        if (bmp != null)
            iv_gallery.setImageBitmap(bmp);

        iv_gallery.setOnClickListener(this);
        iv_gallery.setOnLongClickListener(this);

        // Scrolling to new added ImageView
        hsv.post(new Runnable() {
            @Override
            public void run() {
                // Scroll to the bottom.
                hsv.scrollTo(iv_gallery.getLeft(), iv_gallery.getTop());
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
        if (intent != null) {
            Bundle extras = intent.getExtras();
            if (responseCode == RESULT_OK) {
                switch (requestCode) {
                    case 1: //browse picture
                    case 2: //take picture
                        //取出拍照後回傳資料
                        Bitmap bmp = (Bitmap) extras.get("data");
                        if (bmp != null) {
                            addImageViews(bmp);
                            ResourceImg resourceImg = new ResourceImg();
                            //產生Image Filename
                            Random generator = new Random();
                            int n = 99999;
                            n = generator.nextInt(n);
                            String filename = mResourceId + n + ".jpg";
                            //存檔
                            MyUtils.saveImageFile(this, bmp, filename);
                            resourceImg.setFilename(filename);     //紀錄檔名
                            resourceImg.setComment("");
                            mImages.add(resourceImg);
                            //設定大圖顯示
                            ivImgMain.setImageBitmap(bmp);
                        }
                        break;
                    default:
                }
            }
        }
        super.onActivityResult(requestCode, responseCode, intent);
    }

    public void onClick(View v) {
        if (v != null) {
            updateMainImage(v); //點選小圖，設定大圖顯示
        }
    }

    private void updateMainImage(View v) {
        TextView tv = (TextView) findViewById(R.id.hsv_removeintro);
        if (v != null) {
            //設定大圖顯示
            v.buildDrawingCache();
            //要copy一份新的bitmap，這樣大圖才不會跟著連動(指向同一個記憶體)
            Bitmap bmp = Bitmap.createBitmap(v.getDrawingCache());
            ivImgMain.setImageBitmap(bmp);
            tv.setText(R.string.hsv_removeintro);
        } else {
            tv.setText("");
            ivImgMain.setImageResource(R.drawable.camera_plus_icon2);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.resource_pic_edit_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent();
        switch (item.getItemId()) {
            case R.id.browsePic:
                /* 开启Pictures画面Type设定为image */
                intent.setType("image/*");
                /* 使用Intent.ACTION_GET_CONTENT这个Action */
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, 1);
                break;
            case R.id.takePic:
                intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, 2);
                break;
            case R.id.removePic:
                confirmRemovePic();
                break;
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    private void confirmRemovePic() {
        YesNoDialogFrag mAlert = YesNoDialogFrag.newInstance("注意", "確定刪除照片嗎？");
        mAlert.show(getFragmentManager(), "dialog");
    }

    private void removeSelectedPic() {
        for (int i = ll.getChildCount(); i > 0; i--) {
            if (ll.getChildAt(i - 1).isSelected()) {
                ll.removeView(ll.getChildAt(i - 1));
                File mFile = new File(getFilesDir().getAbsolutePath(), mImages.get(i - 1).getFilename());
                mFile.delete(); //如果檔案已經存在了，要先刪除
                mImages.remove(i - 1);
                updateMainImage(ll.getChildAt(i - 2));
            }
        }
        updateMainImage(ll.getChildAt(0));
    }

    @Override
    public boolean onLongClick(View v) {
        if (v.isSelected()) {
            v.setSelected(false);
        } else {
            v.setSelected(true);
        }
        SelectRemoveCAB callback = new SelectRemoveCAB();
        mActionMode = startActionMode(callback);
        //TODO 更新selected的數字
        mActionMode.setTitle(Integer.toString(countSelected()) + "個已選擇");
        return true;
    }

    @Override
    public void onDialogResult(int message) {

        switch (message) {
            case R.string.Dialog_Cancel:
                break;
            case R.string.Dialog_Yes:
                removeSelectedPic();
                break;
        }
    }

    public int countSelected() {
        int mCount = 0;
        for (int i = 0; i < ll.getChildCount(); i++)
            if (ll.getChildAt(i).isSelected())
                mCount++;
        return mCount;
    }

    class SelectRemoveCAB implements ActionMode.Callback {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.context_select_menu, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {

            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.removePic:
                    confirmRemovePic();
                    mode.finish();
                    break;
            }

            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {

        }
    }

    private void returnMainImage() {
        Gson gson = new Gson();

        getIntent().putExtra("Images", gson.toJson(mImages));
        setResult(RESULT_OK, getIntent());
    }

    @Override
    public void onBackPressed() {
        returnMainImage();
        super.onBackPressed();  //這是硬體按鍵，一定要放最後
    }

    //資料下載Google DataStore
    // Param 1: 輸入條件   Param2: Progress    Param3: 結果
    class ReadImagesAsyncTask extends AsyncTask<ArrayList<ResourceImg>, Void, Boolean> {
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
        protected Boolean doInBackground(ArrayList<ResourceImg>... params) {
            ArrayList<ResourceImg> mImages = params[0];   //Pair 第二個值

            ///////////////////// Get images from Google Cloud Storage ///////////////////
            String downloaddPath = context.getFilesDir().getAbsolutePath() + "/";

            for (int i = 0; i < mImages.size(); i++) {
                try {
                    MyUtilsGcs.downloadMediaFile(context, mImages.get(i).getFilename(), downloaddPath);
                } catch (Exception e) {
                    //Ignore No image
                    e.getMessage();
                }
            }
            return true;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        protected void onPostExecute(Boolean result) {
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                for (int i = 0; i < mImages.size(); i++) {
                    Picasso.with(context)
                            .load(MyUtils.appDir(context) + mImages.get(i).getFilename())
                            .into(((ImageView) ll.getChildAt(i)));
                    if (i == 0) Picasso.with(context)
                            .load(MyUtils.appDir(context) + mImages.get(i).getFilename())
                            .into(ivImgMain);
                }
                mProgressDialog.dismiss();
            }
        }
    }

}
