package tw.binary.dipper.gcm;/* Created by eason on 2015/5/17. */

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;

import tw.binary.dipper.util.HttpUtils;

//Message Post API
public class GcmMessagePostTask extends AsyncTask<byte[], Void, byte[]> {
    private Context mContext;
    private ProgressDialog mProgressDialog;
    private String endpoint;

    public GcmMessagePostTask(Context context) {
        this.mContext = context;
        this.endpoint = "https://graceful-design-89523.appspot.com/m";
        //this.endpoint = mContext.getResources().getString(R.string.server_endpoint);
        //this.endpoint = "http://10.0.2.2:8080/m"; //從Emulator測試
        //this.endpoint = "http://192.168.1.200:8080/m";    //從手機測試
        //mProgressDialog = ProgressDialog.show(mContext, "", mContext.getResources().getString(R.string.system_processing));
    }

    //Download from GAE & Local
    //params:UserID
    //Return ArrayList<CFUser>
    @Override
    public byte[] doInBackground(byte[]... params) {
        byte[] bytes = params[0];

        try {
            return HttpUtils.post(endpoint, bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }

    public void onPostExecute(byte[] result) {
        if (mProgressDialog != null && mProgressDialog.isShowing()) mProgressDialog.dismiss();
        if (result == null) {
            Log.i("TAG", "GCM POST Fail");
        } else {
            Log.i("TAG", "GCM POST Success : " + result);
        }

    }
}
