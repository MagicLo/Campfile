package tw.binary.dipper.gcm;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

/**
 * Created by eason on 2015/5/4.
 */
public class GcmBroadcastReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        // Explicitly specify that GcmMessageHandler will handle the intent.
        ComponentName comp = new ComponentName(context.getPackageName(), GcmBroadcastHandler.class.getName());

        // Start the service, keeping the device awake while it is launching.
        //在intent中再加入intent service的參數
        startWakefulService(context, (intent.setComponent(comp)));

        setResultCode(Activity.RESULT_OK);
    }


}