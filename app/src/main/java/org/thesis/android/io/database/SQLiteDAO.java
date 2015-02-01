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
import org.thesis.android.devutil.CLog;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class SQLiteDAO extends RobustSQLiteOpenHelper {

    private static final Object DB_LOCK = new Object();
    private static final String NAMES_TABLE_NAME = "NAMES_TABLE",
            GROUPS_TABLE_NAME = "GROUPS_TABLE", MY_MESSAGES_TABLE_NAME = "MY_MESSAGE_IDS_TABLE",
            FETCHED_STAMPS_TABLE_NAME = "FETCHED_STAMPS_TABLE_NAME";
    private final String UNGROUPED_TABLE_NAME;
    private static final String TABLE_KEY_NAME = "NAME", TABLE_KEY_GROUP_NAME = "GROUP_NAME",
            TABLE_KEY_TAG_NAME = "TAG_NAME", TABLE_KEY_MESSAGE_ID = "MESSAGE_ID", TABLE_KEY_EPOCH = "TIMESTAMP";
    private static final String SQLITE_MASTER_KEY_TABLE_NAME = "tbl_name";
    private static SQLiteDAO mInstance;

    private SQLiteDAO(@NonNull Context _context) {
        super(_context, String.format(Locale.ENGLISH, _context.getString(R.string
                        .database_name_template), _context.getString(R.string.app_name)), null,
                BuildConfig.VERSION_CODE);
        UNGROUPED_TABLE_NAME = _context.getString(R.string.ungrouped_table_name); //Loaded here because it's locale-dependent
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

        final String createNameTableCmd = "CREATE TABLE IF NOT EXISTS " + NAMES_TABLE_NAME + " ( " +
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

        final String createMyMsgsCmd = "CREATE TABLE IF NOT EXISTS " + MY_MESSAGES_TABLE_NAME + " ( " +
                "" + TABLE_KEY_MESSAGE_ID + " TEXT PRIMARY KEY ON CONFLICT REPLACE )".toUpperCase
                (Locale.ENGLISH);

        final String createFetchedStampsCmd = ("CREATE TABLE IF NOT EXISTS " + FETCHED_STAMPS_TABLE_NAME
                + " ( " + TABLE_KEY_TAG_NAME + " TEXT PRIMARY KEY ON CONFLICT IGNORE," +
                " " + TABLE_KEY_EPOCH + " INTEGER DEFAULT 0" +
                ")").toUpperCase(
                Locale.ENGLISH);

        synchronized (DB_LOCK) {
            db.execSQL(createNameTableCmd);
            db.execSQL(createGroupsTableCmd);
            db.execSQL(createUngroupedCmd);
            db.execSQL(createFetchedStampsCmd);
            final ContentValues unGroupedIntoGroupsTable = new ContentValues();
            unGroupedIntoGroupsTable.put(TABLE_KEY_GROUP_NAME, UNGROUPED_TABLE_NAME);
            db.insert(GROUPS_TABLE_NAME, null, unGroupedIntoGroupsTable);
            db.execSQL(createMyMsgsCmd);
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

    private ContentValues mapFetchedEpochToStorable(String upperCaseTagName, Long epoch) {
        ContentValues ret = new ContentValues();
        ret.put(TABLE_KEY_TAG_NAME, upperCaseTagName);
        ret.put(TABLE_KEY_EPOCH, epoch);
        return ret;
    }

    private ContentValues mapMessageIdToStorable(String messageId) {
        ContentValues ret = new ContentValues();
        ret.put(TABLE_KEY_MESSAGE_ID, messageId);
        return ret;
    }

    public void addUserName(String name) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues x = mapNameToStorable(name);
        synchronized (DB_LOCK) {
            db.beginTransaction();
            db.insert(NAMES_TABLE_NAME, null, x);
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

    private Long mapStorableToTimestamp(Cursor epochCursor) {
        return (long) epochCursor.getInt(epochCursor.getColumnIndex
                (TABLE_KEY_EPOCH));
    }

    public List<String> getNames() {
        final List<String> ret = new LinkedList<>();

        final SQLiteDatabase db = getReadableDatabase();
        synchronized (DB_LOCK) {
            db.beginTransaction();
            Cursor allStorableNames = db.query(NAMES_TABLE_NAME, null, null, null, null, null, null);
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

        deleteTagTableIfNotOnAnyGroup(tagName);

        return ret;
    }

    private void deleteTagTableIfNotOnAnyGroup(String tagName) {
        if (TextUtils.isEmpty(tagName)) {
            return;
        }

        final String upperCaseTagName = tagName.toUpperCase(Locale.ENGLISH);
        final String formattedTag = WordUtils.capitalizeFully(tagName);
        final List<String> groups = getTagGroups();
        for (String group : groups) {
            final List<String> tagsInThisGroup = getGroupTags(group);
            if (tagsInThisGroup.contains(formattedTag)) {
                return;
            }
        }

        final SQLiteDatabase db = getWritableDatabase();
        final String dropTableStatement = "DROP TABLE IF EXISTS '" + upperCaseTagName + "'";

        synchronized (DB_LOCK) {
            db.beginTransaction();
            db.execSQL(dropTableStatement);
            db.setTransactionSuccessful();
            db.endTransaction();
        }
    }

    public void addTagToUngrouped(String tagName) {
        final SQLiteDatabase db = getWritableDatabase();
        final String groupName = UNGROUPED_TABLE_NAME;

        //Just in case
        final String createGroupTableCmd = "CREATE TABLE IF NOT EXISTS " + groupName.toUpperCase
                (Locale.ENGLISH) +
                " ( " +
                TABLE_KEY_TAG_NAME + " TEXT PRIMARY KEY ON CONFLICT IGNORE"
                + " )".toUpperCase(Locale.ENGLISH);

        synchronized (DB_LOCK) {
            db.beginTransaction();
            db.execSQL(createGroupTableCmd);
            db.insert(groupName.toUpperCase(Locale.ENGLISH), null, mapTagToStorable(tagName));
            db.setTransactionSuccessful();
            db.endTransaction();
        }
    }

    public Boolean addTagToGroupAndRemoveFromUngrouped(String tagName, String groupName) {
        final SQLiteDatabase db = getWritableDatabase();
        Boolean ret = Boolean.FALSE;

        //Just in case
        final String createGroupTableCmd = "CREATE TABLE IF NOT EXISTS " + groupName.toUpperCase
                (Locale.ENGLISH) +
                " ( " +
                TABLE_KEY_TAG_NAME + " TEXT PRIMARY KEY ON CONFLICT IGNORE"
                + " )".toUpperCase(Locale.ENGLISH);

        synchronized (DB_LOCK) {
            db.beginTransaction();
            db.execSQL(createGroupTableCmd);
            db.insert(groupName.toUpperCase(Locale.ENGLISH), null, mapTagToStorable(tagName));
            if (!groupName.toUpperCase(Locale.ENGLISH).contentEquals(UNGROUPED_TABLE_NAME))
                ret = db.delete(UNGROUPED_TABLE_NAME, TABLE_KEY_TAG_NAME + " = '" +
                        tagName.toUpperCase(Locale.ENGLISH) + "'", null) > 0;
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
                    final String tableName = mapSqliteMasterStorableToUpperCaseTagName
                            (allTablesMatching);
                    try {
                        Cursor c = db.query(tableName, null, TABLE_KEY_TAG_NAME + " = '" +
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
                        //A few tables don't store tags so they'll throw an exception,
                        // but it's fine
                        CLog.w("Table " + tableName + " skipped on group name check.");
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
        final String createGroupTableCmd = "CREATE TABLE IF NOT EXISTS " + groupName.toUpperCase
                (Locale.ENGLISH) +
                " ( " +
                TABLE_KEY_TAG_NAME + " TEXT PRIMARY KEY ON CONFLICT IGNORE"
                + " )".toUpperCase(Locale.ENGLISH);

        synchronized (DB_LOCK) {
            db.beginTransaction();
            db.execSQL(createGroupTableCmd);
            db.insert(GROUPS_TABLE_NAME, null, mapGroupToStorable(groupName));
            db.setTransactionSuccessful();
            db.endTransaction();
        }
    }

    public Boolean removeGroup(String groupName) {
        final SQLiteDatabase db = getWritableDatabase();
        final Boolean ret;
        final List<String> tagNames = getGroupTags(groupName);

        synchronized (DB_LOCK) {
            db.beginTransaction();
            ret = db.delete(GROUPS_TABLE_NAME, TABLE_KEY_GROUP_NAME + " = '" +
                    groupName
                            .toUpperCase(Locale.ENGLISH) + "'", null) > 0;
            db.setTransactionSuccessful();
            db.endTransaction();
        }

        if (ret) {
            for (String tagName : tagNames)
                deleteTagTableIfNotOnAnyGroup(tagName);
        }

        return ret;
    }

    public void markMessageIdAsMine(String messageId) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues x = new ContentValues();
        x.put(TABLE_KEY_MESSAGE_ID, messageId);
        synchronized (DB_LOCK) {
            db.beginTransaction();
            db.insert(MY_MESSAGES_TABLE_NAME, null, x);
            db.setTransactionSuccessful();
            db.endTransaction();
        }
    }

    public Long getLastFetchedEpoch(String tagName) {
        if (TextUtils.isEmpty(tagName))
            return -1L;
        Long ret = -1L;
        final String upperCaseTagName = tagName.toUpperCase(Locale.ENGLISH);

        final SQLiteDatabase db = getReadableDatabase();
        synchronized (DB_LOCK) {
            db.beginTransaction();
            final Cursor epochCursor = db.query(FETCHED_STAMPS_TABLE_NAME, new String[]{TABLE_KEY_EPOCH},
                    TABLE_KEY_TAG_NAME + " = ?",
                    new String[]{upperCaseTagName}, null, null,
                    null);
            if (epochCursor != null && epochCursor.moveToFirst()) {
                ret = mapStorableToTimestamp(epochCursor);
            }
            if (epochCursor != null)
                epochCursor.close();
            db.setTransactionSuccessful();
            db.endTransaction();
        }
        return ret;
    }

    public void setLastFetchedEpoch(String tagName, Long epoch) {
        if (TextUtils.isEmpty(tagName))
            return;

        final SQLiteDatabase db = getWritableDatabase();
        final String upperCaseTagName = tagName.toUpperCase(Locale.ENGLISH);
        final ContentValues cv = mapFetchedEpochToStorable(upperCaseTagName, epoch);

        synchronized (DB_LOCK) {
            db.beginTransaction();
            if (getLastFetchedEpoch(tagName) != -1) { //Means that this tag already has an entry
                db.update(FETCHED_STAMPS_TABLE_NAME, cv, TABLE_KEY_TAG_NAME + " = ?",
                        new String[]{upperCaseTagName});
            } else
                db.insert(FETCHED_STAMPS_TABLE_NAME, null, cv);
            db.setTransactionSuccessful();
            db.endTransaction();
        }
    }

    public void addMessageIdsToTag(List<String> messageIds, String tagName) {
        if (TextUtils.isEmpty(tagName)) {
            return;
        }
        final SQLiteDatabase db = getWritableDatabase();
        final String upperCaseTableName = tagName.toUpperCase(Locale.ENGLISH);

        //The tag table may or may not exist
        final String createTagTableCmd = "CREATE TABLE IF NOT EXISTS " + upperCaseTableName +
                " ( " +
                TABLE_KEY_MESSAGE_ID + " TEXT PRIMARY KEY ON CONFLICT REPLACE"
                + " )".toUpperCase(Locale.ENGLISH);

        synchronized (DB_LOCK) {
            db.beginTransaction();
            db.execSQL(createTagTableCmd);
            for (String id : messageIds)
                db.insert(upperCaseTableName, null, mapMessageIdToStorable(id));
            db.setTransactionSuccessful();
            db.endTransaction();
        }
    }
}
