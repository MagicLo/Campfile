package tw.binary.dipper.gcm;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import tw.binary.dipper.util.DbHelper;

/**
 * Created by eason on 2015/5/6.
 */
public class GcmContentProvider extends ContentProvider {

    public static final String COL_ID = "_id";
    //Table messages
    public static final String TABLE_MESSAGES = "messages"; //Table name
    public static final String MESSAGE_COL_ID = "_id";
    public static final String MESSAGE_COL_MSG = "msg";
    public static final String MESSAGE_COL_FROM = "email";
    public static final String MESSAGE_COL_TO = "email2";
    public static final String MESSAGE_COL_AT = "at";
    //column position
    public static final int MESSAGE_POS_MSG = 1;    //0 is _id
    public static final int MESSAGE_POS_FROM = 2;
    public static final int MESSAGE_POS_TO = 3;
    public static final int MESSAGE_POS_AT = 4;
    //Table caller
    public static final String TABLE_CALLER = "caller";     //Table name
    public static final String CALLER_COL_ID = "_id";
    public static final String CALLER_COL_NAME = "name";
    public static final String CALLER_COL_EMAIL = "email";
    public static final String CALLER_COL_COUNT = "count";
    //column position
    public static final int CALLER_POS_NAME = 1;
    public static final int CALLER_POS_EMAIL = 2;
    public static final int CALLER_POS_COUNT = 3;

    public static final Uri CONTENT_URI_MESSAGES = Uri.parse("content://tw.binary.dipper.gcm/messages");
    public static final Uri CONTENT_URI_CALLER = Uri.parse("content://tw.binary.dipper.gcm/caller");

    private static final int MESSAGES_ALLROW = 1;
    private static final int MESSAGES_ONEROW = 2;
    private static final int CALLER_ALLROW = 3;
    private static final int CALLER_ONEROW = 4;

    private static final UriMatcher uriMatcher;
    private Context mContext;
    private SQLiteDatabase mSQLiteDatabase;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI("tw.binary.dipper.gcm", "messages", MESSAGES_ALLROW);
        uriMatcher.addURI("tw.binary.dipper.gcm", "messages/#", MESSAGES_ONEROW);
        uriMatcher.addURI("tw.binary.dipper.gcm", "caller", CALLER_ALLROW);
        uriMatcher.addURI("tw.binary.dipper.gcm", "caller/#", CALLER_ONEROW);
    }

    @Override
    public boolean onCreate() {
        mContext = getContext();
        DbHelper dbHelper = new DbHelper(mContext);
        mSQLiteDatabase = dbHelper.getWritableDatabase();
        return (mSQLiteDatabase == null) ? false : true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        switch (uriMatcher.match(uri)) {
            case MESSAGES_ALLROW:
            case CALLER_ALLROW:
                qb.setTables(getTableName(uri));
                break;

            case MESSAGES_ONEROW:
            case CALLER_ONEROW:
                qb.setTables(getTableName(uri));
                qb.appendWhere("_id = " + uri.getLastPathSegment());
                break;

            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        Cursor c = qb.query(mSQLiteDatabase, projection, selection, selectionArgs, null, null, sortOrder);
        c.setNotificationUri(mContext.getContentResolver(), uri);
        return c;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {

        long id;
        switch (uriMatcher.match(uri)) {
            case MESSAGES_ALLROW:
                id = mSQLiteDatabase.insertOrThrow(TABLE_MESSAGES, null, values);
                if (values.get(MESSAGE_COL_TO) == null) {
                    mSQLiteDatabase.execSQL("update caller set count=count+1 where email = ?", new Object[]{values.get(MESSAGE_COL_FROM)});
                    mContext.getContentResolver().notifyChange(CONTENT_URI_CALLER, null);
                }
                break;

            case CALLER_ALLROW:
                id = mSQLiteDatabase.insertOrThrow(TABLE_CALLER, null, values);
                break;

            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        Uri insertUri = ContentUris.withAppendedId(uri, id);
        mContext.getContentResolver().notifyChange(insertUri, null);    //每個動作都有
        return insertUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        int count;
        switch (uriMatcher.match(uri)) {
            case MESSAGES_ALLROW:
            case CALLER_ALLROW:
                count = mSQLiteDatabase.delete(getTableName(uri), selection, selectionArgs);
                break;

            case MESSAGES_ONEROW:
            case CALLER_ONEROW:
                count = mSQLiteDatabase.delete(getTableName(uri), "_id = ?", new String[]{uri.getLastPathSegment()});
                break;

            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        mContext.getContentResolver().notifyChange(uri, null);  //每個動作都有
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        int count;
        switch (uriMatcher.match(uri)) {
            case MESSAGES_ALLROW:
            case CALLER_ALLROW:
                count = mSQLiteDatabase.update(getTableName(uri), values, selection, selectionArgs);
                break;

            case MESSAGES_ONEROW:
            case CALLER_ONEROW:
                count = mSQLiteDatabase.update(getTableName(uri), values, "_id = ?", new String[]{uri.getLastPathSegment()});
                break;

            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        mContext.getContentResolver().notifyChange(uri, null);  //每個動作都有
        return count;
    }

    @Override
    public String getType(Uri uri) {
        //自用就不實做了
        return null;
    }

    private String getTableName(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case MESSAGES_ALLROW:
            case MESSAGES_ONEROW:
                return TABLE_MESSAGES;

            case CALLER_ALLROW:
            case CALLER_ONEROW:
                return TABLE_CALLER;
        }
        return null;
    }


}
