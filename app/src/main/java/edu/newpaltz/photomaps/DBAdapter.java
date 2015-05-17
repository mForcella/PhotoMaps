package edu.newpaltz.photomaps;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DBAdapter extends SQLiteOpenHelper {

    private Context myContext;
    //private SQLiteDatabase myDatabase;
    private static final String DB_NAME = "photoMap.sqlite";
    private static final String DB_PATH = MyApplication.getAppContext().getDatabasePath(DB_NAME).getPath();

    // photo
    private static final String TABLE_PHOTO = "photo";
    private static final String KEY_ID = "id";
    private static final String KEY_LAT = "lat";
    private static final String KEY_LNG = "lng";
    private static final String KEY_COMMENT = "comment";
    private static final String KEY_PHOTO = "photo";
    private static final String KEY_DATE = "date";
    private static final String CREATE_PHOTO = "CREATE TABLE IF NOT EXISTS " + TABLE_PHOTO +
            "(" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + KEY_LAT + " TEXT, " +
            KEY_LNG + " TEXT, " + KEY_COMMENT + " TEXT, " + KEY_PHOTO + " TEXT, " +
            KEY_DATE + " TEXT);";

    // constructor
    public DBAdapter (Context context) throws IOException {
        super(context, DB_NAME, null, 1);
        myContext = context;
        boolean dbExist = checkDatabase();
        if (dbExist)
            openDatabase();
        else
            createDatabase();
    }

    public void createDatabase() {
        boolean dbExist = checkDatabase();
        if(!dbExist) {
            this.getReadableDatabase();
            try {
                copyDatabase();
            } catch(IOException e) {
                throw new Error("Error copying database: "+e.getMessage());
            }
        }
    }

    private Boolean checkDatabase() {
        boolean checkDb = false;
        try {
            File dbFile = new File(DB_PATH);
            checkDb = dbFile.exists();
        } catch(SQLiteException e) {
            System.out.println("Database doesn't exist: "+e.getMessage());
        }
        return checkDb;
    }

    private void copyDatabase() throws IOException {
        InputStream myInput = myContext.getAssets().open(DB_NAME);
        OutputStream myOutput = new FileOutputStream(DB_PATH);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer)) > 0) {
            myOutput.write(buffer,0,length);
        }
        myOutput.flush();
        myOutput.close();
        myInput.close();
    }

    public void openDatabase() throws SQLiteException {
        //myDatabase = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
        SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_PHOTO);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS" + TABLE_PHOTO);
        onCreate(db);
    }

    /********************** CRUD OPERATIONS **********************/

    public void addPhoto(Photo photo) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_LAT, photo.getLat());
        values.put(KEY_LNG, photo.getLng());
        values.put(KEY_COMMENT, photo.getComment());
        values.put(KEY_PHOTO, photo.getPhoto());
        values.put(KEY_DATE, photo.getDate());
        db.insert(TABLE_PHOTO, null, values);
        db.close();
    }

}