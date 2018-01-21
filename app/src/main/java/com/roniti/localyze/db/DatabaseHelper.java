package com.roniti.localyze.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String createFavoriteTable = "CREATE TABLE " + DatabaseConstants.DB_TABLE_FAVORITES + " ("
                + DatabaseConstants.COLUMN_id + " INTEGER PRIMARY KEY,"
                + DatabaseConstants.COLUMN_google_id + " TEXT)";
        try {
            sqLiteDatabase.execSQL(createFavoriteTable);
        } catch (SQLiteException e) {
            e.getMessage();
        }

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + DatabaseConstants.DB_TABLE_FAVORITES);
    }
}
