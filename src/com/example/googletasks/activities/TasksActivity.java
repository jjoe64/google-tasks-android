package com.example.googletasks.activities;

import android.app.ListActivity;
import android.content.AsyncQueryHandler;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import com.example.googletasks.R;
import com.example.googletasks.content.TaskModel;
import com.example.googletasks.content.TasksContentProvider;

public class TasksActivity extends ListActivity {
	private CursorAdapter cursorAdapter;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tasks);

		// setup list adapter
		cursorAdapter = new ResourceCursorAdapter(getApplicationContext(), R.layout.list_tasks_item, null) {
			@Override
			public void bindView(View view, Context context, Cursor cursor) {
				CheckBox name = (CheckBox) view.findViewById(R.id.list_tasks_item_name);
				TextView notes = (TextView) view.findViewById(R.id.list_tasks_item_notes);
				TextView dueDate = (TextView) view.findViewById(R.id.list_tasks_item_due_date);

				TaskModel mdl = TaskModel.parse(cursor);
				name.setChecked(mdl.isDone());
				name.setText(mdl.getName());
				notes.setText(mdl.getNotes());
				dueDate.setText(mdl.getDueDate()); // TODO format
			}
		};
		setListAdapter(cursorAdapter);

		// load cursor
		AsyncQueryHandler query = new AsyncQueryHandler(getContentResolver()) {
			@Override
			protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
				cursorAdapter.changeCursor(cursor);
				startManagingCursor(cursor);
			}
		};
		query.startQuery(0, 0, TasksContentProvider.CONTENT_URI, null, null, null, null);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.tasks_menu, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		if (item.getItemId() == R.id.menuitem_new) {
			Intent intent = new Intent(getApplicationContext(), EditTaskActivity.class);
			startActivity(intent);
			return true;
		} else {
			return super.onMenuItemSelected(featureId, item);
		}
	}
}