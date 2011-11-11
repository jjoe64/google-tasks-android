package com.example.googletasks.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.example.googletasks.ClientCredentials;
import com.example.googletasks.content.TaskModel;
import com.example.googletasks.content.TasksContentProvider;
import com.google.api.client.extensions.android2.AndroidHttp;
import com.google.api.client.googleapis.auth.oauth2.draft10.GoogleAccessProtectedResource;
import com.google.api.client.googleapis.extensions.android2.auth.GoogleAccountManager;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.json.JsonHttpRequest;
import com.google.api.client.http.json.JsonHttpRequestInitializer;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.tasks.Tasks;
import com.google.api.services.tasks.TasksRequest;
import com.google.api.services.tasks.model.Task;

public class GoogleTasksSyncService extends Service {
	public class LocalBinder extends Binder {
		public GoogleTasksSyncService getService() {
			// Return this instance of LocalService so clients can call public methods
			return GoogleTasksSyncService.this;
		}
	}

	private final IBinder mBinder = new LocalBinder();
	private Tasks service;
	private boolean authToken;
	private final HttpTransport transport = AndroidHttp.newCompatibleTransport();

	private final GoogleAccessProtectedResource accessProtectedResource = new GoogleAccessProtectedResource(null);

	// This must be the exact string, and is a special for alias OAuth 2 scope
	// "https://www.googleapis.com/auth/tasks"
	private static final String AUTH_TOKEN_TYPE = "Manage your tasks";
	private static final String PREF = "MyPrefs";

	public static final String BROADCAST_ACTION_START_INTENT = "com.example.googletasks.action.START_INTENT";

	private GoogleAccountManager accountManager;

	private void createOrUpdateTask(Task task) {
		Cursor c = getContentResolver().query(
				TasksContentProvider.CONTENT_URI
				, null // projection
				, TaskModel.COLUMN_NAME+"=?"
				, new String[] {task.getTitle()}
				, null // sortOrder
		);

		ContentValues values = new ContentValues(5);
		values.put(TaskModel.COLUMN_DONE, task.getStatus().equals("completed"));
		values.put(TaskModel.COLUMN_NAME, task.getTitle());
		values.put(TaskModel.COLUMN_NOTES, task.getNotes());
		values.put(TaskModel.COLUMN_DUE_DATE, ""); //TODO task.getDue()
		values.put(TaskModel.COLUMN_MARK_FOR_SYNC, 0);

		if (c.moveToFirst()) {
			// update
			TaskModel mdl = TaskModel.parse(c);
			if (!mdl.isMarkForSync()) {
				Uri uri = Uri.withAppendedPath(TasksContentProvider.CONTENT_URI, String.valueOf(mdl.getId()));
				getContentResolver().update(uri, values, null, null);
			}
			// TODO sync
		} else {
			// create
			getContentResolver().insert(TasksContentProvider.CONTENT_URI, values);
		}
	}

	public Account[] getAccounts() {
		return accountManager.getAccounts();
	}

	public void gotAccount(final Account account) {
		SharedPreferences settings = getSharedPreferences(PREF, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("accountName", account.name);
		editor.commit();
		accountManager.manager.getAuthToken(account, AUTH_TOKEN_TYPE, true,
				new AccountManagerCallback<Bundle>() {
					@Override
					public void run(AccountManagerFuture<Bundle> future) {
						try {
							Bundle bundle = future.getResult();
							if (bundle.containsKey(AccountManager.KEY_INTENT)) {
								Intent startIntent = bundle.getParcelable(AccountManager.KEY_INTENT);
								startIntent.setFlags(startIntent.getFlags() & ~Intent.FLAG_ACTIVITY_NEW_TASK);

								Intent broadcast = new Intent(BROADCAST_ACTION_START_INTENT);
								broadcast.putExtra("startIntent", startIntent);
								sendBroadcast(broadcast);
							} else if (bundle.containsKey(AccountManager.KEY_AUTHTOKEN)) {
								accessProtectedResource.setAccessToken(bundle.getString(AccountManager.KEY_AUTHTOKEN));
								onAuthToken();
							}
						} catch (Exception e) {
							handleException(e);
						}
					}
			}, null);
	}

	public boolean gotAccount(boolean tokenExpired) {
		SharedPreferences settings = getSharedPreferences(PREF, 0);
		String accountName = settings.getString("accountName", null);
		Account account = accountManager.getAccountByName(accountName);
		if (account != null) {
			if (tokenExpired) {
				accountManager.invalidateAuthToken(accessProtectedResource.getAccessToken());
				accessProtectedResource.setAccessToken(null);
			}
			gotAccount(account);
			return false;
		}
		return true;
	}

	private void handleException(Exception e) {
		e.printStackTrace();
		if (e instanceof HttpResponseException) {
			HttpResponse response = ((HttpResponseException) e).getResponse();
			int statusCode = response.getStatusCode();
			try {
				response.ignore();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			if (statusCode == 401) {
				gotAccount(true);
				return;
			}
		}
		Log.e("TasksActivity", e.getMessage(), e);
	}

	private void onAuthToken() {
		authToken = true;
		sync();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		// google tasks api
		service = Tasks.builder(transport, new JacksonFactory()).setApplicationName("GoogleTasks/1.0")
			.setHttpRequestInitializer(accessProtectedResource)
			.setJsonHttpRequestInitializer(new JsonHttpRequestInitializer() {
				@Override
				public void initialize(JsonHttpRequest request) throws IOException {
					TasksRequest tasksRequest = (TasksRequest) request;
					tasksRequest.setKey(ClientCredentials.API_KEY);
				}
			}).build();

		accountManager = new GoogleAccountManager(this);
		Logger.getLogger("com.google.api.client").setLevel(Level.OFF);
		gotAccount(false);
	}

	public void sync() {
		if (authToken) {
			try {
				List<String> taskTitles = new ArrayList<String>();
				List<Task> tasks = service.tasks().list("@default").execute().getItems();
				if (tasks != null) {
					for (Task task : tasks) {
						createOrUpdateTask(task);
					}
				} else {
					taskTitles.add("No tasks.");
				}
			} catch (IOException e) {
				handleException(e);
			}
		}
	}
}
