package tw.binary.dipper.gcm;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;

import tw.binary.dipper.R;
import tw.binary.dipper.util.AccountUtils;

/**
 * Created by eason on 2015/5/5.
 */
public class GcmGetRegId extends AsyncTask<Void, Void, String> {
    private Context context;
    private GoogleCloudMessaging gcm;
    private String regid;

    public GcmGetRegId(Context pContext) {
        context = pContext;
    }

    @Override
    protected String doInBackground(Void... params) {
        String msg = "";
        try {
            if (gcm == null) {
                gcm = GoogleCloudMessaging.getInstance(context);
            }
            regid = gcm.register(context.getResources().getString(R.string.gae_project_number));
            Log.i("GCM", "Device registered, registration ID=" + regid);
            return regid;

        } catch (IOException ex) {
            Log.i("GCM", "Error :" + ex.getMessage());
            return null;
        }
    }

    @Override
    protected void onPostExecute(String msg) {
        if (msg != null) {
            AccountUtils.setGcmId(context, msg);    //存進Preference
        }
    }

}