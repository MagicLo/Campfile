package tw.binary.dipper.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.google.identitytoolkit.GitkitClient;
import com.google.identitytoolkit.GitkitUser;
import com.google.identitytoolkit.IdProvider;
import com.google.identitytoolkit.IdToken;
import com.google.identitytoolkit.UiManager;

import java.lang.ref.WeakReference;

import tw.binary.dipper.R;
import tw.binary.dipper.api.cFUserApi.model.CFUser;

/**
 * Created by eason on 2015/4/16.
 */
public class LoginAndAuthHelper {
    private static final String TAG = LogUtils.makeLogTag(LoginAndAuthHelper.class);
    Context mAppContext;
    GitkitClient mGitkitClient;
    UiManager mUiManager;
    // Are we in the started state? Started state is between onStart and onStop.
    boolean mStarted = false;
    // True if we are currently showing UIs to resolve a connection error.
    boolean mResolving = false;
    // The Activity this object is bound to (we use a weak ref to avoid context leaks)
    WeakReference<Activity> mActivityRef;
    // Callbacks interface we invoke to notify the user of this class of useful events
    WeakReference<Callbacks> mCallbacksRef;


    public interface Callbacks {
        void onAuthSuccess(IdToken idToken, GitkitUser gitkitUser);

        void onAuthFailure();
    }

    public LoginAndAuthHelper(Activity activity, Callbacks callbacks) {
        LogUtils.LOGD(TAG, "Helper created.");
        mActivityRef = new WeakReference<Activity>(activity);
        mCallbacksRef = new WeakReference<Callbacks>(callbacks);
        mAppContext = activity.getApplicationContext();
    }

