package tw.binary.dipper.examples;

import android.app.IntentService;
import android.content.Intent;

/**
 * Created by eason on 2015/5/5.
 */
public class RSSPullService extends IntentService {

    //IntentService執行完會自動結束，不用再手動結束

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public RSSPullService(String name) {

        super(name);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    //處理作業放這
    @Override
    protected void onHandleIntent(Intent intent) {

    }
}


//Manifests 設定
/*
<application/>
    <service android:name=".RSSPullService"
             android:exported="false"/>

<application/> */
