package tw.binary.dipper.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import tw.binary.dipper.R;

/**
 * Created by eason on 2015/5/1.
 */
public class WeatherUtils {
    Context mContext;
    private OnWeatherForcastListener mListener;

    //**************定義Interfece
    public interface OnWeatherForcastListener {
        void forcastResult(JSONArray result);
    }

    //**************宣告給implement用的
    public void setOnWeatherForcastListener(OnWeatherForcastListener listener) {
        mListener = listener;
    }

    public WeatherUtils(Context pContext) {
        mContext = pContext;
    }

    //取得Yahoo WoeID 碼
    public String getWoeId(double lat, double lng) {
        //String url = "http://query.yahooapis.com/v1/public/yql?q=select%20woeid%20from%20geo.placefinder%20where%20text=%2224.9145512,120.9704122%22%20and%20gflags=%22R%22&format=json";
        String param1 = "http://query.yahooapis.com/v1/public/yql?q=select%20woeid%20from%20geo.placefinder%20where%20text=%22";
        String param2 = "%22%20and%20gflags=%22R%22&format=json";
        String url = param1 + String.valueOf(lat) + "," + String.valueOf(lng) + param2;
        //取得網路狀態
        ConnectivityManager connMgr = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connMgr.getActiveNetworkInfo();
        boolean isConnected = netInfo != null
                && netInfo.getState() == NetworkInfo.State.CONNECTED
                //&& netInfo.getType() == ConnectivityManager.TYPE_WIFI
                ;

        if (isConnected) {
            try {
                Log.i("TAG", "getWoeId Connecting");
                byte[] result = HttpUtils.get(url); //取的預報結果JSON
                if (result.length > 0) {
                    InputStream mInputStream = new ByteArrayInputStream(result);
                    Log.i("TAG", "getWoeId Connection Success");
                    return yahooApiParsing(mInputStream);   //解析XML
                } else Log.i("TAG", "getWoeId Connection Fail");

            } catch (Exception e) {
                Log.e("TAG", e.getMessage(), e);
                return null;
            }
        }
        return null;
    }

    private String yahooApiParsing(InputStream result) {
        JSONObject jsonObject;

        try {
            jsonObject = new JSONObject(MyUtils.stream2string(result));
            jsonObject = jsonObject.getJSONObject("query");
            jsonObject = jsonObject.getJSONObject("results");
            jsonObject = jsonObject.getJSONObject("Result");
            return jsonObject.getString("woeid");

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    //Parsing Open Weather Map Json result

    //取得Yahoo五日預報
    private JSONArray getForcast(String woeId) {

        //String url = "https://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20weather.forecast%20where%20woeid%20%3D%2228752535%22&format=json&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys&callback=";
        String param1 = "https://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20weather.forecast%20where%20woeid%20%3D%22";
        String param2 = "%22&format=json&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys&callback=";
        String url = param1 + woeId + param2;
        //取得網路狀態
        ConnectivityManager connMgr = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connMgr.getActiveNetworkInfo();
        boolean isConnected = netInfo != null
                && netInfo.getState() == NetworkInfo.State.CONNECTED
                //&& netInfo.getType() == ConnectivityManager.TYPE_WIFI
                ;

        if (isConnected) {
            try {
                Log.i("TAG", "Connecting");
                byte[] result = HttpUtils.get(url); //取的預報結果JSON
                if (result.length > 0) {
                    InputStream mInputStream = new ByteArrayInputStream(result);
                    Log.i("TAG", "getForcast Connection Success");
                    return forcastParsing(mInputStream);   //解析XML
                } else Log.i("TAG", "getForcast Connection Fail");

            } catch (Exception e) {
                Log.e("TAG", e.getMessage(), e);
                return null;
            }
        }
        return null;
    }

    private JSONArray forcastParsing(InputStream result) {
        //SimpleDateFormat enFormat = new SimpleDateFormat("d MMM yyyy", Locale.ENGLISH);
        //SimpleDateFormat twFormat = new SimpleDateFormat("yyyy MMM d", Locale.ENGLISH);
        JSONObject jsonObject;

        try {
            jsonObject = new JSONObject(MyUtils.stream2string(result));
            jsonObject = jsonObject.getJSONObject("query");
            jsonObject = jsonObject.getJSONObject("results");
            jsonObject = jsonObject.getJSONObject("channel");
            jsonObject = jsonObject.getJSONObject("item");
            return jsonObject.getJSONArray("forecast");
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    //給外界呼叫用的工具
    public void getForcast(double lat, double lng) {
        LatLng latLng = new LatLng(lat, lng);
        GetYahooForcastAsyncTask getYahooForcastAsyncTask = new GetYahooForcastAsyncTask(mContext);
        getYahooForcastAsyncTask.execute(latLng);
    }

    //Yahoo Weather API
    class GetYahooForcastAsyncTask extends AsyncTask<LatLng, Void, JSONArray> {
        private Context context;
        private ProgressDialog mProgressDialog;

        protected GetYahooForcastAsyncTask(Context context) {
            this.context = context;
            mProgressDialog = ProgressDialog.show(context
                    , ""
                    , context.getResources().getString(R.string.system_processing));
        }

        //Download from GAE & Local
        //params:UserID
        //Return ArrayList<CFUser>
        @Override
        protected JSONArray doInBackground(LatLng... params) {
            LatLng latLng = params[0];

            return getForcast(getWoeId(latLng.latitude, latLng.longitude));
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        protected void onPostExecute(JSONArray result) {
            if (mProgressDialog != null && mProgressDialog.isShowing()) mProgressDialog.dismiss();
            if (result == null) {
                Toast.makeText(context, "無資料", Toast.LENGTH_LONG).show();
                mListener.forcastResult(null);
            }
            mListener.forcastResult(result);
        }
    }

}
