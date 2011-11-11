package com.example.googletasks.content;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

public class TasksContentProvider extends ContentProvider {
	private static class DatabaseHelper extends SQLiteOpenHelper {
		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			TaskModel.createTable(db);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			TaskModel.dropTable(db);
			onCreate(db);
		}
	}

	private static final String DATABASE_NAME = "tasks.db";
	private static final int DATABASE_VERSION = 4;

	public static final Uri CONTENT_URI = Uri.parse("content://com.example.googletasks.contentprovider");

	private DatabaseHelper dbHelper;

	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
		String id = uri.getLastPathSegment();
		if (id != null) {
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			int count = db.delete(TaskModel.TABLE_NAME, TaskModel.COLUMN__ID+"=?", new String[] {id});
			getContext().getContentResolver().notifyChange(uri, null);
			return count;
		}
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		long id = dbHelper.getWritableDatabase().insert(TaskModel.TABLE_NAME, null, values);
		getContext().getContentResolver().notifyChange(uri, null);
		return Uri.withAppendedPath(CONTENT_URI, String.valueOf(id));
	}

	@Override
	public boolean onCreate() {
		dbHelper = new DatabaseHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		if (uri.getLastPathSegment() != null) {
			String id = uri.getLastPathSegment();
			selection = TaskModel.COLUMN__ID+"=?";
			selectionArgs = new String[] {id};
		}
		return dbHelper.getReadableDatabase().query(
				TaskModel.TABLE_NAME
				, null // columns
				, selection
				, selectionArgs
				, null // groupBy
				, null // having
				, null // orderBy
		);
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		String id = uri.getLastPathSegment();
		if (id != null) {
			return dbHelper.getWritableDatabase().update(TaskModel.TABLE_NAME, values, TaskModel.COLUMN__ID+"=?", new String[] {id});
		}
		return 0;
	}

}
