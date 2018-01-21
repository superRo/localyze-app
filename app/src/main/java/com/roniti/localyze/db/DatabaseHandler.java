package com.roniti.localyze.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import java.util.ArrayList;

public class DatabaseHandler {

    private DatabaseHelper dbHelper;

    public DatabaseHandler(Context context) {
        dbHelper = new DatabaseHelper(context, DatabaseConstants.DB_NAME, null, DatabaseConstants.DB_VERSION);
    }

    /* Fetch saved favorites. Returns array of GoogleIDs */
    public ArrayList<String> getFaveIDS() {

        ArrayList<String> listOfIds = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.query(DatabaseConstants.DB_TABLE_FAVORITES, null, null, null, null, null, null);
        } catch (SQLiteException e) {
            e.getMessage();
        }
        while (cursor.moveToNext()) {
            String googleID = cursor.getString(cursor.getColumnIndex(DatabaseConstants.COLUMN_google_id));
            listOfIds.add(googleID);
        }
        cursor.close();
        return listOfIds;

    }

    /* Save single result to favorites */
    public void saveFave(String googleID) {
        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(DatabaseConstants.COLUMN_google_id, googleID);
            db.insert(DatabaseConstants.DB_TABLE_FAVORITES, null, values);
        } catch (SQLiteException e) {
            e.getMessage();
        } finally {
            if (db.isOpen()) {
                db.close();
            }

        }

    }

    /* Check if fave already exists before saving it to table */
    public boolean faveExists(String id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selectString = "SELECT * FROM " + DatabaseConstants.DB_TABLE_FAVORITES + " WHERE " + DatabaseConstants.COLUMN_google_id + " =?";

        // Add the String you are searching by here.
        // Put it in an array to avoid an unrecognized token error
        Cursor cursor = db.rawQuery(selectString, new String[] {id});

        boolean hasObject = false;
        if(cursor.moveToFirst()){
            hasObject = true;
        }

        cursor.close();          // Dont forget to close your cursor
        db.close();              //AND your Database!
        return hasObject;
    }

    /* Delete single result from favorites */
    public void deleteFave(String googleID) {

        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();
            db.delete(DatabaseConstants.DB_TABLE_FAVORITES,DatabaseConstants.COLUMN_google_id + "=?",new String[]{googleID});
        } catch (SQLiteException e){
            e.getMessage();
        } finally {
            if (db.isOpen()) {
                db.close();
            }
        }
    }

    /* Delete all favorites */
    public void deleteAllFaves() {
        SQLiteDatabase db = null;
        try{
            db = dbHelper.getWritableDatabase();
            db.delete(DatabaseConstants.DB_TABLE_FAVORITES, null, null);
        } catch (SQLiteException e){
            e.getMessage();
        } finally {
            if (db.isOpen()) {
                db.close();
            }
        }
    }

}
