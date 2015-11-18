package com.yangjing.tools.DB;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by YJ on 15/11/18.
 */
public class MyOpenHelper extends SQLiteOpenHelper2 {


    public MyOpenHelper(Context context) {
        super(context, "AAAAAAAAAAAAAAA", "tools.db", null, 3);
        //super(context, "tools2.db", null, 1);
    }

    /**
     * 创建数据库
     *
     * @param db
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        System.out.println("onCreate");
        db.execSQL("CREATE TABLE shelvesinfo (id INTEGER PRIMARY KEY, hjh NVARCHAR(13),dnm NVARCHAR(13),ghm NVARCHAR(25),ygh NVARCHAR(4),scantime NVARCHAR(30),outtime NVARCHAR(30))");
    }

    /**
     * 数据库升级
     *
     * @param db
     * @param oldVersion
     * @param newVersion
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        System.out.println("onUpgrade");
    }
}
