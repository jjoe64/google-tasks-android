package com.example.googletasks.activities;

import android.app.Activity;
import android.content.ContentValues;
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
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_task);

		final CheckBox done = (CheckBox) findViewById(R.id.edit_task_done);
		final EditText name = (EditText) findViewById(R.id.edit_task_name);
		final EditText notes = (EditText) findViewById(R.id.edit_task_notes);
		final CheckBox cbDueDate = (CheckBox) findViewById(R.id.edit_task_cb_due_date);
		final DatePicker dueDate = (DatePicker) findViewById(R.id.edit_task_due_date);

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
				ContentValues values = new ContentValues(4);
				values.put(TaskModel.COLUMN_DONE, done.isChecked());
				values.put(TaskModel.COLUMN_NAME, name.getText().toString());
				values.put(TaskModel.COLUMN_NOTES, notes.getText().toString());
				if (cbDueDate.isChecked()) {
					values.put(TaskModel.COLUMN_DUE_DATE, dueDate.getYear()+"-"+dueDate.getMonth()+"-"+dueDate.getDayOfMonth());
				}

				getContentResolver().insert(TasksContentProvider.CONTENT_URI, values);
				finish();
			}
		});
	}
}
