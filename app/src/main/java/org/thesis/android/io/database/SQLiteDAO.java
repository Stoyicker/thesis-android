package org.thesis.android.io.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.support.annotation.NonNull;

import org.thesis.android.BuildConfig;
import org.thesis.android.R;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class SQLiteDAO extends RobustSQLiteOpenHelper {

    private static final Object DB_LOCK = new Object();
    private static final String NAME_TABLE_NAME = "NAMES_TABLE";
    private static final String TABLE_KEY_NAME = "NAME";
    private static SQLiteDAO mInstance;

    private SQLiteDAO(@NonNull Context _context) {
        super(_context, String.format(Locale.ENGLISH, _context.getString(R.string
                        .database_name_template), _context.getString(R.string.app_name)), null,
                BuildConfig.VERSION_CODE);
    }

    public static void setup(@NonNull Context _context) {
        SQLiteDAO ret = mInstance;
        if (ret == null) {
            synchronized (DB_LOCK) {
                ret = mInstance;
                if (ret == null) {
                    ret = new SQLiteDAO(_context);
                    mInstance = ret;
                    mInstance.getWritableDatabase();//Force database creation
                }
            }
        }
    }

    @Override
    public void onRobustUpgrade(SQLiteDatabase db, int oldVersion,
                                int newVersion) throws SQLiteException {
        //No other database versions to upgrade from.
    }

    public static SQLiteDAO getInstance() {
        return mInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        super.onCreate(db);

        final String createNameTableCmd = "CREATE TABLE IF NOT EXISTS " + NAME_TABLE_NAME + " ( " +
                TABLE_KEY_NAME + " TEXT PRIMARY KEY ON CONFLICT IGNORE"
                + " )".toUpperCase(Locale.ENGLISH);

        synchronized (DB_LOCK) {
            db.execSQL(createNameTableCmd);
        }
    }

    private ContentValues mapNameToStorable(String name) {
        ContentValues ret = new ContentValues();
        ret.put(TABLE_KEY_NAME, name);
        return ret;
    }

    public void addUserName(String name) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues x = mapNameToStorable(name);
        synchronized (DB_LOCK) {
            db.beginTransaction();
            db.insert(NAME_TABLE_NAME, null, x);
            db.setTransactionSuccessful();
            db.endTransaction();
        }
    }

    private String mapStorableToName(Cursor namesCursor) {
        return namesCursor.getString(namesCursor.getColumnIndex(TABLE_KEY_NAME));
    }

    public List<String> getNames() {
        List<String> ret = new LinkedList<>();

        SQLiteDatabase db = getReadableDatabase();
        synchronized (DB_LOCK) {
            db.beginTransaction();
            Cursor allStorableNames = db.query(NAME_TABLE_NAME, null, null, null, null, null, null);
            if (allStorableNames != null && allStorableNames.moveToFirst()) {
                do {
                    ret.add(mapStorableToName(allStorableNames));
                } while (allStorableNames.moveToNext());
            }
            if (allStorableNames != null)
                allStorableNames.close();
            db.setTransactionSuccessful();
            db.endTransaction();
        }
        return ret;
    }

    /**
     * TODO getTagGroups
     */
    public List<String> getTagGroups() {
        return Arrays.asList("Tag group 1", "tag group 2", "group3",
                "gröup with strange chåräctersñ", "reaaaaally looooooooooooooooong-named group",
                "another group", "plus another one");
    }
}
