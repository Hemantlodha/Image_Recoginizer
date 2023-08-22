package com.example.image_recoginizer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
public class Data extends SQLiteOpenHelper {
    public Data(Context context)
    {
        super(context,"Image.db",null,4);
    }
    @Override
    public void onConfigure(SQLiteDatabase db) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            db.setForeignKeyConstraintsEnabled(true);
        }
        super.onConfigure(db);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table Image(prid integer,image blob)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("drop table if exists Image");
    }
    public boolean insertdata(Integer id,byte[] img){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("prid",id);
        cv.put("image",img);
        long result = db.insert("Image",null,cv);
        if(result==-1)
            return false;
        return true;
    }
    public boolean deletedata(Integer id){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor=db.rawQuery("select * from Image where prid=?",new String[]{String.valueOf(id)});
        if(cursor.getCount()>0) {
            long result = db.delete("Image", "prid=?",new String[]{String.valueOf(id)});
            if (result == -1)
                return false;
            return true;
        }
        return false;
    }
    public Cursor getData(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor= db.rawQuery("select * from Image", null);
        return cursor;
    }
}
