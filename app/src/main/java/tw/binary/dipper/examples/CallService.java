package tw.binary.dipper.examples;

import android.app.Activity;
import android.content.Intent;

/**
 * Created by eason on 2015/5/5.
 */
public class CallService extends Activity {


    public void startMethod() {
        Intent intent = new Intent(this, MyService.class);
        intent.putExtra("status", "abcef");
        startService(intent);
    }

    public void stopMethod() {
        Intent intent = new Intent(this, MyService.class);
        stopService(intent);
    }
}
