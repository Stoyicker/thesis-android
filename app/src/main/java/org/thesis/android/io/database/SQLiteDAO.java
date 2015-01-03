package org.thesis.android.io.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.support.annotation.NonNull;

import org.thesis.android.BuildConfig;
import org.thesis.android.R;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class SQLiteDAO extends RobustSQLiteOpenHelper {

    private static final Object DB_LOCK = new Object();
    private static Context mContext;
    private static SQLiteDAO mInstance;

    private SQLiteDAO(@NonNull Context _context) {
        super(_context, String.format(Locale.ENGLISH, _context.getString(R.string
                        .database_name_template), _context.getString(R.string.app_name)), null,
                BuildConfig.VERSION_CODE);
        mContext = _context;
    }

    public static void setup(@NonNull Context _context) {
        SQLiteDAO ret = mInstance;
        if (ret == null) {
            synchronized (DB_LOCK) {
                ret = mInstance;
                if (ret == null) {
                    ret = new SQLiteDAO(_context);
                    mInstance = ret;
                }
            }
        }
        mInstance.getWritableDatabase();//Force database creation
    }

    @Override
    public void onRobustUpgrade(SQLiteDatabase db, int oldVersion,
                                int newVersion) throws SQLiteException {
        //No other database versions to upgrade from.
    }

    public static SQLiteDAO getInstance() {
        return mInstance;
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
