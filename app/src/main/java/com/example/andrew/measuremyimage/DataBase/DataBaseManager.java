package com.example.andrew.measuremyimage.DataBase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by Andrew on 9/19/2014.
 */
public class DataBaseManager extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 5;//increment to have DB changes take effect
    private static final String DATABASE_NAME = "MeasureMyImage";

    // Log cat tag
    private static final String LOG = "DataBaseManager";

    // Table Names
    private static final String TABLE_USERS = "Users";
    private static final String TABLE_IMAGE = "Image";
    private static final String TABLE_REFERENCE_OBJECT = "ReferenceObject";
    private static final String TABLE_PREFERENCE = "Preference";

    // Common column names across both tables
    private static final String ROW_ID = "rowid";
    private static final String CREATED_ON = "CreatedOn";
    private static final String USER_NAME = "UserName"; // primary key for  Users, Foreign key for Image, ReferenceObject

    // USERS column names
    private static final String PASSWORD = "Password";

    // IMAGE column names
    private static final String IMAGE = "UserImage";

    //REFERENCE_OBJECT OBJECT column names
    private static final String OBJECT_NAME = "ObjectName";
    private static final String HEIGHT = "Height";
    private static final String WIDTH = "Width";
    private static final String UNIT_OF_MEASURE = "UnitOfMeasure";

    // PREFERENCE column names
    private static final String CAMERA_HEIGHT = "CameraHeight";

    // Table Create Statements
    //CREATE_TABLE_USERS
    private static final String CREATE_TABLE_USERS = "CREATE TABLE "
            + TABLE_USERS + "(" + USER_NAME + " TEXT PRIMARY KEY,"
            + PASSWORD + " TEXT," + CREATED_ON + " DATETIME" + ")";

    //CREATE_TABLE_IMAGE
    private static final String CREATE_TABLE_IMAGE = "CREATE TABLE "
            + TABLE_IMAGE + "(" + IMAGE + " BLOB," + CREATED_ON + " DATETIME,"
            + USER_NAME + " TEXT)";

    //TABLE_REFERENCE_OBJECT
    private static final String CREATE_TABLE_REFERENCE_OBJECT = "CREATE TABLE "
            + TABLE_REFERENCE_OBJECT + "(" + OBJECT_NAME + " TEXT," + HEIGHT + " INTEGER," + WIDTH + " INTEGER,"
            + UNIT_OF_MEASURE + " TEXT," + CREATED_ON + " DATETIME," + USER_NAME + " TEXT, PRIMARY KEY("+ OBJECT_NAME + "," + USER_NAME +"))";

    //TABLE_REFERENCE_OBJECT
    private static final String CREATE_TABLE_PREFERENCE = "CREATE TABLE "
            + TABLE_PREFERENCE + "(" + USER_NAME + " TEXT," + CAMERA_HEIGHT + " DOUBLE,"
            + UNIT_OF_MEASURE + " TEXT," + CREATED_ON + " DATETIME, PRIMARY KEY("+ USER_NAME +"))";

    //Singleton
    private static DataBaseManager mInstance = null;

    public static DataBaseManager getInstance(Context aContext) {
        if (mInstance == null) {
            mInstance = new DataBaseManager(aContext.getApplicationContext());
        }
        return mInstance;
    }
    // constructor
    private DataBaseManager(Context aContext) {
        super(aContext,DATABASE_NAME, null,DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase aSQLiteDatabase) {
        // creating required tables
        Log.e(LOG, "Entering: onCreate");
        aSQLiteDatabase.execSQL(CREATE_TABLE_USERS);
        aSQLiteDatabase.execSQL(CREATE_TABLE_IMAGE);
        aSQLiteDatabase.execSQL(CREATE_TABLE_REFERENCE_OBJECT);
        aSQLiteDatabase.execSQL(CREATE_TABLE_PREFERENCE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase aSQLiteDatabase, int aOldVersion, int aNewVersion) {
        // on upgrade drop older tables
        Log.e(LOG, "Entering: onUpgrade OldVersion["+aOldVersion+"] NewVersion["+aNewVersion+"]");
        aSQLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        aSQLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_IMAGE);
        aSQLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_REFERENCE_OBJECT);
        aSQLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_PREFERENCE);

        // create new tables
        onCreate(aSQLiteDatabase);
    }

    // closing database
    public void closeDB() {
        SQLiteDatabase db = this.getReadableDatabase();
        if (db != null && db.isOpen())
            db.close();
    }

    //******************************User Table function*********************************

    /*
    * Creating a User
    */
    public boolean CreateAUser(UserSchema aUser) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(USER_NAME, aUser.getUserName());
        values.put(PASSWORD, aUser.getPassword());
        values.put(CREATED_ON, getDateTime());

        // insert row
        Log.e(LOG, "Insert: User["+aUser.getUserName()+"] Password["+aUser.getPassword()+"]");
        return db.insert(TABLE_USERS,null,values) > 0;
    }

    /*
    * get single User
    */
    public UserSchema getUser(String aUserName) {
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT  * FROM " + TABLE_USERS + " WHERE "
                + USER_NAME + " = '" + aUserName+"'";

        Log.e(LOG, selectQuery);

        Cursor c = db.rawQuery(selectQuery, null);

        if (c != null)
            c.moveToFirst();

        UserSchema user = new UserSchema();
        user.setUserName(c.getString(c.getColumnIndex(USER_NAME)));
        user.setPassword((c.getString(c.getColumnIndex(PASSWORD))));
        user.setCreatedOn(c.getString(c.getColumnIndex(CREATED_ON)));

        c.close();
        return user;
    }

    /*
    * Return true if user exists
    */
    public boolean DoesUserExist(String aUserName, String aPassword) {
        SQLiteDatabase db = this.getReadableDatabase();
        boolean retVal = false;

        String selectQuery = "SELECT  * FROM " + TABLE_USERS + " WHERE "
                + USER_NAME + " = '" + aUserName + "' AND "
                + PASSWORD + " = '" + aPassword + "'";

        Log.e(LOG, selectQuery);

        Cursor c = db.rawQuery(selectQuery, null);

        // record found return true
        if (c.getCount() > 0)
            retVal = true;

        c.close();
        return retVal;
    }

    //************************Image Table Functions*************************

    /*
    * Creating an Image
    */
    public boolean CreateAnImage(ImageSchema aImage) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(IMAGE, GetBitmapByteArray(aImage.getImage()));
        values.put(USER_NAME, aImage.getUserName());
        values.put(CREATED_ON, getDateTime());

        // insert row
        Log.e(LOG, "Insert: Image for User["+aImage.getUserName()+"]");
        return db.insert(TABLE_IMAGE,null,values) > 0;
    }

    /*
    * getting all Images for user
    */
    public List<ImageSchema> getAllImagesForUser(String aUserName) {
        List<ImageSchema> imageList = new ArrayList<ImageSchema>();
        String selectQuery = "SELECT "+ROW_ID+", * FROM " + TABLE_IMAGE
                + " WHERE " + USER_NAME + " = '" + aUserName +"'";

        Log.e(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        Log.e(LOG, "getAllImagesForUser Count["+c.getCount()+"]");
        if (c.getCount() > 0 ) {
            c.moveToFirst();
            do {
                ImageSchema imageSchema = new ImageSchema();
                //convert BLOB to image
                byte[] byteArray = c.getBlob((c.getColumnIndex(IMAGE)));
                Bitmap bitmapImage = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                imageSchema.setId((c.getInt(c.getColumnIndex(ROW_ID))));
                imageSchema.setImage(bitmapImage);
                imageSchema.setUserName((c.getString(c.getColumnIndex(USER_NAME))));
                imageSchema.setCreatedOn(c.getString(c.getColumnIndex(CREATED_ON)));

                // adding to imageList
                imageList.add(imageSchema);
            } while (c.moveToNext());
        }

        c.close();
        return imageList;
    }

    /*
* Delete an Image
*/
    public void DeleteAnImage(int Id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_IMAGE, ROW_ID + " = ?",
                new String[] { String.valueOf(Id) });
    }

    /*
* get single User
*/
    public ImageSchema getImageById(Integer aImageId) {
        Log.e(LOG, "Entering: getImageById");
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT "+ROW_ID+", * FROM " + TABLE_IMAGE
                + " WHERE " + ROW_ID + " = " + aImageId.toString();

        Log.e(LOG, selectQuery);

        Cursor c = db.rawQuery(selectQuery, null);

        if (c != null)
            c.moveToFirst();

        ImageSchema image = new ImageSchema();
        //convert BLOB to image
        byte[] byteArray = c.getBlob((c.getColumnIndex(IMAGE)));
        Bitmap bitmapImage = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        image.setId((c.getInt(c.getColumnIndex(ROW_ID))));
        image.setImage(bitmapImage);
        image.setUserName((c.getString(c.getColumnIndex(USER_NAME))));
        image.setCreatedOn(c.getString(c.getColumnIndex(CREATED_ON)));

        c.close();
        return image;
    }


    //******************************Reference object Table function*********************************

    /*
    * Creating a Reference object
    */
    public boolean CreateAReferenceObject(ReferenceObjectSchema aObject) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(OBJECT_NAME, aObject.getObjectName());
        values.put(HEIGHT, aObject.getHeight());
        values.put(WIDTH, aObject.getWidth());
        values.put(UNIT_OF_MEASURE, aObject.getUnitOfMeasure());
        values.put(CREATED_ON, getDateTime());
        values.put(USER_NAME, aObject.getUserName());

        // insert row
        Log.e(LOG, "Insert: Reference Object["+aObject.getObjectName()+"] User["+aObject.getUserName()+"]");
        return db.insert(TABLE_REFERENCE_OBJECT,null,values) > 0;
    }

    /*
* getting all Reference objects for user
*/
    public List<ReferenceObjectSchema> getAllReferenceObjectsForUser(String aUserName) {
        List<ReferenceObjectSchema> objectList = new ArrayList<ReferenceObjectSchema>();
        String selectQuery = "SELECT "+ROW_ID+", * FROM " + TABLE_REFERENCE_OBJECT
                + " WHERE " + USER_NAME + " = '" + aUserName +"'";

        Log.e(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        Log.e(LOG, "getAllReferenceObjectsForUser Count["+c.getCount()+"]");
        if (c.getCount() > 0 ) {
            c.moveToFirst();
            do {
                ReferenceObjectSchema referenceObjectSchema = new ReferenceObjectSchema();
                referenceObjectSchema.setId((c.getInt(c.getColumnIndex(ROW_ID))));
                referenceObjectSchema.setObjectName(c.getString(c.getColumnIndex(OBJECT_NAME)));
                referenceObjectSchema.setHeight(c.getInt(c.getColumnIndex(HEIGHT)));
                referenceObjectSchema.setWidth(c.getInt(c.getColumnIndex(WIDTH)));
                referenceObjectSchema.setUnitOfMeasure(c.getString(c.getColumnIndex(UNIT_OF_MEASURE)));
                referenceObjectSchema.setCreatedOn(c.getString(c.getColumnIndex(CREATED_ON)));
                referenceObjectSchema.setUserName((c.getString(c.getColumnIndex(USER_NAME))));

                // adding to imageList
                objectList.add(referenceObjectSchema);
            } while (c.moveToNext());
        }

        c.close();
        return objectList;
    }

    /*
* Delete an reference object
*/
    public void DeleteReferenceObject(String aName, String aUserName) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_REFERENCE_OBJECT, OBJECT_NAME + " = ? and " + USER_NAME + " = ? ",
                new String[] { aName, aUserName});
    }


    //******************************Preference Table function*********************************

    /*
    * Creating a Preference
    */
    public boolean CreateAPreference(PreferenceSchema aPreference) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(USER_NAME, aPreference.getUserName());
        values.put(CAMERA_HEIGHT, aPreference.getCameraHeight());
        values.put(UNIT_OF_MEASURE, aPreference.getUnitOfMeasure());
        values.put(CREATED_ON, getDateTime());

        // insert row
        Log.e(LOG, "Insert: User["+aPreference.getUserName()+"] Camera Height["+ Double.toString(aPreference.getCameraHeight()) +
                "] Unit Of Measure["+aPreference.getUnitOfMeasure() +"]");
        return db.insert(TABLE_PREFERENCE ,null,values) > 0;
    }

    /*
    * get single Preference by User
    */
    public PreferenceSchema getPreference(String aUserName) {
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT  * FROM " + TABLE_PREFERENCE + " WHERE "
                + USER_NAME + " = '" + aUserName+"'";

        Log.e(LOG, selectQuery);

        Cursor c = db.rawQuery(selectQuery, null);

        if (c != null)
            c.moveToFirst();

        PreferenceSchema preference = new PreferenceSchema();
        preference.setUserName(c.getString(c.getColumnIndex(USER_NAME)));
        preference.setCameraHeight(c.getDouble(c.getColumnIndex(CAMERA_HEIGHT)));
        preference.setUnitOfMeasure(c.getString(c.getColumnIndex(UNIT_OF_MEASURE)));

        c.close();
        return preference;
    }

    /*
    * Return true if Preference by User exists
    */
    public boolean DoesPreferenceExist(String aUserName) {
        SQLiteDatabase db = this.getReadableDatabase();
        boolean retVal = false;

        String selectQuery = "SELECT  * FROM " + TABLE_PREFERENCE + " WHERE "
                + USER_NAME + " = '" + aUserName + "'";

        Cursor c = db.rawQuery(selectQuery, null);

        // record found return true
        if (c.getCount() > 0)
            retVal = true;

        c.close();
        return retVal;
    }

    /*
    * modify a single Preference
    */
    public boolean modifyPreference(PreferenceSchema aPreference) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues newValues = new ContentValues();
        newValues.put(CAMERA_HEIGHT, aPreference.getCameraHeight());
        newValues.put(UNIT_OF_MEASURE, aPreference.getUnitOfMeasure());
        newValues.put(CREATED_ON, getDateTime());

        db.beginTransaction();
        int i = 0;
        try {
            i = db.update(TABLE_PREFERENCE , newValues, USER_NAME + "='" + aPreference.getUserName() + "'", null);

            if ( i > 0 )
                db.setTransactionSuccessful();
        }
        finally {
            db.endTransaction();
        }
        return i > 0;
    }

    //************************************Helper functions***************
    /*
    * Convert Bitmap to BLOB storage byte array
     */
    private byte[] GetBitmapByteArray(Bitmap aBitmap)
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        aBitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
         byte[] bArray = bos.toByteArray();
        return bArray;
    }


    /**
     * get datetime
     * */
    private String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }
}
