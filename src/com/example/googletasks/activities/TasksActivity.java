package com.example.googletasks.activities;

import android.app.ListActivity;
import android.content.AsyncQueryHandler;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckedTextView;
import android.widget.CursorAdapter;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import com.example.googletasks.R;
import com.example.googletasks.content.TaskModel;
import com.example.googletasks.content.TasksContentProvider;

public class TasksActivity extends ListActivity {
	private CursorAdapter cursorAdapter;

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info;
		try {
			info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		} catch (ClassCastException e) {
			return false;
		}
		if (item.getItemId() == R.id.menu_item_delete_task) {
			Uri url = Uri.withAppendedPath(TasksContentProvider.CONTENT_URI, "/"+info.id);
			getContentResolver().delete(url, null, null);
			cursorAdapter.getCursor().requery();
			return true;
		} else if (item.getItemId() == R.id.menu_item_edit_task) {
			Intent intent = new Intent(getApplicationContext(), EditTaskActivity.class);
			Uri url = Uri.withAppendedPath(TasksContentProvider.CONTENT_URI, "/"+info.id);
			intent.setData(url);
			startActivity(intent);
			return true;
		} else {
			return super.onContextItemSelected(item);
		}
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
        .detectAll()
        .penaltyLog()
        .penaltyDeath()
        .build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
        .detectAll()
        .penaltyLog()
        .penaltyDeath()
        .build());

		setContentView(R.layout.tasks);

		// setup list adapter
		cursorAdapter = new ResourceCursorAdapter(getApplicationContext(), R.layout.list_tasks_item, null, true) {
			@Override
			public void bindView(View view, Context context, Cursor cursor) {
				CheckedTextView name = (CheckedTextView) view.findViewById(android.R.id.text1);
				TextView notes = (TextView) view.findViewById(R.id.list_tasks_item_notes);
				TextView dueDate = (TextView) view.findViewById(R.id.list_tasks_item_due_date);

				TaskModel mdl = TaskModel.parse(cursor);
				name.setChecked(mdl.isDone());
				name.setText(mdl.getName());
				notes.setText(mdl.getNotes());
				dueDate.setText(mdl.getDueDate()); // TODO format

				if (mdl.isOverdue()) {
					view.setBackgroundColor(Color.RED);
				} else {
					view.setBackgroundColor(Color.TRANSPARENT);
				}
			}
		};
		setListAdapter(cursorAdapter);

		//
		getListView().setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Uri url = Uri.withAppendedPath(TasksContentProvider.CONTENT_URI, "/"+id);
				ContentValues values = new ContentValues(1);
				CheckedTextView ch = (CheckedTextView) view.findViewById(android.R.id.text1);
				ch.toggle();
				values.put(TaskModel.COLUMN_DONE, ch.isChecked());
				getContentResolver().update(url, values, null, null);
			}
		});

		// register options menu
		registerForContextMenu(getListView());

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
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		if (v == getListView()) {
			MenuInflater inf = getMenuInflater();
			inf.inflate(R.menu.task_options_menu, menu);
		} else {
			super.onCreateContextMenu(menu, v, menuInfo);
		}
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