package org.thesis.android.io.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import org.apache.commons.lang3.text.WordUtils;
import org.thesis.android.BuildConfig;
import org.thesis.android.R;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class SQLiteDAO extends RobustSQLiteOpenHelper {

    private static final Object DB_LOCK = new Object();
    private static final String NAME_TABLE_NAME = "NAMES_TABLE",
            GROUPS_TABLE_NAME = "GROUPS_TABLE";
    private final String UNGROUPED_TABLE_NAME;
    private static final String TABLE_KEY_NAME = "NAME", TABLE_KEY_GROUP_NAME = "GROUP_NAME",
            TABLE_KEY_TAG_NAME = "TAG_NAME";
    private static final String SQLITE_MASTER_KEY_TABLE_NAME = "tbl_name";
    private static SQLiteDAO mInstance;

    private SQLiteDAO(@NonNull Context _context) {
        super(_context, String.format(Locale.ENGLISH, _context.getString(R.string
                        .database_name_template), _context.getString(R.string.app_name)), null,
                BuildConfig.VERSION_CODE);
        UNGROUPED_TABLE_NAME = _context.getString(R.string.ungrouped_table_name);
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

        final String createGroupsTableCmd = "CREATE TABLE IF NOT EXISTS " + GROUPS_TABLE_NAME + "" +
                " ( " +
                TABLE_KEY_GROUP_NAME + " TEXT PRIMARY KEY ON CONFLICT IGNORE"
                + " )".toUpperCase(Locale.ENGLISH);

        final String createUngroupedCmd = "CREATE TABLE IF NOT EXISTS " + UNGROUPED_TABLE_NAME +
                " ( " +
                TABLE_KEY_TAG_NAME + " TEXT PRIMARY KEY ON CONFLICT IGNORE"
                + " )".toUpperCase(Locale.ENGLISH);

        synchronized (DB_LOCK) {
            db.execSQL(createNameTableCmd);
            db.execSQL(createGroupsTableCmd);
            db.execSQL(createUngroupedCmd);
            final ContentValues unGroupedIntoGroupsTable = new ContentValues();
            unGroupedIntoGroupsTable.put(TABLE_KEY_GROUP_NAME, UNGROUPED_TABLE_NAME);
            db.insert(GROUPS_TABLE_NAME, null, unGroupedIntoGroupsTable);
        }
    }

    private ContentValues mapNameToStorable(String name) {
        ContentValues ret = new ContentValues();
        ret.put(TABLE_KEY_NAME, name.toUpperCase(Locale.ENGLISH));
        return ret;
    }

    private ContentValues mapTagToStorable(String name) {
        ContentValues ret = new ContentValues();
        ret.put(TABLE_KEY_TAG_NAME, name.toUpperCase(Locale.ENGLISH));
        return ret;
    }


    private ContentValues mapGroupToStorable(String groupName) {
        ContentValues ret = new ContentValues();
        ret.put(TABLE_KEY_GROUP_NAME, groupName.toUpperCase(Locale.ENGLISH));
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

    private String mapStorableToName(Cursor nameCursor) {
        return WordUtils.capitalizeFully(nameCursor.getString(nameCursor.getColumnIndex
                (TABLE_KEY_NAME)));
    }

    private String mapSqliteMasterStorableToUpperCaseTagName(Cursor tagNameCursor) {
        return tagNameCursor.getString(tagNameCursor.getColumnIndex
                (SQLITE_MASTER_KEY_TABLE_NAME)).toLowerCase(Locale.ENGLISH);
    }

    private String mapStorableToTagGroup(Cursor tagGroupCursor) {
        return WordUtils.capitalizeFully(tagGroupCursor.getString(tagGroupCursor.getColumnIndex
                (TABLE_KEY_GROUP_NAME)));
    }

    private String mapStorableToTag(Cursor tagCursor) {
        return WordUtils.capitalizeFully(tagCursor.getString(tagCursor.getColumnIndex
                (TABLE_KEY_TAG_NAME)));
    }

    public List<String> getNames() {
        final List<String> ret = new LinkedList<>();

        final SQLiteDatabase db = getReadableDatabase();
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

    public List<String> getTagGroups() {
        final List<String> ret = new LinkedList<>();

        final SQLiteDatabase db = getReadableDatabase();
        synchronized (DB_LOCK) {
            db.beginTransaction();
            Cursor allStorableGroups = db.query(GROUPS_TABLE_NAME, null, null, null, null, null,
                    null);
            if (allStorableGroups != null && allStorableGroups.moveToFirst()) {
                do {
                    ret.add(mapStorableToTagGroup(allStorableGroups));
                } while (allStorableGroups.moveToNext());
            }
            if (allStorableGroups != null)
                allStorableGroups.close();
            db.setTransactionSuccessful();
            db.endTransaction();
        }
        return ret;
    }

    public List<String> getGroupTags(String groupName) {
        final List<String> ret = new LinkedList<>();

        final SQLiteDatabase db = getReadableDatabase();
        synchronized (DB_LOCK) {
            db.beginTransaction();
            Cursor allStorableTags = db.query(groupName.toUpperCase(Locale.ENGLISH), null, null,
                    null, null, null,
                    null);
            if (allStorableTags != null && allStorableTags.moveToFirst()) {
                do {
                    ret.add(mapStorableToTag(allStorableTags));
                } while (allStorableTags.moveToNext());
            }
            if (allStorableTags != null)
                allStorableTags.close();
            db.setTransactionSuccessful();
            db.endTransaction();
        }
        return ret;
    }

    public Boolean removeTagFromGroup(String tagName, String groupName) {
        final SQLiteDatabase db = getWritableDatabase();
        final Boolean ret;

        synchronized (DB_LOCK) {
            db.beginTransaction();
            ret = db.delete(groupName.toUpperCase(Locale.ENGLISH), TABLE_KEY_TAG_NAME + " = '" +
                    tagName
                            .toUpperCase(Locale.ENGLISH) + "'", null) > 0;
            db.setTransactionSuccessful();
            db.endTransaction();
        }

        return ret;
    }

    public Boolean addTagToGroupAndRemoveFromUngrouped(String tagName, String groupName) {
        final SQLiteDatabase db = getWritableDatabase();
        Boolean ret = Boolean.FALSE;

        //Just in case
        final String createTagTable = "CREATE TABLE IF NOT EXISTS " + groupName.toUpperCase
                (Locale.ENGLISH) +
                " ( " +
                TABLE_KEY_TAG_NAME + " TEXT PRIMARY KEY ON CONFLICT IGNORE"
                + " )".toUpperCase(Locale.ENGLISH);

        synchronized (DB_LOCK) {
            db.beginTransaction();
            db.execSQL(createTagTable);
            db.insert(groupName.toUpperCase(Locale.ENGLISH), null, mapTagToStorable(tagName));
            if (!groupName.toUpperCase(Locale.ENGLISH).contentEquals(UNGROUPED_TABLE_NAME))
                ret = db.delete(UNGROUPED_TABLE_NAME, TABLE_KEY_TAG_NAME + " = '" +
                        tagName
                                .toUpperCase(Locale.ENGLISH) + "'", null) > 0;
            db.setTransactionSuccessful();
            db.endTransaction();
        }
        return ret;
    }

    public Boolean isTagOrGroupNameValid(String name) {
        if (TextUtils.isEmpty(name))
            return Boolean.FALSE;

        final Pattern tagFormatPattern = Pattern.compile("[A-Z0-9_]+");
        final SQLiteDatabase db = getReadableDatabase();
        final String upperCaseName;

        if (!tagFormatPattern.matcher(upperCaseName = name.toUpperCase(Locale.ENGLISH)).matches())
            return Boolean.FALSE;

        synchronized (DB_LOCK) {
            db.beginTransaction();
            Cursor allTablesMatching = db.query("sqlite_master", null,
                    "type = 'table'", null,
                    null,
                    null,
                    null);
            if (allTablesMatching != null && allTablesMatching.moveToFirst()) {
                do {
                    try {
                        Cursor c = db.query(mapSqliteMasterStorableToUpperCaseTagName
                                (allTablesMatching), null, TABLE_KEY_TAG_NAME + " = '" +
                                upperCaseName + "'", null, null, null, null);
                        if (c != null && c.getCount() > 0) {
                            c.close();
                            allTablesMatching.close();
                            db.setTransactionSuccessful();
                            db.endTransaction();
                            return Boolean.FALSE;
                        }
                        if (c != null)
                            c.close();
                    } catch (SQLiteException ignored) {
                        //A few tables do not store tags, so they will throw an exception
                    }
                } while (allTablesMatching.moveToNext());
                allTablesMatching.close();
            }
            db.setTransactionSuccessful();
            db.endTransaction();
            return Boolean.TRUE;
        }
    }

    public void addGroup(String groupName) {
        final SQLiteDatabase db = getWritableDatabase();
        final String createTagTable = "CREATE TABLE IF NOT EXISTS " + groupName.toUpperCase
                (Locale.ENGLISH) +
                " ( " +
                TABLE_KEY_TAG_NAME + " TEXT PRIMARY KEY ON CONFLICT IGNORE"
                + " )".toUpperCase(Locale.ENGLISH);

        synchronized (DB_LOCK) {
            db.beginTransaction();
            db.execSQL(createTagTable);
            db.insert(GROUPS_TABLE_NAME, null, mapGroupToStorable(groupName));
            db.setTransactionSuccessful();
            db.endTransaction();
        }
    }

    public Boolean removeGroup(String groupName) {
        final SQLiteDatabase db = getWritableDatabase();
        final Boolean ret;

        synchronized (DB_LOCK) {
            db.beginTransaction();
            ret = db.delete(GROUPS_TABLE_NAME, TABLE_KEY_GROUP_NAME + " = '" +
                    groupName
                            .toUpperCase(Locale.ENGLISH) + "'", null) > 0;
            db.setTransactionSuccessful();
            db.endTransaction();
        }

        return ret;
    }
}
