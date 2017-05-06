package edu.ucsc.cmps115_spring2017.face2name.Identity;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by micah on 4/29/17.
 */

public final class IdentityStorage extends SQLiteOpenHelper {
    public abstract class AsyncQueryCallbacks<T> {
        protected void onSuccess(T result) {}

        protected void onError(Exception ex) {
            ex.printStackTrace();
        }
    }

    public IdentityStorage(Context context) {
        this(context, DBInfo.DB_NAME, null, DBInfo.VERSION);
    }

    private IdentityStorage(Context context, String dbName, SQLiteDatabase.CursorFactory factory, int dbVersion) {
        super(context, dbName, factory, dbVersion);
    }

    private IdentityStorage(Context context, String dbName, SQLiteDatabase.CursorFactory factory, int dbVersion, DatabaseErrorHandler errHandler) {
        super(context, dbName, factory, dbVersion, errHandler);
    }

    @Override
    public void onCreate(final SQLiteDatabase db) {
        db.execSQL(Queries.CreateTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void storeIdentity(Identity identity) {
        SQLiteDatabase db = getWritableDatabase();

        String[] queryParams = new String[] {
                identity.key,
                identity.name
        };
        db.rawQuery(Queries.InsertIdentity, queryParams);
    }

    public void storeIdentity(final Identity identity, final AsyncQueryCallbacks<Void> callbacks) {
        AsyncQuery<Void> query = new AsyncQuery<Void>(callbacks) {
            @Override
            protected Void onExecute() {
                storeIdentity(identity);

                return null;
            }
        };
        query.execute();
    }

    public List<Identity> dumpIdentities() {
        SQLiteDatabase db = getReadableDatabase();

        Cursor queryResult = db.rawQuery(Queries.DumpIdentities, null);

        int rowCount = queryResult.getCount();
        List<Identity> ret = new ArrayList<>(rowCount);

        if (rowCount > 0) {
            do {
                String key = queryResult.getString(0);
                String name = !queryResult.isNull(1) ? queryResult.getString(1) : null;

                ret.add(new Identity(key, name));
            } while (queryResult.moveToNext());
        }
        queryResult.close();

        return ret;
    }

    public void dumpIdentities(final AsyncQueryCallbacks<List<Identity>> callbacks) {
        AsyncQuery<List<Identity>> query = new AsyncQuery<List<Identity>>(callbacks) {
            @Override
            protected List<Identity> onExecute() {
                return dumpIdentities();
            }
        };
        query.execute();
    }

    public Identity getIdentity(Identity identity) {
        if (identity.key == null) {
            throw new RuntimeException("Identity's key field must not be null.");
        }
        SQLiteDatabase db = getReadableDatabase();
        String[] queryParams = new String[] {
                identity.key
        };
        Cursor queryResult = db.rawQuery(Queries.GetIdentity, queryParams);

        Identity result = null;

        if (queryResult.getCount() > 0) {
            String name = !queryResult.isNull(1) ? queryResult.getString(1) : null;
            result = new Identity(identity.key, name);
        }
        queryResult.close();

        return result;
    }

    public void getIdentity(final Identity identity, final AsyncQueryCallbacks<Identity> callbacks) {
        AsyncQuery<Identity> query = new AsyncQuery<Identity>(callbacks) {
            @Override
            protected Identity onExecute() {
                return getIdentity(identity);
            }
        };
        query.execute();
    }

    private class AsyncQueryResult<T> {
        public T value;
        public Exception err;
    }

    private abstract class AsyncQuery<T> extends AsyncTask<Void, Void, AsyncQueryResult<T>> {
        AsyncQuery() {
            super();
        }
        AsyncQuery(AsyncQueryCallbacks<T> callbacks) {
            super();

            if (callbacks != null) {
                mCallbacks = callbacks;
            }
        }

        protected abstract T onExecute();

        protected void onSuccess(T result) {
            if (mCallbacks == null) return;

            mCallbacks.onSuccess(result);
        }

        protected void onError(Exception ex) {
            if (mCallbacks == null) return;

            mCallbacks.onError(ex);
        }

        protected void onStart() {

        }

        protected void onComplete() {

        }

        @Override
        protected void onPreExecute() {
            onStart();
        }

        @Override
        protected AsyncQueryResult<T> doInBackground(Void... nothing) {
            AsyncQueryResult<T> ret = new AsyncQueryResult<>();

            try {
                ret.value = onExecute();
            } catch (Exception ex) {
                ret.err = ex;
            }
            return ret;
        }

        @Override
        protected void onPostExecute(AsyncQueryResult<T> result) {
            onComplete();

            if (result.value != null) {
                onSuccess(result.value);
            } else {
                onError(result.err);
            }
        }

        private AsyncQueryCallbacks<T> mCallbacks = new AsyncQueryCallbacks<T>() {
            @Override
            protected void onSuccess(T result) {}
        };
    }

    private static class DBInfo implements BaseColumns {
        final static int VERSION = 1;
        final static String DB_NAME = "Face2Name";
        final static String TABLE_NAME = "identities";
    }

    private static class Queries {
        final static String CreateTable = "CREATE TABLE IF NOT EXISTS " + DBInfo.TABLE_NAME +
                                            "(key TEXT PRIMARY KEY NOT NULL," +
                                            "name TEXT)";
        final static String DumpIdentities = "SELECT * FROM " + DBInfo.TABLE_NAME;
        final static String InsertIdentity = "INSERT OR REPLACE (key, name) INTO " + DBInfo.TABLE_NAME + " VALUES" +
                                                "(?1," +
                                                "COALESCE((SELECT name FROM " + DBInfo.TABLE_NAME + "WHERE key=?1), ?2))";
        final static String GetIdentity = "SELECT name FROM " + DBInfo.TABLE_NAME + " WHERE key=?";
    }
}
