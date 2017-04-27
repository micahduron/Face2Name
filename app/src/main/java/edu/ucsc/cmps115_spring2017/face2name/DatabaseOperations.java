package edu.ucsc.cmps115_spring2017.face2name;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.ContactsContract;
import android.util.Log;

/**
 * Created by evanlouie on 4/20/17.
 */

public class DatabaseOperations extends SQLiteOpenHelper {
    public static final int database_version = 1;
    public String CREATE_QUERY = "CREATE TABLE " + TableData.TableInfo.TABLE_NAME + "(" + TableData.TableInfo.USER_NAME + " TEXT, " + TableData.TableInfo.USER_ID + " INT);";
    public String INSERT_QUERY = "INSERT INTO " + TableData.TableInfo.TABLE_NAME + " VALUES ('Bob', '9123');";

    public DatabaseOperations(Context context) {
        super(context, TableData.TableInfo.DATABASE_NAME, null, database_version);
        Log.d("Database operations", "Database created");

    }

    @Override
    public void onCreate(SQLiteDatabase sdb) {
        sdb.execSQL(CREATE_QUERY);
        Log.d("Database operations", "Table created");

    }

    @Override
    public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {

    }

    public void insertInfo(DatabaseOperations dop) {
        SQLiteDatabase SQ = dop.getWritableDatabase();
        SQ.execSQL(INSERT_QUERY);
        Log.d("Database operations", "Inserted");
    }
}
