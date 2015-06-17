package tw.binary.dipper;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.IOException;
import java.util.HashMap;

import tw.binary.dipper.message.MessageDbHelper;
import tw.binary.dipper.util.AccountUtils;
import tw.binary.dipper.util.HttpUtils;
import tw.binary.dipper.util.MyUtilsDate;

// Created by eason on 2015/1/27.
public class HomeActivityDrawer extends DrawerBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        HashMap<String, String> message = new HashMap<>();
        message.put(MessageDbHelper.MESSAGE_COL_MSG, "Message form Home");
        message.put(MessageDbHelper.MESSAGE_COL_FROM, AccountUtils.getLocalId(this));
        message.put(MessageDbHelper.MESSAGE_COL_TO, AccountUtils.getLocalId(this));
        message.put(MessageDbHelper.MESSAGE_COL_SENT, MyUtilsDate.CurrentDateTime());
        try {
            HttpUtils.postMessage(this, message);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //自行傳送
        /*GcmMsg msg = new GcmMsg();
        //訊息接收者
        msg.addRegId(AccountUtils.getGcmId(this));
        //訊息內容
        msg.createData("Message body",              //MESSAGE_COL_MSG = "msg";
                AccountUtils.getLocalId(this),      //MESSAGE_COL_FROM = "callerlocalid";
                AccountUtils.getGcmId(this),
                AccountUtils.getLocalId(this),      //MESSAGE_COL_TO = "receiverlocalid";
                AccountUtils.getGcmId(this),
                AccountUtils.getDisplayName(this),  //MESSAGE_COL_DISPLAYNAME = "displayname";
                AccountUtils.getPhotoUrl(this),     //MESSAGE_COL_PHOTOURL = "photourl";
                MyUtilsDate.CurrentDateTime());     //MESSAGE_COL_SENT = "senttime";

        GcmMessageSendTask gcmMessageSendTask = new GcmMessageSendTask(this);
        gcmMessageSendTask.execute(msg.toJsonString());
        //gcmMessageSendTask.execute(new Pair<>(param1, param2)); //Pair<>參數傳遞範例

*/
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_home;
    }

    protected int getSelfNavDrawerItem() {
        return NAVDRAWER_ITEM_HOME;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_example:
                Toast.makeText(this, "Action Example", Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_settings:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean isAuthNeeded() {
        return false;
    }

    @Override
    protected void updateUi() {

    }
}
