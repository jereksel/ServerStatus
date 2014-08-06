package com.jereksel.serverstatus;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class Helpers {

    //The Android's default system path of your application database.
    private static String DB_PATH = "/data/data/com.jereksel.serverstatus/";

    private static String DB_NAME = "servers.db";

    private static final int DB_VERSION = 1;
    private static final String DB_SERVERS_TABLE = "servers";
    private static final String KEY_ID = "id";
    private static final String ID_OPTIONS = "INTEGER PRIMARY KEY AUTOINCREMENT";
    private static final int ID_COLUMN = 0;
    private static final String KEY_USERNAME = "username";
    private static final String USERNAME_OPTIONS = "TEXT NOT NULL";
    private static final int USERNAME_COLUMN = 1;
    private static final String KEY_HOSTNAME = "hostname";
    private static final String HOSTNAME_OPTIONS = "TEXT NOT NULL";
    private static final int HOSTNAME_COLUMN = 2;
    private static final String KEY_PORT = "port";
    private static final String PORT_OPTIONS = "INTEGER NOT NULL";
    private static final int PORT_COLUMN = 3;
    private static final String KEY_PATH = "path";
    private static final String PATH_OPTIONS = "TEXT NOT NULL";
    private static final int PATH_COLUMN = 4;


    private static final String DB_CREATE_TODO_TABLE =
            "CREATE TABLE " + DB_SERVERS_TABLE + "( " +
                    KEY_ID + " " + ID_OPTIONS + ", " +
                    KEY_USERNAME + " " + USERNAME_OPTIONS + ", " +
                    KEY_HOSTNAME + " " + HOSTNAME_OPTIONS + ", " +
                    KEY_PORT + " " + PORT_OPTIONS + ", " +
                    KEY_PATH + " " + PATH_OPTIONS +
                    ");";


    public static boolean isNumeric(String str) {
        try {
            double d = Double.parseDouble(str);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }


    public static void createDatabase() {
        File file = new File(DB_PATH + DB_NAME);
        if (file.exists()) return;

        SQLiteDatabase db;
        db = SQLiteDatabase.openDatabase(DB_PATH + DB_NAME, null, SQLiteDatabase.CREATE_IF_NECESSARY);
        db.execSQL(DB_CREATE_TODO_TABLE);
        db.close();
    }

    public static void addServer(String username, String hostname, int port, String path) {
        ContentValues newTodoValues = new ContentValues();
        newTodoValues.put(KEY_USERNAME, username);
        newTodoValues.put(KEY_HOSTNAME, hostname);
        newTodoValues.put(KEY_PORT, port);
        newTodoValues.put(KEY_PATH, path);

        SQLiteDatabase db = SQLiteDatabase.openDatabase(DB_PATH + DB_NAME, null, SQLiteDatabase.OPEN_READWRITE);
        db.insert(DB_SERVERS_TABLE, null, newTodoValues);
        db.close();
    }

    public static String[] dbToListView() {

        String[] result = new String[]{};

        String selectQuery = "SELECT  * FROM " + DB_SERVERS_TABLE;
        SQLiteDatabase db = null;

        try {
            db = SQLiteDatabase.openDatabase(DB_PATH + DB_NAME, null, SQLiteDatabase.OPEN_READWRITE);
        } catch (SQLiteException e) {
            return result;
        }
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {

                result = appendValue(result, cursor.getString(USERNAME_COLUMN) + "@" + cursor.getString(HOSTNAME_COLUMN) + ":" + cursor.getString(PORT_COLUMN) + cursor.getString(PATH_COLUMN));

            } while (cursor.moveToNext());
        }
        db.close();
        return result;
    }

    public static String[] dbToIdList() {

        String[] result = new String[]{};

        String selectQuery = "SELECT  * FROM " + DB_SERVERS_TABLE;
        SQLiteDatabase db = null;

        try {
            db = SQLiteDatabase.openDatabase(DB_PATH + DB_NAME, null, SQLiteDatabase.OPEN_READWRITE);
        } catch (SQLiteException e) {
            return result;
        }
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {

                result = appendValue(result, cursor.getString(ID_COLUMN));

            } while (cursor.moveToNext());
        }
        db.close();
        return result;
    }

    private static String[] appendValue(String[] obj, String newObj) {

        ArrayList<String> temp = new ArrayList<String>(Arrays.asList(obj));
        temp.add(newObj);
        return temp.toArray(new String[temp.size()]);

    }

    public static void deleteServer(int id) {

        SQLiteDatabase db;

        try {
            db = SQLiteDatabase.openDatabase(DB_PATH + DB_NAME, null, SQLiteDatabase.OPEN_READWRITE);
        } catch (SQLiteException e) {
            return;
        }

        String where = KEY_ID + "=" + id;
        db.delete(DB_SERVERS_TABLE, where, null);
        db.close();
    }

    public static String[] getInfo(int id) {


        String[] result = null;

        String selectQuery = "SELECT  * FROM " + DB_SERVERS_TABLE + " WHERE " + KEY_ID + " = " + id;
        SQLiteDatabase db = null;

        try {
            db = SQLiteDatabase.openDatabase(DB_PATH + DB_NAME, null, SQLiteDatabase.OPEN_READWRITE);
        } catch (SQLiteException e) {
            return null;
        }
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {

                //          result = appendValue(cursor.getString(USERNAME_COLUMN) + "@" + cursor.getString(HOSTNAME_COLUMN) + ":" + cursor.getString(PORT_COLUMN) + cursor.getString(PATH_COLUMN));
                //            result = appe

                result = new String[]{cursor.getString(USERNAME_COLUMN), cursor.getString(HOSTNAME_COLUMN), cursor.getString(PORT_COLUMN), cursor.getString(PATH_COLUMN)};

            } while (cursor.moveToNext());
        }
        db.close();
        return result;

    }

    public static void updateInfo(int id, String username, String hostname, int port, String path) {

        SQLiteDatabase db;

        try {
            db = SQLiteDatabase.openDatabase(DB_PATH + DB_NAME, null, SQLiteDatabase.OPEN_READWRITE);
        } catch (SQLiteException e) {
            return;
        }

        String where = KEY_ID + "=" + id;
        ContentValues updateTodoValues = new ContentValues();
        updateTodoValues.put(KEY_PATH, path);
        updateTodoValues.put(KEY_USERNAME, username);
        updateTodoValues.put(KEY_PORT, port);
        updateTodoValues.put(KEY_HOSTNAME, hostname);
        db.update(DB_SERVERS_TABLE, updateTodoValues, where, null);
        db.close();
    }

}
