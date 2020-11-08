package com.nbut.memorandum;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class NoteDBHelper extends SQLiteOpenHelper {
    //创建Note表
    public static final String CREATE_NOTE = "create table Note ("
            + "id integer primary key autoincrement, "
            + "tag integer, "
            + "num integer, "
            + "textDate text, "
            + "textTime text, "
            + "alarm text, "
            + "mainText text,"
            + "noteTitle text,"
            + "flag text)";

    //创建Tabs表
    public static final String CREATE_TABS = "create table Tabs ("
            + "id integer primary key autoincrement,"
            + "position integer,"
            + "tabName varchar(10) unique)";
    private Context mContext;

    public NoteDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        mContext = context;
    }

    /**
     * 数据库创建
     * @param db
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_NOTE);
        db.execSQL(CREATE_TABS);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists Note");
        db.execSQL("drop table if exists Tabs");
        onCreate(db);
    }

}
