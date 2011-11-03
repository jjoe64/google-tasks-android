package com.example.googletasks.content;

import java.util.Date;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class TaskModel {
	public static final String TABLE_NAME = "tasks";
	public static final String COLUMN__ID = "_id";
	public static final String COLUMN_NAME = "NAME";
	public static final String COLUMN_DONE = "DONE";
	public static final String COLUMN_NOTES = "NOTES";
	public static final String COLUMN_DUE_DATE = "DUE_DATE";

	public static void createTable(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + TABLE_NAME + " ("
				+ COLUMN__ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ COLUMN_NAME + " VARCHAR(255),"
				+ COLUMN_DONE + " INTEGER,"
				+ COLUMN_NOTES + " LONGTEXT,"
				+ COLUMN_DUE_DATE + " LONG"
				+ ");");
	}

	public static void dropTable(SQLiteDatabase db) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
	}

	public static TaskModel parse(Cursor c) {
		TaskModel mdl = new TaskModel();
		mdl._id = c.getLong(c.getColumnIndexOrThrow(COLUMN__ID));
		mdl.name = c.getString(c.getColumnIndexOrThrow(COLUMN_NAME));
		mdl.done = c.getInt(c.getColumnIndexOrThrow(COLUMN_DONE))==1;
		mdl.notes = c.getString(c.getColumnIndexOrThrow(COLUMN_NOTES));
		Long d = c.getLong(c.getColumnIndexOrThrow(COLUMN_DUE_DATE));
		if (d != null && d != 0) {
			mdl.dueDate = new Date(d);
		}
		return mdl;
	}

	private long _id;
	private String name;
	private boolean done;
	private String notes;
	private Date dueDate;

	public Date getDueDate() {
		return dueDate;
	}

	public long getId() {
		return _id;
	}

	public String getName() {
		return name;
	}

	public String getNotes() {
		return notes;
	}

	public boolean isDone() {
		return done;
	}
}
