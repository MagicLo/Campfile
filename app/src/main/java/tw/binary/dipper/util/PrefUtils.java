package tw.binary.dipper.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;


/**
 * Utilities and constants related to app preferences.
 */
public class PrefUtils {
    private static final String TAG = LogUtils.makeLogTag("PrefUtils");
    public static final String PREF_LOCAL_TIMES = "pref_local_times";
    // Sync sessions with local calendar
    public static final String PREF_SYNC_CALENDAR = "pref_sync_calendar";
    // Boolean indicating whether we performed the (one-time) welcome flow.
    public static final String PREF_WELCOME_DONE = "pref_welcome_done";
    public static final String PREF_BASE_CURRENCY = "base_currency";

    public static boolean isUsingLocalTime(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_LOCAL_TIMES, false);
    }

    public static void setUsingLocalTime(final Context context, final boolean usingLocalTime) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(PREF_LOCAL_TIMES, usingLocalTime).commit();
    }

    public static boolean shouldSyncCalendar(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_SYNC_CALENDAR, false);
    }

    public static void registerOnSharedPreferenceChangeListener(final Context context,
                                                                SharedPreferences.OnSharedPreferenceChangeListener listener) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.registerOnSharedPreferenceChangeListener(listener);
    }

    public static void unregisterOnSharedPreferenceChangeListener(final Context context,
                                                                  SharedPreferences.OnSharedPreferenceChangeListener listener) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.unregisterOnSharedPreferenceChangeListener(listener);
    }

    //未來可用在偵測手機語系，以設定幣別
    public static void setBaseCurrency(final Context context, final String imageUrl) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString(PREF_BASE_CURRENCY, imageUrl).apply();
    }

    public static String getBaseCurrency(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(PREF_BASE_CURRENCY, null);
    }
}
