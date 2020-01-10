package com.vincentperez.supercounter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class DbManager {
    private DatabaseHelper dbHelper;

    private Context context;

    private SQLiteDatabase database;

    public DbManager(Context c) {
        context = c;
    }

    public DbManager open() throws SQLException {
        dbHelper = new DatabaseHelper(context);
        database = dbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        dbHelper.close();
    }

    public boolean insert(int counter, String date) {
        ContentValues contentValue = new ContentValues();
        contentValue.put(DatabaseHelper.COUNTER, counter);
        contentValue.put(DatabaseHelper.DATE, date);
        long ret = database.insert(DatabaseHelper.TABLE_NAME, null, contentValue);
        return ret != -1;
    }

    public Cursor fetch() {
        String[] columns = new String[] { DatabaseHelper._ID, DatabaseHelper.COUNTER, DatabaseHelper.DATE };
        Cursor cursor = database.query(DatabaseHelper.TABLE_NAME, columns, null, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }

    public Cursor fetchDate(String date) {
        String[] columns = new String[] { DatabaseHelper._ID, DatabaseHelper.COUNTER, DatabaseHelper.DATE };
        String[] selectionArgs = { date };
        Cursor cursor = database.query(DatabaseHelper.TABLE_NAME, columns, DatabaseHelper.DATE + " = ?", selectionArgs, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }

    public int updateOnId(long _id, int counter, String date) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.COUNTER, counter);
        contentValues.put(DatabaseHelper.DATE, date);
        int i = database.update(DatabaseHelper.TABLE_NAME, contentValues, DatabaseHelper._ID + " = " + _id, null);
        return i;
    }

    public int updateOnDate(int counter, String date) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.COUNTER, counter);
        contentValues.put(DatabaseHelper.DATE, date);
        int i = database.update(DatabaseHelper.TABLE_NAME, contentValues, DatabaseHelper.DATE + " = '" + date + "'", null);
        return i;
    }


    public void deleteOnId(long _id) {
        database.delete(DatabaseHelper.TABLE_NAME, DatabaseHelper._ID + "=" + _id, null);
    }

}

