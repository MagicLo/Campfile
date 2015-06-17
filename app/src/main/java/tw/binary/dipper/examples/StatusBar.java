package tw.binary.dipper.examples;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import tw.binary.dipper.R;

/**
 * Created by eason on 2015/5/5.
 */
public class StatusBar extends Activity {
    private static final int uniqueID = 12345;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        NotificationManager mNotificationManager;
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotificationManager.cancel(uniqueID);  //由status bar呼叫此Activity後，取消status bar上的訊息通知

        Intent intent = new Intent(this, StatusBar.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        String msgBody = "This is a message from";
        String msgTitle = "Title";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        Notification notification = builder.setContentIntent(pendingIntent) //要打開的intent
                .setSmallIcon(R.drawable.sunny_day).setTicker("You got message").setWhen(System.currentTimeMillis())
                .setAutoCancel(true).setContentTitle(msgTitle)
                .setContentText(msgBody).build();           //圖 訊息說明 發送時間 標題 內容

        notification.defaults = Notification.DEFAULT_ALL;   //提供振動和響鈴效果

        mNotificationManager.notify(uniqueID, notification);//傳送通知
    }

}
