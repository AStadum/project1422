package com.addressbook.thorrism.addressbook;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.sql.SQLException;

/**
 * Created by Lucas Crawford on 1/6/2015.
 */
public class DatabaseAdapter{

    //Database column names
    public static final String KEY_ID       = "_id";
    public static final String KEY_NAME     = "name";
    public static final String KEY_NUMBER   = "number";
    public static final String KEY_ADDRESS  = "address";
    public static final String KEY_EMAIL    = "email";


    //Database information
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDatabase;
    private static final String DATABASE_NAME  = "AddressBook";
    private static final String TABLE_NAME     = "Contacts";
    private static final int DATABASE_VERSION  = 1;

    //Application context
    private static Context mCtx;

    //Create table query
    private static final String CREATE_DATABASE =
            "CREATE TABLE if not exists " + TABLE_NAME + " (" +
                    KEY_ID + " integer PRIMARY KEY autoincrement," +
                    KEY_NAME + "," +
                    KEY_NUMBER + "," +
                    KEY_ADDRESS + "," +
                    KEY_EMAIL + "," +
                    " UNIQUE (" + KEY_NAME +"));";

    //Database helper class
    private class DatabaseHelper extends SQLiteOpenHelper{

        public DatabaseHelper(Context context){
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db){
            db.execSQL(CREATE_DATABASE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }
    }

    //Constructor for the adapter
    public DatabaseAdapter(Context ctx){
        mCtx = ctx;
    }

    public DatabaseAdapter open() throws SQLException{
        mDbHelper = new DatabaseHelper(mCtx);
        mDatabase = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close(){
        if(mDbHelper != null){
            mDbHelper.close();
        }
    }

    /**
     * Adds a new contact to insert into the database.
     * @param contact - a contact created from user input
     * @return long to indicate results of the insert
     */
    public long createContact(Contact contact){
        ContentValues values = new ContentValues();

        values.put(KEY_NAME, contact.getFirstName() + " " + contact.getLastName());
        values.put(KEY_NUMBER, contact.getNumber());
        values.put(KEY_EMAIL, contact.getEmail());
        values.put(KEY_ADDRESS, contact.getAddress());

        return mDatabase.insert(TABLE_NAME, null, values);
    }

    /**
     * Fetch all contacts that exist in the database.
     * @return Cursor with the data from the table row entry for each contact
     */
    public Cursor fetchAll(){
        mDatabase = mDbHelper.getReadableDatabase();

        Cursor mCursor = mDatabase.query(TABLE_NAME, new String[]{
                KEY_ID,
                KEY_NAME,
                KEY_NUMBER,
                KEY_EMAIL,
                KEY_ADDRESS},
                null, null, null, null, null);

        if(mCursor != null)
            mCursor.moveToFirst();
        return mCursor;
    }

    /**
     * Delete a contact with the specific name from the database.
     *
     * Possibly changed to ID since two people can have the same name??
     */
    public boolean deleteContact(String name){
        return mDatabase.delete(TABLE_NAME, KEY_NAME + "=" + name, null) > 0;
    }

    /**
     * Truncate all contacts from the database. This is how to clear the contacts list
     */
    public boolean truncateContacts(){
        return mDatabase.delete(TABLE_NAME, null, null) > 0;
    }


    public void testCreate(){
        for(int i=0; i<10; ++i){
            Contact contact = new Contact();
            contact.setFirstName(contact.getFirstName() + " " + Integer.toString(i));
            createContact(contact);
        }
    }

    //ADD UPDATE, DELETE ALL, FETCH-BY-NAME
}
