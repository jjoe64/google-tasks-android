package com.example.googletasks.content;

import java.util.Date;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

public class TasksContentProvider extends ContentProvider {
	public static final Uri CONTENT_URI = Uri.parse("content://com.example.googletasks.contentprovider");

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean onCreate() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		MatrixCursor cursor = new MatrixCursor(new String[] {
				"_ID", "NAME", "DONE", "DUE_DATE", "NOTES"
		}, 2);
		cursor.addRow(new Object[] {
				0, "Auto waschen", 0, null, "maximal 10 €"
		});
		cursor.addRow(new Object[] {
				1, "Postbank kündigen", 1, new Date().getTime(), null
		});
		return cursor;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

}
