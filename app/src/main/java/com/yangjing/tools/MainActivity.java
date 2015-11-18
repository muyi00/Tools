package com.yangjing.tools;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.yangjing.tools.DB.MyOpenHelper;

public class MainActivity extends AppCompatActivity {

    private MyOpenHelper helper;
    private SQLiteDatabase db;
    private TextView count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        count = (TextView) findViewById(R.id.count);


        helper = new MyOpenHelper(this);
        db = helper.getDatabase();

    }

    public void Btn1(View view) {
        db.execSQL("delete from shelvesinfo");
        count.setText("数据条数: 0");
    }

    public void Btn2(View view) {
        db.execSQL(
                "insert into shelvesinfo(id,hjh,dnm,ghm,ygh,scantime,outtime) values(null,?,?,?,?,?,?)",
                new Object[]{"1111111", //
                        "2222222",//
                        "33333",//
                        "4444",//
                        "55555", //
                        ""});
        Cursor c = db.rawQuery("select count(*) from shelvesinfo", new String[]{});
        while (c.moveToNext()) {
            count.setText("数据条数: " + c.getInt(0));
        }
    }


}
