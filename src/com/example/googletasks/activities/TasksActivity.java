package com.example.googletasks.activities;

import android.app.ListActivity;
import android.content.AsyncQueryHandler;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.example.googletasks.R;
import com.example.googletasks.content.TasksContentProvider;

public class TasksActivity extends ListActivity {
	private CursorAdapter cursorAdapter;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tasks);

		// setup list
		ListView list = getListView();
		list.setAdapter(cursorAdapter = new SimpleCursorAdapter(getApplicationContext(), R.layout.list_tasks_item, null, new String[] {
			"NAME"
			, "NOTES"
			, "DUE_DATE"
		}, new int[] {
			R.id.list_tasks_item_name
			, R.id.list_tasks_item_notes
			, R.id.list_tasks_item_due_date
		}));

		// load cursor
		AsyncQueryHandler query = new AsyncQueryHandler(getContentResolver()) {
			@Override
			protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
				cursorAdapter.changeCursor(cursor);
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
			startActivity(intent); // TODO startActivityForResult
			return true;
		} else {
			return super.onMenuItemSelected(featureId, item);
		}
	}
}