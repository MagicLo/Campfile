package tw.binary.dipper.message;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import java.util.HashMap;

/**
 * Created by eason on 2015/5/7.
 */
public class MessageContentProvider extends ContentProvider {
    public static final Uri CONTENT_URI_MESSAGES = Uri.parse("content://tw.binary.dipper/message");
    public static final Uri CONTENT_URI_MESSAGER = Uri.parse("content://tw.binary.dipper/messager");

    private static final int MESSAGE_ALLROWS = 1;
    private static final int MESSAGE_ONEROW = 2;
    private static final int MESSAGER_ALLROWS = 3;
    private static final int MESSAGER_ONEROW = 4;
    private static final int MYRESOURCE_ALLROWS = 5;
    private static final int MYRESOURCE_ONEROW = 6;
    private static final UriMatcher uriMatcher;
    private MessageDbHelper mMessageDbHelper;
    private static HashMap<String, String> projectionMap = new HashMap<String, String>();

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI("tw.binary.dipper", "message", MESSAGE_ALLROWS);
        uriMatcher.addURI("tw.binary.dipper", "message/#", MESSAGE_ONEROW);
        uriMatcher.addURI("tw.binary.dipper", "messager", MESSAGER_ALLROWS);
        uriMatcher.addURI("tw.binary.dipper", "messager/#", MESSAGER_ONEROW);
        uriMatcher.addURI("tw.binary.dipper", "myresource", MYRESOURCE_ALLROWS);
        uriMatcher.addURI("tw.binary.dipper", "myresource/#", MYRESOURCE_ONEROW);
        //ProjectionMap 是將查詢的虛擬欄位轉換成實際的SQL欄位條件的Mapping
        projectionMap.put("_id", MessageDbHelper.MESSAGE_COL_FROM + " as _id ");
        projectionMap.put(MessageDbHelper.MESSAGE_COL_DISPLAYNAME, MessageDbHelper.MESSAGE_COL_DISPLAYNAME);
        projectionMap.put(MessageDbHelper.MESSAGE_COL_PHOTOURL, MessageDbHelper.MESSAGE_COL_PHOTOURL);
        projectionMap.put(MessageDbHelper.MESSAGE_AGG_COUNT, "COUNT(senttime) AS count");
        projectionMap.put(MessageDbHelper.MESSAGE_AGG_SENT, "MAX(senttime) AS senttime");
    }

    @Override
    public boolean onCreate() {
        mMessageDbHelper = new MessageDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = mMessageDbHelper.getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        String groupBy;

        switch (uriMatcher.match(uri)) {
            case MESSAGE_ALLROWS:
            case MESSAGER_ALLROWS:
                qb.setTables(getTableName(uri));
                break;
            case MESSAGE_ONEROW:
            case MESSAGER_ONEROW:
                qb.setTables(getTableName(uri));
                qb.appendWhere("_id = " + uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        switch (uriMatcher.match(uri)) {
            case MESSAGER_ALLROWS:
            case MESSAGER_ONEROW:
                qb.setProjectionMap(projectionMap); //欄位Mapping定義
                groupBy = MessageDbHelper.MESSAGE_COL_FROM;
                break;
            default:
                groupBy = null;
        }
        //Log.i("TAG", "selectionArgs = " + selectionArgs[0]);
        //讀取資料
        Cursor c = qb.query(db, projection, selection, selectionArgs, groupBy, null, sortOrder);
        //String sql = qb.buildQuery(projection, selection, groupBy, null, sortOrder);
        String sql = qb.buildQuery(projection, selection, groupBy, null, sortOrder, null);
        Log.i("TAG", sql);
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = mMessageDbHelper.getWritableDatabase();

        long id;
        switch (uriMatcher.match(uri)) {
            case MESSAGE_ALLROWS:
                //除存message
                id = db.insertOrThrow(MessageDbHelper.TABLE_MESSAGE, null, values);
                //為了Messager的Content Provider的通知更新功能
                Uri updateMessager = MessageContentProvider.CONTENT_URI_MESSAGER;
                Uri insertMessagerUri = ContentUris.withAppendedId(updateMessager, id);
                getContext().getContentResolver().notifyChange(insertMessagerUri, null);
                break;

            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        Uri insertUri = ContentUris.withAppendedId(uri, id);
        getContext().getContentResolver().notifyChange(insertUri, null);
        return insertUri;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mMessageDbHelper.getWritableDatabase();

        int count;
        switch (uriMatcher.match(uri)) {
            case MESSAGE_ALLROWS:
                count = db.update(getTableName(uri), values, selection, selectionArgs);

                break;

            case MESSAGE_ONEROW:
                count = db.update(getTableName(uri), values, "_id = ?", new String[]{uri.getLastPathSegment()});
                break;

            case MESSAGER_ALLROWS:
            case MESSAGER_ONEROW:
                count = 0;
                break;

            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        //為了Messager的Content Provider的通知更新功能，外加通知更新messenger
        Uri updateMessager = MessageContentProvider.CONTENT_URI_MESSAGER;
        Uri updateMessagerUri = ContentUris.withAppendedId(updateMessager, count);
        getContext().getContentResolver().notifyChange(updateMessagerUri, null);
        //一般程序
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mMessageDbHelper.getWritableDatabase();

        int count;
        switch (uriMatcher.match(uri)) {
            case MESSAGE_ALLROWS:
                count = db.delete(getTableName(uri), selection, selectionArgs);
                break;

            case MESSAGE_ONEROW:
                count = db.delete(getTableName(uri), "_id = ?", new String[]{uri.getLastPathSegment()});
                break;

            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    private String getTableName(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case MESSAGE_ALLROWS:
            case MESSAGE_ONEROW:
            case MESSAGER_ALLROWS:
            case MESSAGER_ONEROW:
                return MessageDbHelper.TABLE_MESSAGE;
        }
        return null;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }
}


/*

String[] columns = new String[] { "_id", "item", "description" };

MatrixCursor matrixCursor= new MatrixCursor(columns);
startManagingCursor(matrixCursor);

matrixCursor.addRow(new Object[] { 1, "Item A", "...." });

SimpleCursorAdapter adapter =
        new SimpleCursorAdapter(this, R.layout.layout_row, matrixCursor, ...);

setListAdapter(adapter);

 */