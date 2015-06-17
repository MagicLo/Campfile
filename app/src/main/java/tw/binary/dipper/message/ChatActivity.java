package tw.binary.dipper.message;

import android.content.ContentValues;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.util.HashMap;

import tw.binary.dipper.R;
import tw.binary.dipper.ToolbarBaseActivity;
import tw.binary.dipper.util.AccountUtils;
import tw.binary.dipper.util.HttpUtils;
import tw.binary.dipper.util.MyUtilsDate;

/**
 * Created by eason on 2015/5/13.
 */
public class ChatActivity extends ToolbarBaseActivity implements
        MessageFragment.OnFragmentInteractionListener, View.OnClickListener {
    private String fromLocalId;
    private String fromDisplayName;
    private EditText msgEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //寄件人資訊
        fromLocalId = getIntent().getStringExtra(MessageDbHelper.MESSAGE_COL_FROM);
        fromDisplayName = getIntent().getStringExtra(MessageDbHelper.MESSAGE_COL_DISPLAYNAME);

        findViewId();
        customActionBar();
    }

    private void findViewId() {
        Button btSend = (Button) findViewById(R.id.send_btn);
        msgEdit = (EditText) findViewById(R.id.msg_edit);
        btSend.setOnClickListener(this);
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_chat;
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.send_btn:
                //傳送訊息給Server
                if (!TextUtils.isEmpty(msgEdit.getText())) {
                    send(msgEdit.getText().toString());
                    msgEdit.setText(null);
                }
                break;
        }
    }

    private void send(final String txt) {

        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";

                ContentValues values = new ContentValues(8);
                //我的資訊
                values.put(MessageDbHelper.MESSAGE_COL_MSG, txt);
                values.put(MessageDbHelper.MESSAGE_COL_FROM, AccountUtils.getLocalId(getApplicationContext()));
                values.put(MessageDbHelper.MESSAGE_COL_TO, fromLocalId);
                //values.put(MessageDbHelper.MESSAGE_COL_PHOTOURL, AccountUtils.getPhotoUrl(mContext));//Local要存
                //values.put(MessageDbHelper.MESSAGE_COL_DISPLAYNAME, AccountUtils.getDisplayName(mContext));//Local要存
                values.put(MessageDbHelper.MESSAGE_COL_SENT, MyUtilsDate.CurrentDateTime());//Local要存
                //Local DB也存一份，才能同步顯示
                getContentResolver().insert(MessageContentProvider.CONTENT_URI_MESSAGES, values);
                //送給主機
                try {
                    HttpUtils.postMessage2(getApplicationContext(), values);
                } catch (IOException e) {
                    e.printStackTrace();
                    msg = "Message could not be sent";
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                if (!TextUtils.isEmpty(msg)) {
                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                }
            }
        }.execute(null, null, null);
    }

    protected void customActionBar() {
        ActionBar actionBar = getSupportActionBar();
/*
        //在ActionBar上顯示圖示
        String fromPhotoUrl = getIntent().getStringExtra(MessageDbHelper.MESSAGE_COL_PHOTOURL);
        actionBar.setDisplayOptions(actionBar.getDisplayOptions() | ActionBar.DISPLAY_SHOW_CUSTOM);
        ImageView imageView = new ImageView(actionBar.getThemedContext());
        imageView.setScaleType(ImageView.ScaleType.CENTER);

        Transformation transformation = new RoundedTransformationBuilder()
                .borderWidthDp(0)
                .cornerRadiusDp(50)
                .oval(false)
                .build();
        if (fromPhotoUrl != null && !fromPhotoUrl.equals("")) {
            Picasso.with(getApplicationContext())
                    .load(fromPhotoUrl)
                    .fit().centerCrop()
                    .transform(transformation)
                    .placeholder(R.drawable.person_image_empty_48)
                    .into((ImageView) imageView);
        }

        ActionBar.LayoutParams layoutParams = new ActionBar.LayoutParams(
                ActionBar.LayoutParams.WRAP_CONTENT,
                ActionBar.LayoutParams.WRAP_CONTENT,
                Gravity.RIGHT | Gravity.CENTER_VERTICAL);

        //layoutParams.rightMargin = 40;
        imageView.setLayoutParams(layoutParams);
        actionBar.setCustomView(imageView);
*/

        //設定Actionbar顯示寄件人名稱
        if (actionBar != null) {
            actionBar.setTitle(getIntent().getStringExtra(MessageDbHelper.MESSAGE_COL_DISPLAYNAME));
            actionBar.setDisplayHomeAsUpEnabled(false);
            //actionBar.setSubtitle("");
        }
    }

    //交換給Fragment用的，以便顯示在Fragment中
    @Override
    public HashMap<String, String> getFromLocalInfo() {
        HashMap<String, String> callerInfo = new HashMap<>();

        callerInfo.put(MessageDbHelper.MESSAGE_COL_FROM, fromLocalId);
        callerInfo.put(MessageDbHelper.MESSAGE_COL_DISPLAYNAME, fromDisplayName);

        return callerInfo;
    }
}
