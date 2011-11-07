package com.example.googletasks.activities;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.EditText;

import com.example.googletasks.R;
import com.example.googletasks.content.TaskModel;
import com.example.googletasks.content.TasksContentProvider;

public class EditTaskActivity extends Activity {
	private CheckBox done;
	private EditText name;
	private EditText notes;
	private CheckBox cbDueDate;
	private DatePicker dueDate;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_task);

		done = (CheckBox) findViewById(R.id.edit_task_done);
		name = (EditText) findViewById(R.id.edit_task_name);
		notes = (EditText) findViewById(R.id.edit_task_notes);
		cbDueDate = (CheckBox) findViewById(R.id.edit_task_cb_due_date);
		dueDate = (DatePicker) findViewById(R.id.edit_task_due_date);

		dueDate.setVisibility(View.GONE);
		cbDueDate.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				dueDate.setVisibility(isChecked?View.VISIBLE:View.GONE);
			}
		});

		Button save = (Button) findViewById(R.id.edit_task_save);
		save.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				save();
			}
		});

		// edit
		if (getIntent().getData() != null) {
			Cursor cursor = getContentResolver().query(getIntent().getData(), null, null, null, null);
			if (cursor.moveToFirst()) {
				TaskModel mdl = TaskModel.parse(cursor);
				done.setChecked(mdl.isDone());
				name.setText(mdl.getName());
				notes.setText(mdl.getNotes());
				//dueDate.setText(mdl.getDueDate()); // TODO format
			}
		}
	}

	private void save() {
		ContentValues values = new ContentValues(4);
		values.put(TaskModel.COLUMN_DONE, done.isChecked());
		values.put(TaskModel.COLUMN_NAME, name.getText().toString());
		values.put(TaskModel.COLUMN_NOTES, notes.getText().toString());
		if (cbDueDate.isChecked()) {
			values.put(TaskModel.COLUMN_DUE_DATE, dueDate.getYear()+"-"+dueDate.getMonth()+"-"+dueDate.getDayOfMonth());
		}

		if (getIntent().getData() != null) {
			getContentResolver().update(getIntent().getData(), values, null, null);
		} else {
			getContentResolver().insert(TasksContentProvider.CONTENT_URI, values);
		}
		finish();
	}
}
