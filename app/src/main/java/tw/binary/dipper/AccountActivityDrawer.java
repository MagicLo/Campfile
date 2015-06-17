package tw.binary.dipper;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.identitytoolkit.GitkitUser;
import com.google.identitytoolkit.IdToken;
import com.squareup.picasso.Picasso;

import tw.binary.dipper.api.cFUserApi.model.CFUser;
import tw.binary.dipper.util.AccountUtils;
import tw.binary.dipper.util.LogUtils;
import tw.binary.dipper.util.MyUtilsDate;

// Created by eason on 2015/4/13.
public class AccountActivityDrawer extends DrawerBaseActivity implements View.OnClickListener {
    private static final String TAG = LogUtils.makeLogTag(AccountActivityDrawer.class);
    private ImageView ivPhoto;
    private EditText etDisplayName, etEmail, etPhone, etAddtess;
    private TextView tvRegId, tvLocalId;
    private Button btSave;
    //private GoogleCloudMessaging gcm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        findViewId();
        loadResources();
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_account;
    }

    private void findViewId() {
        ivPhoto = (ImageView) findViewById(R.id.ivPhotoLeft);
        etDisplayName = (EditText) findViewById(R.id.etDisplayName);
        etEmail = (EditText) findViewById(R.id.etEmail);
        etPhone = (EditText) findViewById(R.id.etPhone);
        etAddtess = (EditText) findViewById(R.id.etAddtess);
        btSave = (Button) findViewById(R.id.btSave);
        tvRegId = (TextView) findViewById(R.id.tvRegId);    //GCM RegID
        tvLocalId = (TextView) findViewById(R.id.tvLocalId);
        btSave.setOnClickListener(this);
    }


    private boolean updateCFUser() {
        CFUser cfUser = new CFUser();

        cfUser.setId(AccountUtils.getLocalId(getApplicationContext()));
        cfUser.setLastModifyTime(MyUtilsDate.CurrentDateTime());
        cfUser.setLastLoginTime(MyUtilsDate.CurrentDateTime());
        cfUser.setIdprovider(AccountUtils.getIdProvider(getApplicationContext()));
        cfUser.setDisplayName(AccountUtils.getDisplayName(getApplicationContext()));
        cfUser.setAddress(AccountUtils.getAddress(getApplicationContext()));
        cfUser.setEmail(AccountUtils.getEmail(getApplicationContext()));
        cfUser.setPhotoURL(AccountUtils.getPhotoUrl(getApplicationContext()));
        cfUser.setPhoneNumber(AccountUtils.getPhone(getApplicationContext()));
        cfUser.setGcmRegId(AccountUtils.getGcmId(getApplicationContext()));
        return mLoginAndAuthHelper.updateCFUser(this, cfUser);
    }

    protected void updateUi() {
    }

    private void loadResources() {
        String photoUrl = AccountUtils.getPhotoUrl(getApplicationContext());
        if (photoUrl != null && !photoUrl.equals("")) {
            Picasso.with(getApplicationContext())
                    .load(photoUrl)
                    .fit()
                    .placeholder(R.drawable.person_image_empty)
                    .into(ivPhoto);
        }

        etDisplayName.setText(AccountUtils.getDisplayName(getApplicationContext()));
        etEmail.setText(AccountUtils.getEmail(getApplicationContext()));
        etPhone.setText(AccountUtils.getPhone(getApplicationContext()));
        etAddtess.setText(AccountUtils.getAddress(getApplicationContext()));
        tvRegId.setText(AccountUtils.getGcmId(getApplicationContext()));
        tvLocalId.setText(AccountUtils.getLocalId(getApplicationContext()));

        /*
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
        }*/
    }

    private boolean updateResources() {
        AccountUtils.setPhone(getApplicationContext(), etPhone.getText().toString());
        AccountUtils.setAddress(getApplicationContext(), etAddtess.getText().toString());
        //TODO Upload Google Data Store
        return true;
    }

    //有這一行Drawer才會顯示
    protected int getSelfNavDrawerItem() {
        return NAVDRAWER_ITEM_ACCOUNT;
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
    protected void onPause() {
        super.onPause();
        updateResources();
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        //Check by Id 暫時沒用到
        switch (v.getId()) {
            case R.id.btSave:
                if (updateResources() && updateCFUser()) {
                    Toast.makeText(this, getResources().getString(R.string.successfulUpdate), Toast.LENGTH_SHORT).show();
                    goToNavDrawerItem(0);   //跳至首個Activity
                }
                break;
            default:
        }
        //Check by Tag
        /*
        switch (v.getTag().toString()) {
            case "btAdd":
                startResourceDetail(this,null);
                break;
        }*/
    }

}
