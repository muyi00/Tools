package com.yangjing.tools.DB;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteException;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;


/**
 * Created by YJ on 15/11/18.
 */
public abstract class SQLiteOpenHelper2 {

    private static final String TAG = SQLiteOpenHelper2.class.getSimpleName();

    private final Context mContext;
    private final String mName;
    private final SQLiteDatabase.CursorFactory mFactory;
    private final int mNewVersion;

    private SQLiteDatabase mDatabase;
    private boolean mIsInitializing;
    private boolean mEnableWriteAheadLogging;
    private final DatabaseErrorHandler mErrorHandler;
    private String mSDCardDir;


    /**
     *
     * @param context
     * @param sDCardDir 数据在内存卡中的目录
     * @param name 数据库名称
     * @param factory
     * @param version
     */
    public SQLiteOpenHelper2(Context context, String sDCardDir, String name, CursorFactory factory, int version) {
        this(context, sDCardDir, name, factory, version, null);

    }

    public SQLiteOpenHelper2(Context context, String sDCardDir, String name, CursorFactory factory, int version,
                             DatabaseErrorHandler errorHandler) {
        if (version < 1) throw new IllegalArgumentException("Version must be >= 1, was " + version);

        mContext = context;
        mSDCardDir = sDCardDir;
        mName = name;
        mFactory = factory;
        mNewVersion = version;
        mErrorHandler = errorHandler;
    }


    /**
     * 获取数据库的名称
     *
     * @return
     */
    public String getDatabaseName() {
        return mName;
    }

    /**
     * 获取数据库绝对路径
     * @return
     */
    public  String getDatabasePath(){
       return getDBPath(mSDCardDir, mName);
    }

    /**
     * 启用或禁用日志
     * @param enabled
     */
    public void setWriteAheadLoggingEnabled(boolean enabled) {
        synchronized (this) {
            if (mEnableWriteAheadLogging != enabled) {
                if (mDatabase != null && mDatabase.isOpen() && !mDatabase.isReadOnly()) {
                    if (enabled) {
                        mDatabase.enableWriteAheadLogging();
                    } else {
                        mDatabase.disableWriteAheadLogging();
                    }
                }
                mEnableWriteAheadLogging = enabled;
            }
        }
    }


    /**
     * 获取数据库对象
     * @return
     */
    public SQLiteDatabase getDatabase() {
        synchronized (this) {
            if (mDatabase != null) {
                if (!mDatabase.isOpen()) {
                    // Darn!  The user closed the database by calling mDatabase.close().
                    mDatabase = null;
                } else {
                    // The database is already open for business.
                    return mDatabase;
                }
            }

            if (mIsInitializing) {
                throw new IllegalStateException("getDatabase called recursively");
            }

            SQLiteDatabase db = mDatabase;
            try {
                mIsInitializing = true;

                if (db != null) {

                } else if (mName == null || mSDCardDir == null) {
                    db = SQLiteDatabase.create(null);
                } else {
                    final String path = getDBPath(mSDCardDir, mName);
                    try {
//                        db=SQLiteDatabase.openOrCreateDatabase(path,mFactory,mErrorHandler);
                        db = SQLiteDatabase.openDatabase(path, mFactory,
                                SQLiteDatabase.OPEN_READWRITE, mErrorHandler);
                    } catch (SQLiteException ex) {
                        Log.e(TAG, "Couldn't open " + mName + ":", ex);
//                        db = SQLiteDatabase.openDatabase(path, mFactory,
//                                SQLiteDatabase.OPEN_READWRITE, mErrorHandler);
                        db=SQLiteDatabase.openOrCreateDatabase(path,mFactory,mErrorHandler);
                    }
                }

                onConfigure(db);

                final int version = db.getVersion();
                if (version != mNewVersion) {
                    if (db.isReadOnly()) {
                        throw new SQLiteException("Can't upgrade read-only database from version " +
                                db.getVersion() + " to " + mNewVersion + ": " + mName);
                    }

                    db.beginTransaction();
                    try {
                        if (version == 0) {
                            onCreate(db);
                        } else {
                            if (version > mNewVersion) {
                                onDowngrade(db, version, mNewVersion);
                            } else {
                                onUpgrade(db, version, mNewVersion);
                            }
                        }
                        db.setVersion(mNewVersion);
                        db.setTransactionSuccessful();
                    } finally {
                        db.endTransaction();
                    }
                }

                onOpen(db);

                if (db.isReadOnly()) {
                    Log.w(TAG, "Opened " + mName + " in read-only mode");
                }

                mDatabase = db;
                return db;
            } finally {
                mIsInitializing = false;
                if (db != null && db != mDatabase) {
                    db.close();
                }
            }
        }
    }


    /***
     * 创建数据库并返回绝对路径
     *
     * @param dirPath
     * @param name
     * @return
     */
    private String getDBPath(String dirPath, String name) {
        File fileDir = new File(Environment.getExternalStorageDirectory(),dirPath);
        if (!fileDir.exists()) {
            fileDir.mkdirs();
        }

        File dbFile = new File(fileDir.getAbsolutePath(), name);
        if (!dbFile.exists()) {
            try {
                dbFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return dbFile.getAbsolutePath();
    }


    /**
     * 关闭数据库
     */
    public synchronized void close() {
        if (mIsInitializing) throw new IllegalStateException("Closed during initialization");

        if (mDatabase != null && mDatabase.isOpen()) {
            mDatabase.close();
            mDatabase = null;
        }
    }


    /**
     * 数据库连接配置
     * @param db
     */
    public void onConfigure(SQLiteDatabase db) {
    }


    /**
     * 创建数据库
     * @param db
     */
    public abstract void onCreate(SQLiteDatabase db);

    /**
     * 数据库升级
     * @param db
     * @param oldVersion
     * @param newVersion
     */
    public abstract void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion);

    /**
     * 数据库降级
     * @param db
     * @param oldVersion
     * @param newVersion
     */
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        throw new SQLiteException("Can't downgrade database from version " +
                oldVersion + " to " + newVersion);
    }

    /**
     * 打开数据库
     * @param db
     */
    public void onOpen(SQLiteDatabase db) {
    }


}
