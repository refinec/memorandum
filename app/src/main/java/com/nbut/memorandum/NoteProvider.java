package com.nbut.memorandum;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class NoteProvider extends ContentProvider {
    public static final int NOTE_DIR = 0;//访问所有数据
    public static final int NOTE_ITEM = 1;//访问单条数据
    public static final int TABS_DIR = 2;//访问所有数据
    public static final int TABS_ITEM = 3;//访问单条数据
    public static final String AUTHORITY = "com.nbut.memorandum.provider";
    private static UriMatcher uriMatcher;

    private NoteDBHelper dbHelper;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, "note", NOTE_DIR);
        uriMatcher.addURI(AUTHORITY, "note/#", NOTE_ITEM);
        uriMatcher.addURI(AUTHORITY, "tabs", TABS_DIR);
        uriMatcher.addURI(AUTHORITY, "tabs/#", TABS_ITEM);
    }

    @Override
    public boolean onCreate() {
        //创建数据库和表
        dbHelper = new NoteDBHelper(getContext(), "Notes.db", null, 1);
        dbHelper.getWritableDatabase();
        return true;
    }

    /**
     * 插入数据
     *
     * @param uri
     * @param values
     * @return uri
     */
    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Uri uriReturn = null;
        switch (uriMatcher.match(uri)) {
            case NOTE_DIR:
            case NOTE_ITEM:
                long newNoteId = db.insert("Note", null, values);
                uriReturn = Uri.parse("content://" + AUTHORITY + "/note/" + newNoteId);
                break;
            case TABS_DIR:
            case TABS_ITEM:
                try {
                    long newTabsId = db.insert("Tabs", null, values);
                    uriReturn = Uri.parse("content://" + AUTHORITY + "/tabs/" + newTabsId);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }
        getContext().getContentResolver().notifyChange(uri, null);//通知改变
        return uriReturn;
    }

    /**
     * 删除数据
     *
     * @param uri
     * @param selection
     * @param selectionArgs
     * @return 删除的记录数
     */
    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int deletedRows = 0;
        switch (uriMatcher.match(uri)) {
            case NOTE_DIR:
                deletedRows = db.delete("Note", selection, selectionArgs);
                break;
            case NOTE_ITEM:
                String noteId = uri.getPathSegments().get(1);
                deletedRows = db.delete("Note", "id = ?", new String[]{noteId});
                break;
            case TABS_DIR:
                deletedRows = db.delete("Tabs", selection, selectionArgs);
                break;
            case TABS_ITEM:
                String tabId = uri.getPathSegments().get(1);
                deletedRows = db.delete("Tabs", "id = ?", new String[]{tabId});
                break;
            default:
                break;
        }
        if (deletedRows > 0) {
            //通知改变
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return deletedRows;
    }

    /**
     * 根据传入的内容URI来返回相应的MIME类型
     *
     * @param uri
     * @return mimetype
     */
    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (uriMatcher.match(uri)) {
            case NOTE_DIR:
                return "vnd.android.cursor.dir/vnd.com.nbut.memorandum.provider.note";
            case NOTE_ITEM:
                return "vnd.android.cursor.item/vnd.com.nbut.memorandum.provider.note";
            case TABS_DIR:
                return "vnd.android.cursor.dir/vnd.com.nbut.memorandum.provider.tabs";
            case TABS_ITEM:
                return "vnd.android.cursor.item/vnd.com.nbut.memorandum.provider.tabs";
        }
        return null;
    }

    /**
     * 查询数据
     *
     * @param uri
     * @param projection
     * @param selection
     * @param selectionArgs
     * @param sortOrder
     * @return cursor
     */
    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        //查询数据
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        switch (uriMatcher.match(uri)) {
            case NOTE_DIR:
                cursor = db.query("Note", projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case NOTE_ITEM:
                String noteId = uri.getPathSegments().get(1);
                cursor = db.query("Note", projection, "id = ?", new String[]{noteId},
                        null, null, sortOrder);
                break;
            case TABS_DIR:
                cursor = db.query("Tabs", projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case TABS_ITEM:
                String tabId = uri.getPathSegments().get(1);
                cursor = db.query("Tabs", projection, "id = ?", new String[]{tabId}, null, null, sortOrder);
                break;
            default:
                break;
        }
        return cursor;
    }

    /**
     * 更新数据
     *
     * @param uri
     * @param values
     * @param selection
     * @param selectionArgs
     * @return
     */
    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values,
                      @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int updatedRows = 0;
        switch (uriMatcher.match(uri)) {
            case NOTE_DIR:
                updatedRows = db.update("Note", values, selection, selectionArgs);
                break;
            case NOTE_ITEM:
                String noteId = uri.getPathSegments().get(1);
                updatedRows = db.update("Note", values, "id = ?", new String[]{noteId});
                break;
            case TABS_DIR:
                updatedRows = db.update("Tabs", values, selection, selectionArgs);
                break;
            case TABS_ITEM:
                String tabId = uri.getPathSegments().get(1);
                updatedRows = db.update("Tabs", values, "id = ?", new String[]{tabId});
                break;
            default:
                break;
        }
        if (updatedRows > 0) {
            getContext().getContentResolver().notifyChange(uri, null);//通知改变
        }
        return updatedRows;
    }
}
