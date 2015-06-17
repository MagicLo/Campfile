package tw.binary.dipper.gcm;

import android.app.ActivityManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.util.List;

import tw.binary.dipper.R;
import tw.binary.dipper.message.CallerListActivity;
import tw.binary.dipper.message.MessageContentProvider;
import tw.binary.dipper.message.MessageDbHelper;

/**
 * Created by eason on 2015/5/4.
 */
public class GcmBroadcastHandler extends IntentService {

    private String msg, callerLocalId, callerGcmId, receiverLocalId, receiverGcmId;
    private String displayName, photoUrl, sentTime;
    private Handler handler;
    private Context context;

    public GcmBroadcastHandler() {
        super("GcmMessageHandler");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler();
        context = this;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();

        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);
        if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
            sendNotification("Send error", false);

        } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
            sendNotification("Deleted messages on server", false);

        } else {
            msg = extras.getString(MessageDbHelper.MESSAGE_COL_MSG);
            callerLocalId = extras.getString(MessageDbHelper.MESSAGE_COL_FROM);
            receiverLocalId = extras.getString(MessageDbHelper.MESSAGE_COL_TO);
            displayName = extras.getString(MessageDbHelper.MESSAGE_COL_DISPLAYNAME);
            photoUrl = extras.getString(MessageDbHelper.MESSAGE_COL_PHOTOURL);
            sentTime = extras.getString(MessageDbHelper.MESSAGE_COL_SENT);

            //Message部份
            ContentValues messageValues = new ContentValues(6);
            messageValues.put(MessageDbHelper.MESSAGE_COL_MSG, msg);
            messageValues.put(MessageDbHelper.MESSAGE_COL_FROM, callerLocalId);
            messageValues.put(MessageDbHelper.MESSAGE_COL_TO, receiverLocalId);
            messageValues.put(MessageDbHelper.MESSAGE_COL_DISPLAYNAME, displayName);
            messageValues.put(MessageDbHelper.MESSAGE_COL_PHOTOURL, photoUrl);
            messageValues.put(MessageDbHelper.MESSAGE_COL_SENT, sentTime);
            context.getContentResolver().insert(MessageContentProvider.CONTENT_URI_MESSAGES, messageValues);

            if (true) {
                sendNotification("New message", true);

                //showToast(messageType);
                Log.i("GCM", "Received : (" + messageType + ")  " + extras.getString("title"));
            }
        }
        // Test Begin 20150519
        ActivityManager am = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);//Lollipo要用getAppTasks()
        ComponentName componentInfo = taskInfo.get(0).topActivity;
        Log.i("TAG", "CURRENT Activity ::" + taskInfo.get(0).topActivity.getClassName() + "   Package Name :  " + componentInfo.getPackageName());
        //Test End 20150519
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    public void showToast(final String messageType) {
        handler.post(new Runnable() {
            public void run() {
                if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                    Toast.makeText(getApplicationContext(), "Send error", Toast.LENGTH_LONG).show();
                } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
                    Toast.makeText(getApplicationContext(), "Deleted messages on server", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                    sendNotification(getString(R.string.youGotMail), true);
                    //TODO 將訊息存入local ＆ server db（留底用）中
                }
            }
        });
    }

    private void sendNotification(String text, boolean launchApp) {
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Notification.Builder mBuilder = new Notification.Builder(this)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(text);

        /*if (!TextUtils.isEmpty(Common.getRingtone())) {
            mBuilder.setSound(Uri.parse(Common.getRingtone()));
        }
        */
        if (launchApp) {
            Intent intent = new Intent(context, CallerListActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(pi);
        }

        mNotificationManager.notify(Integer.valueOf(getString(R.string.notification_id)), mBuilder.getNotification());
    }
}
