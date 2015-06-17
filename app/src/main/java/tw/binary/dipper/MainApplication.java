package tw.binary.dipper;

import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;

public class MainApplication extends com.orm.SugarApp {
    public CharSequence mTitle;    //Function Bar 的文字
    public String[] uNavigationmenu;
    public boolean OAuth = false; //儲存使用者登入驗證狀態

    //public static DbHelper dbHelper;
    public static SQLiteDatabase db;
    public static SharedPreferences sp;

    public static final String BASE_CURRENCY = "currency";
    public static final String DEFAULT_CURRENCY = "NTD";

    @Override
    public void onCreate() {
        super.onCreate();
        mTitle = getResources().getString(R.string.app_name);    //Function Bar 的文字
        //uNavigationmenu = getResources().getStringArray(R.array.navigationmenu);

        PreferenceManager.setDefaultValues(this, R.xml.preference, false);
        sp = PreferenceManager.getDefaultSharedPreferences(this);

        //dbHelper = new DbHelper(this);
        //db = dbHelper.getWritableDatabase();
        //new InitTask(MainApplication.this).execute();
    }


}
