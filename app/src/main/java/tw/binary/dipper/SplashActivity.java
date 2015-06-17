package tw.binary.dipper;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import tw.binary.dipper.gcm.GcmGetRegId;
import tw.binary.dipper.util.AccountUtils;
import tw.binary.dipper.util.Constants;

public class SplashActivity extends Activity {
    private Typeface font;

    private TextView tv1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        if (!isNetworkConnected())
            Toast.makeText(this, getResources().getString(R.string.NoNetworkConnection), Toast.LENGTH_LONG).show();

        if (AccountUtils.isWelcomeDone(this)) {
            showLoadingPage();
        } else {
            showEulaPage();
        }
        //沒有gcmregid就要一個
        if (AccountUtils.getGcmId(this) == null) {
            GcmGetRegId gcmGetRegId = new GcmGetRegId(this);
            gcmGetRegId.execute();
        }

        //setContentView(R.layout.activity_splash);
        //設定Splash頁視覺效果
        //tv1 = (TextView) findViewById(R.id.textView1);
        //font = Typeface.createFromAsset(getAssets(), "fonts/OpenSans.ttf");
        //tv1.setTypeface(font);
    }

    private void showLoadingPage() {
        //下載匯率
        ExchangeRateTask exchangeRate = new ExchangeRateTask(getApplicationContext());
        exchangeRate.execute();
        //WeatherForecastTask weatherForecastTask = new WeatherForecastTask(getApplicationContext());
        //weatherForecastTask.execute();
        setContentView(R.layout.activity_splash);
        registerReceiver(initStatusReceiver, new IntentFilter(Constants.ACTION_EXCHANGERATE));
    }

    private void showEulaPage() {
        setContentView(R.layout.activity_splash_eula);
        TextView tvEULA = (TextView) findViewById(R.id.tvEULA);

        String url = "<a href=" + Constants.GCS_PUBLIC_BUCKET_URL
                + "html/eula.html>"
                + getResources().getString(R.string.eula_title)
                + "</a>";
        tvEULA.setText(Html.fromHtml(url));
        tvEULA.setMovementMethod(LinkMovementMethod.getInstance());
        Button btAccept = (Button) findViewById(R.id.btAccept);
        btAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AccountUtils.setWelcomeDone(getApplicationContext());
                showLoadingPage();
            }
        });
        Button btCancel = (Button) findViewById(R.id.btCancel);
        btCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private boolean isNetworkConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    @Override
    protected void onPause() {
        //if(AccountUtils.isWelcomeDone(this))
        //    unregisterReceiver(initStatusReceiver);
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(initStatusReceiver);
    }

    private BroadcastReceiver initStatusReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && Constants.ACTION_EXCHANGERATE.equals(intent.getAction())) {
                switch (intent.getIntExtra(Constants.EXTRA_STATUS, 100)) {  //100->default value
                    case Constants.STATUS_SUCCESS:
                        //另一種寫法
                        /*
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                                startActivity(new Intent(SplashActivity.this, HomeActivityDrawer.class));
                                //Log.i("TAG","start SplashActivity");
                                finish();
                            }
                        });
                        */
                        startActivity(new Intent(SplashActivity.this, HomeActivityDrawer.class));
                        finish();
                        break;

                    case Constants.STATUS_FAILED:
                        Toast.makeText(context, getString(R.string.init_failed), Toast.LENGTH_SHORT).show();
                        finish();
                        break;
                }
            }
        }
    };
}