    public void startSignIn() {
        Activity activity = getActivity("start()");
        mAppContext = activity; //update context
        if (activity == null) {
            return;
        }

        if (mStarted) {
            LogUtils.LOGW(TAG, "Helper already started. Ignoring redundant call.");
            return;
        }

        mStarted = true;
        LogUtils.LOGD(TAG, "Helper starting.");

        mUiManager = new UiManager() {
            @Override
            public void setRequestHandler(RequestHandler pRequestHandler) {
                Toast.makeText(mAppContext, "setRequestHandler", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void showStartSignIn(GitkitUser.UserProfile pUserProfile) {
                Toast.makeText(mAppContext, "showStartSignIn", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void showPasswordSignIn(String s) {
                Toast.makeText(mAppContext, "showPasswordSignIn", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void showPasswordSignUp(String s) {
                Toast.makeText(mAppContext, "showPasswordSignUp", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void showPasswordAccountLinking(String s, IdProvider pIdProvider) {
                Toast.makeText(mAppContext, "showPasswordAccountLinking", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void showIdpAccountLinking(String s, IdProvider pIdProvider, IdProvider pIdProvider2) {
                Toast.makeText(mAppContext, "showIdpAccountLinking", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void handleError(ErrorCode pErrorCode, Object... pObjects) {
                Toast.makeText(mAppContext, "handleError", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void dismiss() {
                Toast.makeText(mAppContext, "dismiss", Toast.LENGTH_SHORT).show();
            }
        };

        mGitkitClient = GitkitClient.newBuilder(activity, new GitkitClient.SignInCallbacks() {
            @Override
            public void onSignIn(IdToken idToken, GitkitUser user) {
                onAuthSuccess(idToken, user);
            }

            @Override
            public void onSignInFailed() {
                onAuthFailure();
            }
        }).build(); //setUiManager(mUiManager).build();

        mGitkitClient.startSignIn();
    }

    public void onAuthSuccess(IdToken idToken, GitkitUser user) {
        CFUser cfUser = new CFUser();

        cfUser.setId(user.getLocalId());
        cfUser.setIdprovider(user.getIdProvider().toString());
        cfUser.setPhotoURL(user.getPhotoUrl());
        cfUser.setEmail(user.getEmail());
        cfUser.setDisplayName(user.getDisplayName());
        cfUser.setLastLoginTime(MyUtilsDate.CurrentDateTime());
        cfUser.setLastModifyTime(MyUtilsDate.CurrentDateTime());
        cfUser.setGcmRegId(AccountUtils.getGcmId(mAppContext)); //如果先前Splash已取得GCM ID，就儲存在GDS

        CFUserApiHelper.InsertCFuserAsyncTask insertCFuserAsyncTask = new CFUserApiHelper.InsertCFuserAsyncTask(mAppContext);
        insertCFuserAsyncTask.execute(cfUser);
        try {
            //取得AsyncTask執行結果
            insertCFuserAsyncTask.get();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        //登入成功寫入 Preferences
        AccountUtils.setLocalId(mAppContext, user.getLocalId());
        AccountUtils.setDisplayName(mAppContext, user.getDisplayName());
        AccountUtils.setEmail(mAppContext, user.getEmail());
        AccountUtils.setPhotoUrl(mAppContext
                , user.getPhotoUrl() != null ? user.getPhotoUrl() : "");
        AccountUtils.setIdProvider(mAppContext
                , user.getIdProvider() != null ? user.getIdProvider().toString() : "");
        AccountUtils.setAuthToken(mAppContext, idToken.getTokenString());
        AccountUtils.setGitkitUser(mAppContext, user.toString());

        Callbacks callbacks;
        if (null != (callbacks = mCallbacksRef.get())) {
            callbacks.onAuthSuccess(idToken, user);
        }
    }

    public boolean updateCFUser(Context context, CFUser cfUser) {
        CFUserApiHelper.UpdateCFuserAsyncTask updateCFuserAsyncTask = new CFUserApiHelper.UpdateCFuserAsyncTask(context);
        updateCFuserAsyncTask.execute(cfUser);
        try {
            //取得AsyncTask執行結果
            return updateCFuserAsyncTask.get();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void onAuthFailure() {
        //Toast.makeText(this, "Sign in failed", Toast.LENGTH_LONG).show();
        Callbacks callbacks;
        Toast.makeText(mAppContext, R.string.login_fail, Toast.LENGTH_SHORT).show();
        if (null != (callbacks = mCallbacksRef.get())) {
            callbacks.onAuthFailure();
        }
    }

    public boolean handleActivityResult(int requestCode, int resultCode, Intent intent) {
        return mGitkitClient.handleActivityResult(requestCode, resultCode, intent);
    }

    public boolean handleIntent(Intent intent) {
        return mGitkitClient.handleIntent(intent);
    }

    /*private void reportAuthSuccess(boolean newlyAuthenticated) {
        LogUtils.LOGD(TAG, "Auth success for account, newlyAuthenticated=" + newlyAuthenticated);
        Callbacks callbacks;
        if (null != (callbacks = mCallbacksRef.get())) {
            callbacks.onAuthSuccess();
        }
    }*/

    public boolean isStarted() {
        return mStarted;
    }

    private Activity getActivity(String methodName) {
        Activity activity = mActivityRef.get();
        if (activity == null) {
            LogUtils.LOGD(TAG, "Helper lost Activity reference, ignoring (" + methodName + ")");
        }
        return activity;
    }

    /**
     * Stop the helper. Call this from your Activity's onStop().
     */
    public void stop() {
        if (!mStarted) {
            LogUtils.LOGW(TAG, "Helper already stopped. Ignoring redundant call.");
            return;
        }
        LogUtils.LOGD(TAG, "Helper stopping.");
        mStarted = false;
        //mGitkitClient.destroy();
        mResolving = false;
    }


    public void aboutIsShown() {
        AccountUtils.setWelcomeDone(mAppContext);
    }

    public boolean isAboutShown() {
        return AccountUtils.isWelcomeDone(mAppContext);
    }

    public boolean isUserLoggedIn() {
        return AccountUtils.getAuthToken(mAppContext) != null && AccountUtils.getGitkitUser(mAppContext) != null;
    }

}
