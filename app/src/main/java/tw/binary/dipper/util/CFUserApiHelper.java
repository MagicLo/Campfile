package tw.binary.dipper.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;

import java.io.IOException;

import tw.binary.dipper.R;
import tw.binary.dipper.api.cFUserApi.CFUserApi;
import tw.binary.dipper.api.cFUserApi.model.CFUser;

/**
 * Created by eason on 2015/4/30.
 */
public class CFUserApiHelper {
    // Param 1: 輸入條件   Param2: Progress    Param3: 結果
    static class InsertCFuserAsyncTask extends AsyncTask<CFUser, Void, Boolean> {
        private CFUserApi myApiService;
        private Context context;
        private ProgressDialog mProgressDialog;

        protected InsertCFuserAsyncTask(Context context) {
            this.context = context;
            mProgressDialog = ProgressDialog.show(context
                    , ""
                    , context.getResources().getString(R.string.system_processing));
        }

        //Download from GAE & Local
        //params:UserID
        //Return ArrayList<MyResource>
        @Override
        protected Boolean doInBackground(CFUser... params) {
            CFUser cfUser = params[0];

            ///////////////////// get from GAE /////////////////////////
            if (myApiService == null) {  // Only do this once
                CFUserApi.Builder builder = new CFUserApi.Builder(AndroidHttp.newCompatibleTransport(),
                        new AndroidJsonFactory(), null)
                        // options for running against local devappserver
                        // - 10.0.2.2:8080 is localhost's IP address in Android emulator
                        // - turn off compression when running against local devappserver
                        .setRootUrl("https://graceful-design-89523.appspot.com/_ah/api/")
                                //.setRootUrl("http://10.0.2.2:8080/_ah/api/")
                        .setGoogleClientRequestInitializer(new GoogleClientRequestInitializer() {
                            @Override
                            public void initialize(AbstractGoogleClientRequest<?> abstractGoogleClientRequest) throws IOException {
                                abstractGoogleClientRequest.setDisableGZipContent(true);
                            }
                        });
                myApiService = builder.build();
            }

            try {
                CFUserApi.Insert api = myApiService.insert(cfUser);
                cfUser = api.execute();
            } catch (IOException e) {
                e.getMessage();
                return false;    //失敗
            }
            return cfUser != null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        protected void onPostExecute(Boolean result) {
            if (mProgressDialog != null && mProgressDialog.isShowing()) mProgressDialog.dismiss();
            if (!result)
                Toast.makeText(context, "無資料", Toast.LENGTH_LONG).show();
        }
    }

    static class UpdateCFuserAsyncTask extends AsyncTask<CFUser, Void, Boolean> {
        private CFUserApi myApiService;
        private Context context;
        private ProgressDialog mProgressDialog;

        protected UpdateCFuserAsyncTask(Context context) {
            this.context = context;
            mProgressDialog = ProgressDialog.show(context
                    , ""
                    , context.getResources().getString(R.string.system_processing));
        }

        //Download from GAE & Local
        //params:UserID
        //Return ArrayList<MyResource>
        @Override
        protected Boolean doInBackground(CFUser... params) {
            CFUser cfUser = params[0];

            ///////////////////// get from GAE /////////////////////////
            if (myApiService == null) {  // Only do this once
                CFUserApi.Builder builder = new CFUserApi.Builder(AndroidHttp.newCompatibleTransport(),
                        new AndroidJsonFactory(), null)
                        // options for running against local devappserver
                        // - 10.0.2.2:8080 is localhost's IP address in Android emulator
                        // - turn off compression when running against local devappserver
                        .setRootUrl("https://graceful-design-89523.appspot.com/_ah/api/")
                                //.setRootUrl("http://10.0.2.2:8080/_ah/api/")
                        .setGoogleClientRequestInitializer(new GoogleClientRequestInitializer() {
                            @Override
                            public void initialize(AbstractGoogleClientRequest<?> abstractGoogleClientRequest) throws IOException {
                                abstractGoogleClientRequest.setDisableGZipContent(true);
                            }
                        });
                myApiService = builder.build();
            }

            try {
                CFUserApi.Update api = myApiService.update(cfUser.getId(), cfUser);
                cfUser = api.execute();
            } catch (IOException e) {
                e.getMessage();
                return false;    //失敗
            }
            return cfUser != null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        protected void onPostExecute(Boolean result) {
            if (mProgressDialog != null && mProgressDialog.isShowing()) mProgressDialog.dismiss();
            if (!result)
                Toast.makeText(context, "無資料", Toast.LENGTH_LONG).show();
        }
    }

    static class GetCFuserAsyncTask extends AsyncTask<String, Void, CFUser> {
        private CFUserApi myApiService = null;
        private Context context;
        private ProgressDialog mProgressDialog;

        protected GetCFuserAsyncTask(Context context) {
            this.context = context;
            mProgressDialog = ProgressDialog.show(context
                    , ""
                    , context.getResources().getString(R.string.system_processing));
        }

        //Download from GAE & Local
        //params:UserID
        //Return ArrayList<CFUser>
        @Override
        protected CFUser doInBackground(String... params) {
            String userID = params[0];
            CFUser cfUser;

            ///////////////////// get from GAE /////////////////////////
            if (myApiService == null) {  // Only do this once
                CFUserApi.Builder builder = new CFUserApi.Builder(AndroidHttp.newCompatibleTransport(),
                        new AndroidJsonFactory(), null)
                        // options for running against local devappserver
                        // - 10.0.2.2:8080 is localhost's IP address in Android emulator
                        // - turn off compression when running against local devappserver
                        .setRootUrl("https://graceful-design-89523.appspot.com/_ah/api/")
                                //.setRootUrl("http://10.0.2.2:8080/_ah/api/")
                        .setGoogleClientRequestInitializer(new GoogleClientRequestInitializer() {
                            @Override
                            public void initialize(AbstractGoogleClientRequest<?> abstractGoogleClientRequest) throws IOException {
                                abstractGoogleClientRequest.setDisableGZipContent(true);
                            }
                        });
                myApiService = builder.build();
            }

            try {
                CFUserApi.Get api = myApiService.get(userID);
                cfUser = api.execute();
            } catch (IOException e) {
                e.getMessage();
                return null;    //失敗
            }
            if (cfUser == null) return null;
            else return cfUser;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        protected void onPostExecute(CFUser result) {
            if (mProgressDialog != null && mProgressDialog.isShowing()) mProgressDialog.dismiss();
            if (result == null)
                Toast.makeText(context, "無資料", Toast.LENGTH_LONG).show();
        }
    }

}
