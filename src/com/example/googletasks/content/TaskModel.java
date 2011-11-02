package com.example.googletasks.content;

import java.util.Date;

import android.database.Cursor;

public class TaskModel {
	public static TaskModel parse(Cursor c) {
		TaskModel mdl = new TaskModel();
		mdl._id = c.getLong(c.getColumnIndexOrThrow("_ID"));
		mdl.name = c.getString(c.getColumnIndexOrThrow("NAME"));
		mdl.done = c.getInt(c.getColumnIndexOrThrow("DONE"))==1;
		mdl.notes = c.getString(c.getColumnIndexOrThrow("NOTES"));
		Long d = c.getLong(c.getColumnIndexOrThrow("DUE_DATE"));
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
