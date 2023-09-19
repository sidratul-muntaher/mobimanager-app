package com.iis.mobimanagercocacola.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.iis.mobimanagercocacola.model.AppList;
import com.iis.mobimanagercocacola.model.Message;
import com.iis.mobimanagercocacola.model.NotificationFile;

import java.util.ArrayList;
import java.util.HashMap;

public class AppDatabaseHelper extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 5;
    // Database Name
    private static final String DATABASE_NAME = "mdm_data";


    // COLUMN NAMES OF locations TABLE

    public static final String TABLE_APP = "TABLE_APP";
    public static final String TABLE_APP_NAME = "TABLE_APP_NAME";
    public static final String TABLE_APP_VERSION = "TABLE_APP_VERSION";
    public static final String TABLE_APP_PKG = "TABLE_APP_PKG";
    public static final String TABLE_APP_ICON = "TABLE_APP_ICON";
    public static final String TABLE_APP_URL = "TABLE_APP_URL";


    public static final String TABLE_SEND_LOCATION = "locations";
    public static final String TABLE_SEND_USES_DATA = "usesData";
    public static final String TABLE_MESSAGE = "message";
    public static final String TABLE_FILE = "files";
    public static final String COLUMN_ID = "id";


    public AppDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        try {

            db.execSQL("CREATE TABLE if not exists " + TABLE_SEND_LOCATION + "(" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "imei_one TEXT," +

                    "imei_two TEXT," +
                    "latitude TEXT," +
                    "longitude TEXT," +
                    "dateTime TEXT," +
                    "address TEXT" +
                    ");");

            db.execSQL("CREATE TABLE if not exists " + TABLE_APP + "(" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    TABLE_APP_NAME + " TEXT," +

                    TABLE_APP_ICON + " TEXT," +
                    TABLE_APP_PKG + " TEXT," +
                    TABLE_APP_VERSION + " TEXT," +
                    TABLE_APP_URL + " TEXT" +
                    ");");

            db.execSQL("CREATE TABLE if not exists " + TABLE_SEND_USES_DATA + "(" +
                    "imei_one TEXT," +
                    "imei_two TEXT," +
                    "mobile_data TEXT," +
                    "wifi_data TEXT," +
                    "date TEXT" +
                    ");");

            db.execSQL("CREATE TABLE if not exists " + TABLE_MESSAGE + "(" +
                    "message_id TEXT," +
                    "message_title TEXT," +
                    "message_details TEXT," +
                    "image_url TEXT," +
                    "date TEXT," +
                    "priority TEXT" +
                    ");");

            db.execSQL("CREATE TABLE if not exists " + TABLE_FILE + "(" +
                    "file_id TEXT," +
                    "file_name TEXT," +
                    "type TEXT," +
                    "file_url TEXT," +
                    "date TEXT," +
                    "size TEXT," +
                    "is_downloaded TEXT," +
                    "description TEXT" +
                    ");");


        } catch (SQLiteException e) {
            e.printStackTrace();
            Log.e("Table creation error", "onCreate error");
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        if (newVersion > 1 && newVersion < 4) {
            try {
                db.execSQL("CREATE TABLE if not exists " + TABLE_SEND_USES_DATA + "(" +
                        "imei_one TEXT," +
                        "imei_two TEXT," +
                        "mobile_data TEXT," +
                        "wifi_data TEXT," +
                        "date TEXT" +
                        ");");
            } catch (SQLiteException e) {
                e.printStackTrace();
                Log.e("Table creation error", "onUpgrade error");
            }
        }

        if (newVersion > 2) {
            try {
                db.execSQL("DROP TABLE if exists " + TABLE_SEND_LOCATION);
                db.execSQL("DROP TABLE if exists " + TABLE_APP);
            } catch (SQLiteException e) {
                e.printStackTrace();
                Log.e("Table dropping error", "onUpgrade error");
            }
        }

        if (newVersion > 3) {
            try {
                db.execSQL("DROP TABLE if exists " + TABLE_SEND_USES_DATA);
            } catch (SQLiteException e) {
                e.printStackTrace();
                Log.e("Table dropping error", "onUpgrade error");
            }
        }

        onCreate(db);
//        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SEND_LOCATION);
//        onCreate(db);
    }

    public boolean insertSendLocationData(String imeiOne, String imeiTwo, String latitude,
                                          String longitude, String address, String dateTime) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("imei_one", imeiOne);
        contentValues.put("imei_two", imeiTwo);
        contentValues.put("latitude", latitude);
        contentValues.put("longitude", longitude);
        contentValues.put("address", address);
        contentValues.put("dateTime", dateTime);
        db.insert(TABLE_SEND_LOCATION, null, contentValues);


        return true;
    }

    public void insertAppData(String name, String pkg, String version, String icon, String url) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(TABLE_APP_ICON, icon);
        contentValues.put(TABLE_APP_PKG, pkg);
        contentValues.put(TABLE_APP_URL, url);
        contentValues.put(TABLE_APP_NAME, name);
        contentValues.put(TABLE_APP_VERSION, version);
        db.insertWithOnConflict(TABLE_APP, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public void deleteApps(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_APP, null, null);
        db.close();
    }
    public ArrayList<AppList> getApplist() {

        ArrayList<AppList> appLists = new ArrayList<>();
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("select * from " + TABLE_APP, null);

        if (cursor.moveToFirst()) {
            do {
                String name = cursor.getString(cursor.getColumnIndex(TABLE_APP_NAME));
                String version = cursor.getString(cursor.getColumnIndex(TABLE_APP_VERSION));
                String pkg = cursor.getString(cursor.getColumnIndex(TABLE_APP_PKG));
                String url = cursor.getString(cursor.getColumnIndex(TABLE_APP_URL));
                String icon = cursor.getString(cursor.getColumnIndex(TABLE_APP_ICON));

                AppList dataModel = new AppList();

                dataModel.setMinVersion(version);
                dataModel.setAppName(name);
                dataModel.setPackageName(pkg);
                dataModel.setAppDownloadUrl(url);
                dataModel.setAppIcon(icon);

                appLists.add(dataModel);
            } while (cursor.moveToNext());
        }

        cursor.close();
        sqLiteDatabase.close();

        return appLists;
    }

    public void updateAppData(String name, String pkg, String version, String icon, String url) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(TABLE_APP_ICON, icon);
        contentValues.put(TABLE_APP_PKG, pkg);
        contentValues.put(TABLE_APP_URL, url);
        contentValues.put(TABLE_APP_NAME, name);
        contentValues.put(TABLE_APP_VERSION, version);
        db.update(TABLE_APP, contentValues, TABLE_APP_PKG + "=?", new String[]{pkg});
    }

    public boolean insertSendUsesData(String imeiOne, String imeiTwo, String mobile_data,
                                      String wifi_data, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("imei_one", imeiOne);
        contentValues.put("imei_two", imeiTwo);
        contentValues.put("mobile_data", mobile_data);
        contentValues.put("wifi_data", wifi_data);
        contentValues.put("date", date);
        db.insert(TABLE_SEND_USES_DATA, null, contentValues);


        return true;
    }

    public boolean insertMessageData(String messageId, String messageTitle, String messageDetails,
                                     String imageUrl, String date, String priority) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("message_id", messageId);
        contentValues.put("message_title", messageTitle);
        contentValues.put("message_details", messageDetails);
        contentValues.put("image_url", imageUrl);
        contentValues.put("date", date);
        contentValues.put("priority", priority);
        db.insert(TABLE_MESSAGE, null, contentValues);
        return true;
    }

    public boolean insertFileData(String fileId, String fileName, String fileType,
                                  String fileUrl, String date, String size, String description, String isDownloaded) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("file_id", fileId);
        contentValues.put("file_name", fileName);
        contentValues.put("type", fileType);
        contentValues.put("file_url", fileUrl);
        contentValues.put("date", date);
        contentValues.put("size", size);
        contentValues.put("description", description);
        contentValues.put("is_downloaded", isDownloaded);
        db.insert(TABLE_FILE, null, contentValues);
        return true;
    }

    // get data from send_location table
    public ArrayList<HashMap<String, String>> getSendLocationData() {

        ArrayList<HashMap<String, String>> sendLocation = new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from " + TABLE_SEND_LOCATION, null);
        res.moveToFirst();

        while (res.isAfterLast() == false) {

            HashMap<String, String> map = new HashMap<String, String>();
            map.put("imei_one", res.getString(res.getColumnIndex("imei_one")));
            map.put("imei_two", res.getString(res.getColumnIndex("imei_two")));
            map.put("latitude", res.getString(res.getColumnIndex("latitude")));
            map.put("longitude", res.getString(res.getColumnIndex("longitude")));
            map.put("address", res.getString(res.getColumnIndex("address")));
            map.put("dateTime", res.getString(res.getColumnIndex("dateTime")));
            sendLocation.add(map);
            res.moveToNext();
        }

        return sendLocation;
    }

    // get data from send_sendUsesData table
    public ArrayList<HashMap<String, String>> getSendUsesData() {

        ArrayList<HashMap<String, String>> sendUsesData = new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from " + TABLE_SEND_USES_DATA, null);
        res.moveToFirst();

        while (res.isAfterLast() == false) {

            HashMap<String, String> map = new HashMap<String, String>();
            map.put("imei_one", res.getString(res.getColumnIndex("imei_one")));
            map.put("imei_two", res.getString(res.getColumnIndex("imei_two")));
            map.put("mobile_data", res.getString(res.getColumnIndex("mobile_data")));
            map.put("wifi_data", res.getString(res.getColumnIndex("wifi_data")));
            map.put("date", res.getString(res.getColumnIndex("date")));
            sendUsesData.add(map);
            res.moveToNext();
        }

        return sendUsesData;
    }

    // get data from send_sendUsesData table
    public boolean isDataAvailable(String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from " + TABLE_SEND_USES_DATA + " where date = '" + date + "'", null);
        res.moveToFirst();

        if (res.moveToFirst()) {
            return true;
        }

        return false;
    }


    // get all message from the list
    public ArrayList<Message> getAllMessageData() {

        ArrayList<Message> messageArrayList = new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from " + TABLE_MESSAGE, null);
        res.moveToFirst();

        while (res.isAfterLast() == false) {
            Message message = new Message();
            message.setMessageId(res.getString(res.getColumnIndex("message_id")));
            message.setMessageTitle(res.getString(res.getColumnIndex("message_title")));
            message.setMessageDetails(res.getString(res.getColumnIndex("message_details")));
            message.setImageUrl(res.getString(res.getColumnIndex("image_url")));
            message.setDateTime(res.getString(res.getColumnIndex("date")));
            message.setPriority(res.getString(res.getColumnIndex("priority")));
            messageArrayList.add(message);
            res.moveToNext();
        }
        return messageArrayList;
    }

    public void deleteAllRowsExceptLast() {
        SQLiteDatabase db = this.getWritableDatabase();

        // Get the last row's ID
        String lastRowIdQuery = "SELECT MAX(" + COLUMN_ID + ") FROM " + TABLE_SEND_LOCATION;
        Cursor cursor = db.rawQuery(lastRowIdQuery, null);
        int lastRowId = 0;
        if (cursor.moveToFirst()) {
            lastRowId = cursor.getInt(0);
        }
        cursor.close();

        // Delete rows except the last one
        int x = db.delete(TABLE_SEND_LOCATION, COLUMN_ID + " < ?", new String[]{String.valueOf(lastRowId)});

        Log.e("TAG", "deleteAllRowsExceptLast: " + x);
        db.close();
    }

    public boolean deleteAllDataFromLocationTable() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from " + TABLE_SEND_LOCATION);
        return true;
    }

    public boolean deleteAllDataFromUsesDataTable() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from " + TABLE_SEND_USES_DATA);
        return true;
    }

    public boolean deleteAllDataFromUsesDataTableExceptToday(String today_date) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from " + TABLE_SEND_USES_DATA + " where date !='" + today_date + "'");
        return true;
    }

    public boolean deleteNotificationFiles(String fileName, String fileType) {
        Log.d("_mdm_firebase", "deleteNotificationFiles: " + fileName + "." + fileType);
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from " + TABLE_FILE + " where file_name ='" + fileName + "' and type = '" + fileType + "'");
        return true;


    }
    //DELETE FROM [DSPCONTENT01].[dbo].[Contact Center] WHERE F10 NOT IN (2096)

    public ArrayList<NotificationFile> getAllFilesData() {

        ArrayList<NotificationFile> notificationFiles = new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from " + TABLE_FILE, null);
        res.moveToFirst();

        while (res.isAfterLast() == false) {
            NotificationFile notificationFile = new NotificationFile();
            notificationFile.setFileId(res.getString(res.getColumnIndex("file_id")));
            notificationFile.setFileName(res.getString(res.getColumnIndex("file_name")));
            notificationFile.setFileType(res.getString(res.getColumnIndex("type")));
            notificationFile.setFileSize(res.getString(res.getColumnIndex("size")));
            notificationFile.setDescription(res.getString(res.getColumnIndex("description")));
            notificationFile.setDownloadUrl(res.getString(res.getColumnIndex("file_url")));
            notificationFile.setFileDate(res.getString(res.getColumnIndex("date")));
            if (res.getString(res.getColumnIndex("is_downloaded")).equalsIgnoreCase("TRUE")) {
                notificationFile.setDownloaded(true);
            } else {
                notificationFile.setDownloaded(false);
            }
            notificationFiles.add(notificationFile);
            res.moveToNext();
        }
        return notificationFiles;
    }

    public boolean updateFileDownloadStatus(String fileId, boolean status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        if (status) {
            cv.put("is_downloaded", "TRUE");
        } else {
            cv.put("is_downloaded", "FALSE");
        }
        db.update(TABLE_FILE, cv, "file_id=" + fileId, null);
        return false;
    }

    public boolean updateUsesData(String mobile_data, String wifi, String date) {
        Log.d("_database_", "mobile_data :" + mobile_data + " wifi :" + wifi + " date :" + date);
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("mobile_data", mobile_data);
        cv.put("wifi_data", wifi);

        db.update(TABLE_SEND_USES_DATA, cv, "date = ?", new String[]{date});
        return false;
    }
}

