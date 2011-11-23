package com.example.googletasks.activities;

import android.accounts.Account;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.AsyncQueryHandler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckedTextView;
import android.widget.CursorAdapter;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import com.example.googletasks.R;
import com.example.googletasks.content.TaskModel;
import com.example.googletasks.content.TasksContentProvider;
import com.example.googletasks.services.GoogleTasksSyncService;
import com.example.googletasks.services.GoogleTasksSyncService.LocalBinder;

public class TasksActivity extends ListActivity {
	private CursorAdapter cursorAdapter;

	private GoogleTasksSyncService service;
	private boolean serviceBound = false;

	private static final int DIALOG_ACCOUNTS = 0;
	public static final int REQUEST_AUTHENTICATE = 0;

	private final ServiceConnection mConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			// We've bound to LocalService, cast the IBinder and get LocalService instance
			LocalBinder binder = (LocalBinder) service;
			TasksActivity.this.service = binder.getService();
			serviceBound = true;

			// google auth
			if (TasksActivity.this.service.gotAccount(false)) {
				showDialog(DIALOG_ACCOUNTS);
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			serviceBound = false;
		}
	};

	private final BroadcastReceiver serviceReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(GoogleTasksSyncService.BROADCAST_ACTION_START_INTENT)) {
				// start intent for result
				Intent startIntent = (Intent) intent.getParcelableExtra("startIntent");
				startActivityForResult(startIntent, TasksActivity.REQUEST_AUTHENTICATE);
			}
		}
	};

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
			case REQUEST_AUTHENTICATE:
				if (resultCode == RESULT_OK) {
					// google auth
					if (TasksActivity.this.service.gotAccount(false)) {
						showDialog(DIALOG_ACCOUNTS);
					}
				} else {
					showDialog(DIALOG_ACCOUNTS);
				}
				break;
		}
	}

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
	protected Dialog onCreateDialog(int id) {
		switch (id) {
			case DIALOG_ACCOUNTS:
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle("Select a Google account");

				final Account[] accounts = service.getAccounts();
				final int size = accounts.length;
				String[] names = new String[size];
				for (int i = 0; i < size; i++) {
					names[i] = accounts[i].name;
				}
				builder.setItems(names, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						service.gotAccount(accounts[which]);
					}
				});
				return builder.create();
		}
		return null;
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

	@Override
	protected void onPause() {
		super.onPause();

		unregisterReceiver(serviceReceiver);
	}

	@Override
	protected void onResume() {
		super.onResume();

		registerReceiver(
			serviceReceiver , new IntentFilter(GoogleTasksSyncService.BROADCAST_ACTION_START_INTENT)
		);
	}

	@Override
	protected void onStart() {
		super.onStart();

		Intent intent = new Intent(this, GoogleTasksSyncService.class);
		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onStop() {
		super.onStop();
		// Unbind from the service
		if (serviceBound) {
			unbindService(mConnection);
			serviceBound = false;
		}
	}
}