package tw.binary.dipper.examples;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

/**
 * Created by eason on 2015/5/5.
 * Service 不會自行停止，一定要下指令，但intent service會自行停止
 */

public class MyService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    //創建
    @Override
    public void onCreate() {
        super.onCreate();
        Toast.makeText(this, "Service is created", Toast.LENGTH_SHORT).show();
    }

    //執行
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "Service is started", Toast.LENGTH_SHORT).show();
        String message = intent.getStringExtra("status");

        stopSelf(); //可自行停止服務作業
        return super.onStartCommand(intent, flags, startId);
    }

    //結束
    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "Service is Destroy(stoped)", Toast.LENGTH_SHORT).show();
    }
}

/*
<application>
    <service android:name=".MyService"
        android:exported="false" /> //不給其他APP用
</application>
 */