package tw.binary.dipper.examples;

import android.app.IntentService;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.util.Log;

import org.apache.http.client.HttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import tw.binary.dipper.util.Constants;

public class DataService extends IntentService {
    private SQLiteDatabase db;
    private ConnectivityManager connMgr;
    private HttpClient client;

    public DataService() {
        super("DataService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // TODO initialize db, connMgr, client
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String[] pair = intent.getStringArrayExtra(Constants.EXTRA_PAIR);
        String result = null;
        BufferedReader br = null;

        // TODO check for network connectivity
        // TODO construct url from currency pair
        // TODO make REST call and get result

        if (result != null) {
            db.beginTransaction();
            try {
                br = new BufferedReader(new StringReader(result));
                update(db, br);
                db.setTransactionSuccessful();
                broadcastStatus(Constants.STATUS_SUCCESS);

            } catch (Exception e) {
                Log.e("TAG", e.getMessage(), e);
            } finally {
                db.endTransaction();
                try {
                    if (br != null) br.close();
                } catch (IOException e) {
                }
            }
        }
        broadcastStatus(Constants.STATUS_FAILED);
    }

    /**
     * "USDEUR=X",0.7802,0.7799,0.78,"3/29/2013","6:55pm"
     */
    private void update(SQLiteDatabase db, BufferedReader br) throws IOException {
        String line;
        String[] tokens;
        while ((line = br.readLine()) != null) {
            tokens = line.split(",");

            // TODO set tokens to Quote and do update(db)
        }
    }

    private void broadcastStatus(int status) {
        Intent intent = new Intent(Constants.ACTION_UPDATE);
        intent.putExtra(Constants.EXTRA_STATUS, status);
        sendBroadcast(intent);
    }
}
