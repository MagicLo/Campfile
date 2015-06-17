package tw.binary.dipper;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Xml;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import tw.binary.dipper.model.Quote;
import tw.binary.dipper.util.Constants;


//取得Yahoo API
public class ExchangeRateTask extends AsyncTask<Void, Integer, Integer> {

    private Context context;
    private SQLiteDatabase db;

    //Constructor，放在onPreExecute應該也可以，試試看
    public ExchangeRateTask(Context context) {
        super();
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Integer doInBackground(Void... params) {
        String URL = "http://finance.yahoo.com/webservice/v1/symbols/allcurrencies/quote";

        String result = null;
        ConnectivityManager connMgr = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connMgr.getActiveNetworkInfo();
        HttpClient client = new DefaultHttpClient();

        boolean isConnected = netInfo != null
                && netInfo.getState() == NetworkInfo.State.CONNECTED
                //&& netInfo.getType() == ConnectivityManager.TYPE_WIFI
                ;

        if (isConnected) {
            try {
                //Log.i("TAG", "Connecting");
                HttpGet request = new HttpGet(new URI(URL));
                HttpResponse response = client.execute(request);
                BasicResponseHandler handler = new BasicResponseHandler();
                result = handler.handleResponse(response);
                if (result != "") {
                    InputStream mInputStream = new ByteArrayInputStream(result.getBytes());
                    xmlParsing(mInputStream);   //解析XML
                    //Log.i("TAG", "Connection Success");
                } else {
                    //Log.i("TAG", "Connection Fail");
                }
                return Constants.STATUS_SUCCESS;

            } catch (Exception e) {
                //Log.e("TAG", e.getMessage(), e);
                return Constants.STATUS_FAILED;
            }
        }
        return Constants.STATUS_SUCCESS;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {

        super.onProgressUpdate(values); //可顯示載入進度
    }

    protected void xmlParsing(InputStream result) {
        Quote mQuote = new Quote();
        String elementName = "", elementValue = "", mAttributeValue = "", mFieldTag = "";
        XmlPullParser parser = Xml.newPullParser();
        Quote.deleteAll(Quote.class);   //刪除所有Quote資料

        try {
            parser.setInput(result, "utf-8");
            int eventType = parser.getEventType();  //利用eventType來判斷目前分析到XML是哪一個部份

            while (eventType != XmlPullParser.END_DOCUMENT) {
                elementName = parser.getName();
                elementValue = parser.getText();

                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        mFieldTag = elementName;    //取得XML標籤
                        if (mFieldTag.equalsIgnoreCase("resource"))
                            mQuote = new Quote();
                        //Log.i("TAG", "START_TAG:" + mFieldTag + "-" + elementValue + "-" + mAttributeValue);

                        if (elementName.equalsIgnoreCase("field")) {
                            mAttributeValue = parser.getAttributeValue(null, "name");  //0表示第一個attribute
                        } else {
                            mAttributeValue = "";
                        }
                        break;
                    case XmlPullParser.TEXT:
                        //Log.i("TAG", "TEXT_TAG:" + elementName + "-" + elementValue + "-" + mAttributeValue + "&&&&&&&&&&&&" + mFieldTag);
                        if (mFieldTag.equalsIgnoreCase("field")) {
                            if (mAttributeValue.equalsIgnoreCase("name")) {
                                mQuote.setName(elementValue);
                            } else if (mAttributeValue.equalsIgnoreCase("price")) {
                                mQuote.setPrice(elementValue);
                            } else if (mAttributeValue.equalsIgnoreCase("symbol")) {
                                if (mQuote.getName().contains("USD")) {
                                    mQuote.setSymbol(elementValue.substring(0, 3));
                                } else {
                                    mQuote.setSymbol("");
                                }
                            } else if (mAttributeValue.equalsIgnoreCase("type")) {
                                mQuote.setType(elementValue);
                            } else if (mAttributeValue.equalsIgnoreCase("utctime")) {
                                mQuote.setUtctime(elementValue);
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        mFieldTag = "";
                        //Log.i("TAG", "END_TAG:" + elementName + "-" + elementValue + "-" + mAttributeValue + "-" + mFieldTag);
                        if (elementName != null && elementName.equalsIgnoreCase("resource")) {
                            //Log.i("TAG", mResource.toString());
                            mQuote.save();
                        }
                }
                eventType = parser.next();
            }
        } catch (IOException e) {
            Log.i("TAG", "I/O Error");
        } catch (XmlPullParserException e) {
            Log.i("TAG", "XML Parsing error!");
        }
    }

    //result 是doInBackground 的結果傳入
    protected void onPostExecute(Integer result) {
        Intent intent = new Intent(Constants.ACTION_EXCHANGERATE);
        intent.putExtra(Constants.EXTRA_STATUS, result);
        context.sendBroadcast(intent);    //傳送給所有AP
    }
}
