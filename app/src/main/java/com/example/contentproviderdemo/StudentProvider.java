package com.example.contentproviderdemo;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;

public class StudentProvider extends ContentProvider {

    static final String PROVIDER_NAME = "com.example.contentproviderdemo.University";
    static final String URL = "content://" + PROVIDER_NAME + "/students"; // <Prefix>://<authority>/<data_type>/<id>
    static final Uri CONTENT_URI = Uri.parse(URL);

    static final String _ID = "_id";
    static final String NAME = "name";
    static final String GRADE ="grade";

    private static HashMap<String,String> STUDENTS_MAP;

    static final int STUDENTS = 1;
    static final int STUDENT_ID = 2;

    static final UriMatcher uriMatcher;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME,"students",STUDENTS);
        uriMatcher.addURI(PROVIDER_NAME,"students/#",STUDENT_ID);
    }

    private SQLiteDatabase sqLiteDatabase;
    static final String DATABASE_NAME = "University";
    static final String STUDENTS_TABLE_NAME = "students";
    static final int DATABASE_VERSION = 1;
    static final String CREATE_DB_TABLE =
            "create table "
            + STUDENTS_TABLE_NAME
            +"(_id integer primary key autoincrement, name text not null, grade text not null)";

    @Override
    public boolean onCreate() {
        Context context = getContext();
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        sqLiteDatabase = databaseHelper.getWritableDatabase();
        if(sqLiteDatabase == null){
            return false;
        }return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteQueryBuilder sqLiteQueryBuilder = new SQLiteQueryBuilder();
        sqLiteQueryBuilder.setTables(STUDENTS_TABLE_NAME);
        switch (uriMatcher.match(uri)){
            case STUDENTS:
                //// Creates a new projection map instance. The map returns a column name
                sqLiteQueryBuilder.setProjectionMap(STUDENTS_MAP);
                break;
            case STUDENT_ID:
                sqLiteQueryBuilder.appendWhere(_ID + "=" + uri.getPathSegments().get(1)); //content://com.example.contentproviderdemo.University/students/id
                break;
            default:
                throw new IllegalArgumentException("Unknow URI" + uri);
        }
        if (sortOrder == null || sortOrder == "") {
            sortOrder = NAME;
        }Cursor cursor = sqLiteQueryBuilder.query(sqLiteDatabase,projection,selection,
                selectionArgs,null,null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(),uri); // Cursor will know what ContentProvider Uri it was created for
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (uriMatcher.match(uri)){
            /**
             * Get all student records
             */
            case STUDENTS:
                return "vnd.android.cursor.dir/vnd.example.students";

            /**
             * Get a particular student
             */
            case STUDENT_ID:
                return "vnd.android.cursor.item/vnd.example.students";

            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        //Having implemented queries, a MIME type must be specified to identify the
        //data returned. The type returned should include two forms
        //Single item: vnd.android.cursor.item/vnd.<companyname>.<contenttype>
        //All items: vnd.android.cursor.dir/vnd.<companyname>.<contenttype>
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        long rowId = sqLiteDatabase.insert(STUDENTS_TABLE_NAME, "", contentValues);
        if(rowId>0){
            Uri thisUri = ContentUris.withAppendedId(CONTENT_URI,rowId);
            getContext().getContentResolver().notifyChange(thisUri,null);
            return thisUri;
        }
        throw new SQLException("Fail to insert row into " + uri);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        int count = 0;
        switch (uriMatcher.match(uri)){
            case STUDENTS:
                count = sqLiteDatabase.delete(STUDENTS_TABLE_NAME,selection,selectionArgs);
                break;
            case STUDENT_ID:
                String id = uri.getPathSegments().get(1);
                count = sqLiteDatabase.delete(STUDENTS_TABLE_NAME, _ID + "=" + id +
                        (!TextUtils.isEmpty(selection)? " AND (" + selection + ")" :""),selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknow URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri,null);
        return count;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        int count = 0;
        switch (uriMatcher.match(uri)) {
            case STUDENTS:
                count = sqLiteDatabase.update(STUDENTS_TABLE_NAME, values, selection, selectionArgs);
                break;
            case STUDENT_ID:
                count = sqLiteDatabase.update(STUDENTS_TABLE_NAME, values, _ID + "=" + uri.getPathSegments().get(1)
                        + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ")" :""),selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknow URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    private static class DatabaseHelper extends SQLiteOpenHelper{
        DatabaseHelper(Context context){
            super(context,DATABASE_NAME, null,DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            sqLiteDatabase.execSQL(CREATE_DB_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + STUDENTS_TABLE_NAME);
            onCreate(sqLiteDatabase);
        }
    }
}
