package tw.binary.dipper;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import tw.binary.dipper.model.Weather;
import tw.binary.dipper.util.Constants;
import tw.binary.dipper.util.HttpUtils;
import tw.binary.dipper.util.MyUtils;

// Created by eason on 2015/4/30.

// https://query.yahooapis.com/v1/public/yql?q=select * from weather.forecast where woeid in (select woeid from geo.places(1) where text="hsinchu")&format=json&env=store://datatables.org/alltableswithkeys&callback=
// https://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20weather.forecast%20where%20woeid%20in%20(select%20woeid%20from%20geo.places(1)%20where%20text%3D%22hsinchu%22)&format=json&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys&callback=

//取得Yahoo Weather API
//**********此程式暫時沒用到，參考用
public class WeatherForecastTask extends AsyncTask<Void, Integer, Integer> {

    private Context context;
    private SQLiteDatabase db;

    //Constructor，放在onPreExecute應該也可以，試試看
    public WeatherForecastTask(Context context) {
        super();
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Integer doInBackground(Void... params) {
        String param1 = "https://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20weather.forecast%20where%20woeid%20in%20(select%20woeid%20from%20geo.places(1)%20where%20text%3D%22";
        String city = "hsinchu";
        String param2 = "%22)&format=json&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys&callback=";
        //String url = "https://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20weather.forecast%20where%20woeid%20in%20(select%20woeid%20from%20geo.places(1)%20where%20text%3D%22hsinchu%22)&format=json&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys&callback=";
        String url = param1 + city + param2;
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
                byte[] result = HttpUtils.get(url.toString()); //取的預報結果JSON
                if (result.length > 0) {
                    InputStream mInputStream = new ByteArrayInputStream(result);
                    jsonParsing(mInputStream);   //解析XML
                    //Log.i("TAG", "Connection Success");
                } else {
                    //Log.i("TAG", "Connection Fail");
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
        Intent intent = new Intent(Constants.ACTION_WEATHER);
        intent.putExtra(Constants.EXTRA_STATUS, result);
        context.sendBroadcast(intent);
    }

    protected void jsonParsing(InputStream result) {
        SimpleDateFormat enFormat = new SimpleDateFormat("d MMM yyyy", Locale.ENGLISH);
        SimpleDateFormat twFormat = new SimpleDateFormat("yyyy MMM d", Locale.ENGLISH);
        JSONObject jsonObject;
        JSONArray jsonArray;

        Weather.deleteAll(Weather.class);   //刪除所有Quote資料
        try {
            jsonObject = new JSONObject(MyUtils.stream2string(result));
            jsonObject = jsonObject.getJSONObject("query");
            jsonObject = jsonObject.getJSONObject("results");
            jsonObject = jsonObject.getJSONObject("channel");
            jsonObject = jsonObject.getJSONObject("item");
            jsonArray = jsonObject.getJSONArray("forecast");
            for (int day = 0; day < jsonArray.length(); day++) {
                jsonObject = jsonArray.getJSONObject(day);
                //找出日期資料是否存在，避免重複
                String forcastDate = twFormat.format(enFormat.parse(jsonObject.getString("date")));
                String forecastCountry = "Taiwan";
                String forecastCity = "Taipei";
                //Insert record
                Weather weather = new Weather();
                weather.Country = forecastCountry;
                weather.City = forecastCity;
                weather.Code = jsonObject.getString("code");
                weather.Date = forcastDate;
                weather.Day = jsonObject.getString("day");
                weather.High = jsonObject.getString("high");
                weather.Low = jsonObject.getString("low");
                weather.Text = jsonObject.getString("text");
                weather.save();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
