package tw.binary.dipper.gcm;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import tw.binary.dipper.R;
import tw.binary.dipper.util.Constants;
import tw.binary.dipper.util.HttpUtils;

/**
 * Created by eason on 2015/5/6.
 */
public class GcmMessageSendTask extends AsyncTask<String, Integer, Integer> {

    private Context context;
    private SQLiteDatabase db;

    //Constructor，放在onPreExecute應該也可以，試試看
    public GcmMessageSendTask(Context context) {
        super();
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Integer doInBackground(String... params) {
        String gcmApiKey = context.getResources().getString(R.string.gae_server_api_key);
        String gcmMessage = params[0];

        //取得網路狀態
        ConnectivityManager connMgr = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connMgr.getActiveNetworkInfo();
        boolean isConnected = netInfo != null
                && netInfo.getState() == NetworkInfo.State.CONNECTED
                //&& netInfo.getType() == ConnectivityManager.TYPE_WIFI
                ;

        if (isConnected) {
            try {
                //Log.i("TAG", "Connecting");
                byte[] result = HttpUtils.post2gcm(gcmApiKey, gcmMessage.getBytes());
                if (result.length > 0) {
                    InputStream mInputStream = new ByteArrayInputStream(result);
                    Log.i("TAG", "GCM SENT Success");
                } else {
                    Log.i("TAG", "GCM SENT Fail");
                }
                return Constants.STATUS_SUCCESS;

            } catch (Exception e) {
                Log.e("TAG", e.getMessage(), e);
                return Constants.STATUS_FAILED;
            }
        }
        return Constants.STATUS_FAILED;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {

        super.onProgressUpdate(values); //可顯示載入進度
    }

    //result 是doInBackground 的結果傳入
    protected void onPostExecute(Integer result) {
        /*if(result.equals(Constants.STATUS_SUCCESS))
            Toast.makeText(context,"Message Sent", Toast.LENGTH_LONG).show();
        else
            Toast.makeText(context,"Message not Sent", Toast.LENGTH_LONG).show();*/
    }

}
