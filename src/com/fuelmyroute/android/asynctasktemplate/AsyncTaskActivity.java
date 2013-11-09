package com.fuelmyroute.android.asynctasktemplate;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class AsyncTaskActivity extends Activity {

	private static final String TAG = AsyncTaskActivity.class.getSimpleName();

	private static final String STATE_LOAD_IN_PROGRESS = "state.load.in_progress";

	private static final String STATE_LOAD_MAX = "state.load.max";

	private LoadTask loadTask = null;
	private TextView textViewTotal = null;
	private ProgressBar progressBar = null;
	private int max = 10;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_async_task);

		textViewTotal = (TextView) findViewById(R.id.textview_total);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_async_task, menu);
		return true;
	}

	public void onLoadClick(View loadButton) {
		
		Toast.makeText(getApplicationContext(), "Clicked!", Toast.LENGTH_SHORT)
				.show();
		loadTask = new LoadTask();
		// max could be based on user input, but for now is just a hard coded
		// instance variable in this activity
		loadTask.execute(max);
	}

	@Override
	protected void onDestroy() {
		Log.d(TAG, "onDestroy");
		super.onDestroy();

		// We need to cancel the task even in onDestroy because
		// onSaveInstanceState is only called when an activity instance may need
		// to be restored
		onCancelLoad();
	}

	private boolean onCancelLoad() {

		if (loadTask != null
				&& loadTask.getStatus() == AsyncTask.Status.RUNNING) {
			// NOTE: if you need to dismiss dialogs, then you will need to do that in 
			// the Activity, on the UI thread, before calling AsyncTask.cancel
			loadTask.cancel(true);
			loadTask = null;
			return true;
		}
		return false;
	}

	// TODO: why not just do this in onCreate?
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {

		Log.d(TAG, "onRestoreInstanceState");
		super.onRestoreInstanceState(savedInstanceState);

		restoreLoadTask(savedInstanceState);
	}

	private void restoreLoadTask(Bundle savedInstanceState) {

		Log.d(TAG, "onRestoreInstanceState, " + STATE_LOAD_IN_PROGRESS + ": " + savedInstanceState.getBoolean(STATE_LOAD_IN_PROGRESS));
		Log.d(TAG, "onRestoreInstanceState, " + STATE_LOAD_MAX + ": " + savedInstanceState.getInt(STATE_LOAD_MAX));
		if (savedInstanceState.getBoolean(STATE_LOAD_IN_PROGRESS)) {
			max = savedInstanceState.getInt(STATE_LOAD_MAX);
			loadTask = new LoadTask();
			loadTask.execute(max);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		Log.d(TAG, "onSaveInstanceState: isFinishing=" + isFinishing());
		super.onSaveInstanceState(outState);

		// TODO: maybe we don't need to do this if isFinishing?
		saveLoadTask(outState);
	}

	private void saveLoadTask(Bundle outState) {

		if (onCancelLoad()){
			
			if (max != 0) {
				outState.putBoolean(STATE_LOAD_IN_PROGRESS, true);
				outState.putInt(STATE_LOAD_MAX, max);
			}
		}
	}


	private class LoadTask extends AsyncTask<Integer, Integer, String> {

		int i = 0;
		int sum = 0;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			textViewTotal.setText("Starting...");
			progressBar.setVisibility(View.VISIBLE);
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);

			textViewTotal.setText(result);
			progressBar.setVisibility(View.GONE);
		}

		// first value is the running total, the second value is the iteration
		// third value is the max
		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			int runningTotal = values[0];
			int iteration = values[1];
			int maxIterations = values[2];
			textViewTotal.setText("Current total: " + runningTotal);
			progressBar.setProgress(Math.round(iteration * 100 / maxIterations));
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			textViewTotal.setText("Cancelled!");
			Log.d(TAG, "Cancelled the task!");
			progressBar.setVisibility(View.GONE);
		}

		@Override
		protected String doInBackground(Integer... params) {

			Log.d(TAG, "doInBackground");
			int max = params[0];

			while (i < max) {

				i++;
				sum += i;
				publishProgress(sum, i, max);

				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					Log.d(TAG, "interrupted!", e);
				}
			}

			return "The sum is: " + sum;
		}

	}
}
