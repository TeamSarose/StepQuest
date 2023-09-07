package com.SAROSE.StepQuest.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.SAROSE.StepQuest.util.Util;


public class DatabaseHandler extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "UserData";

    // Users table
    private static final String TABLE_USERS = "users";
    private static final String COLUMN_USER_USERNAME = "username";
    private static final String COLUMN_USER_PASSWORD = "password";

    // Steps Table
    private final static String TABLE_STEPS = "steps";

    private static DatabaseHandler instance;

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating main Objective of DB
    public static synchronized DatabaseHandler getInstance(final Context context) {
        if (instance == null) {
            instance = new DatabaseHandler(context.getApplicationContext());
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create the users table
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                + COLUMN_USER_USERNAME + " TEXT PRIMARY KEY,"
                + COLUMN_USER_PASSWORD + " TEXT" + ")";
        db.execSQL(CREATE_USERS_TABLE);

        // Create steps table
        db.execSQL("CREATE TABLE " + TABLE_STEPS + " (date INTEGER, steps INTEGER)");

    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
//        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DETAILS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_STEPS);

        // Create tables again
        onCreate(db);
    }

    // Insert a new user
    public void addUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_USERNAME, username);
        values.put(COLUMN_USER_PASSWORD, password);

        // Inserting Row
        db.insert(TABLE_USERS, null, values);
        db.close(); // Closing database connection
    }


    // Check if a user exists
    public boolean checkUser(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[] { COLUMN_USER_USERNAME },
                COLUMN_USER_USERNAME + "=?", new String[] { username }, null, null, null, null);
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    public String getUserPassword(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT password FROM users WHERE username=?", new String[]{username});
        String password = null;
        if (cursor.moveToFirst()) {
            password = cursor.getString(0);
        }
        cursor.close();
        db.close();
        return password;
    }


    // Check if DB contains any Data
    public String serachifnotnull(){
        SQLiteDatabase db=this.getReadableDatabase();
        String query ="SELECT * FROM " + TABLE_USERS;
        Cursor cursor = db.rawQuery(query,null);
        String a,d;
        d="null";
        if(cursor.moveToFirst()){
            do{
                a=cursor.getString(0);
                if (a!=null){
                    d="not null";
                    break;
                }
            }
            while (cursor.moveToNext());
        }
        return d;

    }

    // Filter steps table according to the input parameters
    public Cursor query(final String[] columns, final String selection,
                        final String[] selectionArgs, final String groupBy, final String having,
                        final String orderBy, final String limit) {
        return getReadableDatabase()
                // Perform the query on the TABLE_STEPS table with the provided parameters
                .query(TABLE_STEPS, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
    }

    //    Insert a new day and steps into the DB
    public void insertNewDay(long date, int steps) {
        getWritableDatabase().beginTransaction();
        try {
            Cursor c = getReadableDatabase().query(TABLE_STEPS, new String[]{"date"}, "date = ?",
                    new String[]{String.valueOf(date)}, null, null, null);
            if (c.getCount() == 0 && steps >= 0) {

                // add 'steps' to yesterdays count
                addToLastEntry(steps);

                // add today
                ContentValues values = new ContentValues();
                values.put("date", date);
                // use the negative steps as offset
                values.put("steps", -steps);
                getWritableDatabase().insert(TABLE_STEPS, null, values);
            }
            c.close();

            getWritableDatabase().setTransactionSuccessful();
        } finally {
            getWritableDatabase().endTransaction();
        }
    }

    //    Update last steps entry to the final date
    public void addToLastEntry(int steps) {
        getWritableDatabase().execSQL("UPDATE " + TABLE_STEPS + " SET steps = steps + " + steps +
                " WHERE date = (SELECT MAX(date) FROM " + TABLE_STEPS + ")");
    }

    // Restore dates and steps from Imported file and insert into the DB
    public boolean insertDayFromBackup(long date, int steps) {
        getWritableDatabase().beginTransaction();
        boolean newEntryCreated = false;
        try {
            ContentValues values = new ContentValues();
            values.put("steps", steps);
            int updatedRows = getWritableDatabase()
                    .update(TABLE_STEPS, values, "date = ?", new String[]{String.valueOf(date)});
            if (updatedRows == 0) {
                values.put("date", date);
                getWritableDatabase().insert(TABLE_STEPS, null, values);
                newEntryCreated = true;
            }
            getWritableDatabase().setTransactionSuccessful();
        } finally {
            getWritableDatabase().endTransaction();
        }
        return newEntryCreated;
    }

    //    Get the total steps till the current day
    public int getTotalWithoutToday() {
        Cursor c = getReadableDatabase()
                .query(TABLE_STEPS, new String[]{"SUM(steps)"}, "steps > 0 AND date > 0 AND date < ?",
                        new String[]{String.valueOf(Util.getToday())}, null, null, null);
        c.moveToFirst();
        int re = c.getInt(0);
        c.close();
        return re;
    }


    public int getSteps(final long date) {
        Cursor c = getReadableDatabase().query(TABLE_STEPS, new String[]{"steps"}, "date = ?",
                new String[]{String.valueOf(date)}, null, null, null);
        c.moveToFirst();
        int re;
        if (c.getCount() == 0) re = Integer.MIN_VALUE;
        else re = c.getInt(0);
        c.close();
        return re;
    }

    // Calculate average
    public int getDaysWithoutToday() {
        Cursor c = getReadableDatabase()
                .query(TABLE_STEPS, new String[]{"COUNT(*)"}, "steps > ? AND date < ? AND date > 0",
                        new String[]{String.valueOf(0), String.valueOf(Util.getToday())}, null,
                        null, null);
        c.moveToFirst();
        int re = c.getInt(0);
        c.close();
        return re < 0 ? 0 : re;
    }

    // Get Current day
    public int getDays() {
        // todays is not counted yet
        int re = this.getDaysWithoutToday() + 1;
        return re;
    }
    // Save current Steps
    public void saveCurrentSteps(int steps) {
        ContentValues values = new ContentValues();
        values.put("steps", steps);
        if (getWritableDatabase().update(TABLE_STEPS, values, "date = -1", null) == 0) {
            values.put("date", -1);
            getWritableDatabase().insert(TABLE_STEPS, null, values);
        }

        }

        // get current steps
    public int getCurrentSteps() {
        int re = getSteps(-1);
        return re == Integer.MIN_VALUE ? 0 : re;
    }
}

