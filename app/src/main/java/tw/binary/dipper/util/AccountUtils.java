package tw.binary.dipper.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.google.identitytoolkit.GitkitUser;
import com.google.identitytoolkit.IdToken;

/**
 * Account and login utilities. This class manages a local shared preferences object
 * that stores which account is currently active, and can store associated information
 * such as Google+ profile info (name, image URL, cover URL) and also the auth token
 * associated with the account.
 */
public class AccountUtils {
    private static final String TAG = LogUtils.makeLogTag(AccountUtils.class);

    private static final String PREF_ACTIVE_ACCOUNT = "chosen_account";

    // these names are are prefixes; the account is appended to them
    private static final String PREF_AUTH_TOKEN = "auth_token_";
    private static final String PREF_LOCAL_ID = "LocalId";
    private static final String PREF_DISPLAY_NAME = "display_name";
    private static final String PREF_PHOTO_URL = "photo_url";
    //private static final String PREFIX_PREF_PLUS_COVER_URL = "plus_cover_url_"; //?
    private static final String PREF_EMAIL = "email";
    private static final String PREF_GITKIT_USER = "gitkitUser";
    private static final String PREF_WELCOME = "welcome_done";
    private static final String PREF_PHONE = "contactPhone";
    private static final String PREF_ADDRESS = "contactAddress";
    private static final String PREF_GCMID = "gcmid";

    /*
    public static final String AUTH_SCOPES[] = {
            Scopes.PLUS_LOGIN,
            Scopes.DRIVE_APPFOLDER,
            "https://www.googleapis.com/auth/userinfo.email"};

    static final String AUTH_TOKEN_TYPE;


    static {
        StringBuilder sb = new StringBuilder();
        sb.append("oauth2:");
        for (String scope : AUTH_SCOPES) {
            sb.append(scope);
            sb.append(" ");
        }
        AUTH_TOKEN_TYPE = sb.toString();
    }*/

    private static SharedPreferences getSharedPreferences(final Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static boolean hasActiveAccount(final Context context) {
        return !TextUtils.isEmpty(getIdProvider(context));
    }

    public static String getIdProvider(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(PREF_ACTIVE_ACCOUNT, null);
    }

    public static boolean setIdProvider(final Context context, final String accountName) {
        LogUtils.LOGD(TAG, "Set active account to: " + accountName);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString(PREF_ACTIVE_ACCOUNT, accountName).apply();
        return true;
    }

    public static String getAuthToken(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String tokenString = sp.getString(PREF_AUTH_TOKEN, null);
        if (tokenString != null) {
            if (!isAuthTokenExpired(tokenString)) {
                return tokenString;
            }
        }
        return null;
    }

    public static void setAuthToken(final Context context, final String authToken) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString(PREF_AUTH_TOKEN, authToken).apply();
    }

    public static boolean isAuthTokenExpired(String tokenString) {
        if (tokenString != null) {
            IdToken idToken = IdToken.parse(tokenString);
            if (idToken != null && !idToken.isExpired()) {
                return false;
            }
        }
        return true;
    }

    public static void setLocalId(final Context context, final String profileId) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString(PREF_LOCAL_ID, profileId).apply();
    }

    public static String getLocalId(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(PREF_LOCAL_ID, null);
    }

    public static boolean hasAccountInfo(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return !TextUtils.isEmpty(sp.getString(PREF_PHONE, null)) && !TextUtils.isEmpty(sp.getString(PREF_EMAIL, null)) ? true : false;
    }

    public static boolean hasToken(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return !TextUtils.isEmpty(sp.getString(PREF_AUTH_TOKEN, null)) ? true : false;
    }

    public static void setDisplayName(final Context context, final String name) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString(PREF_DISPLAY_NAME, name).apply();
    }

    public static String getDisplayName(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(PREF_DISPLAY_NAME, null);
    }

    public static void setPhotoUrl(final Context context, final String imageUrl) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString(PREF_PHOTO_URL, imageUrl).apply();
    }

    public static String getPhotoUrl(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(PREF_PHOTO_URL, null);
    }

    public static void setEmail(final Context context, final String email) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString(PREF_EMAIL, email).apply();
    }

    public static String getEmail(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(PREF_EMAIL, null);
    }

    /*public static void setGcmKey(final Context context, final String gcmKey) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString(PREF_GCM_KEY, gcmKey).apply();
        LogUtils.LOGD(TAG, "GCM key set to: " + sanitizeGcmKey(gcmKey));
    }

    public static String getGcmKey(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String gcmKey = sp.getString(PREF_GCM_KEY, null);

        // if there is no current GCM key, generate a new random one
        if (TextUtils.isEmpty(gcmKey)) {
            gcmKey = UUID.randomUUID().toString();
            LogUtils.LOGD(TAG, "No GCM key. Generating random one: "
                    + sanitizeGcmKey(gcmKey));
            setGcmKey(context, gcmKey);
        }

        return gcmKey;
    }*/

    public static String sanitizeGcmKey(String key) {
        if (key == null) {
            return "(null)";
        } else if (key.length() > 8) {
            return key.substring(0, 4) + "........" + key.substring(key.length() - 4);
        } else {
            return "........";
        }
    }

    public static void setWelcomeDone(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(PREF_WELCOME, true).apply();
    }

    public static boolean isWelcomeDone(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_WELCOME, false);
    }

    public static void setGitkitUser(final Context context, final String user) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString(PREF_GITKIT_USER, user).apply();
    }

    public static GitkitUser getGitkitUser(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String userString = sp.getString(PREF_GITKIT_USER, null);
        if (userString != null) {
            return GitkitUser.fromJsonString(userString);
        }
        return null;
    }

    public static void setPhone(final Context context, final String phone) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString(PREF_PHONE, phone).apply();
    }

    public static String getPhone(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(PREF_PHONE, null);
    }

    public static void setAddress(final Context context, final String address) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString(PREF_ADDRESS, address).apply();
    }

    public static String getAddress(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(PREF_ADDRESS, null);
    }

    public static void clearLoggedInUser(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit()
                //        .remove(ID_TOKEN_KEY)
                .remove(PREF_GITKIT_USER)
                .remove(PREF_AUTH_TOKEN)
                .remove(PREF_PHOTO_URL)
                .remove(PREF_DISPLAY_NAME)
                .remove(PREF_EMAIL)
                .commit();
    }

    public static String getGcmId(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(PREF_GCMID, null);
    }

    public static boolean setGcmId(final Context context, final String gcmId) {
        LogUtils.LOGD(TAG, "Set active account to: " + gcmId);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString(PREF_GCMID, gcmId).apply();
        return true;
    }

    public static boolean isUserLoggedIn(Context context) {
        return AccountUtils.getAuthToken(context) != null && AccountUtils.getGitkitUser(context) != null;
    }

}
