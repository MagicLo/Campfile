package tw.binary.dipper.message;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by eason on 2015/5/7.
 */
public class MessageDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "campfire.sqlite";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_MESSAGE = "messages";
    public static final String COL_ID = "_id";
    public static final String MESSAGE_COL_MSG = "msg";
    public static final String MESSAGE_COL_FROM = "callerlocalid";
    public static final String MESSAGE_COL_FROM_GCMID = "callergcmid";
    public static final String MESSAGE_COL_TO = "receiverlocalid";
    public static final String MESSAGE_COL_TO_GCMID = "receivergcmid";
    public static final String MESSAGE_COL_DISPLAYNAME = "displayname";
    public static final String MESSAGE_COL_PHOTOURL = "photourl";
    public static final String MESSAGE_COL_SENT = "senttime";
    public static final String MESSAGE_COL_READ = "readtime";
    public static final String MESSAGE_AGG_COUNT = "count";
    public static final String MESSAGE_AGG_SENT = "senttime";

    public MessageDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_MESSAGE + " (" +
                COL_ID + " INTEGER PRIMARY KEY, " +
                MESSAGE_COL_MSG + " text, " +
                MESSAGE_COL_FROM + " text, " +
                //MESSAGE_COL_FROM_GCMID + " text, " +
                MESSAGE_COL_TO + " text, " +
                //MESSAGE_COL_TO_GCMID + " text, " +
                MESSAGE_COL_DISPLAYNAME + " text, " +
                MESSAGE_COL_PHOTOURL + " text, " +
                MESSAGE_COL_SENT + " text, " +
                MESSAGE_COL_READ + " text);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGE);
            onCreate(db);
        }
    }
}