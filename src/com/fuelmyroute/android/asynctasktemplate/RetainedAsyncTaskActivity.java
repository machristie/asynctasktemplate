package com.fuelmyroute.android.asynctasktemplate;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class RetainedAsyncTaskActivity extends Activity {

	private static final String TEXT_VIEW_TOTAL_TEXT = "textViewTotal.text";

	private static final String PROGRESS_BAR_PROGRESS = "progressBar.progress";

	private static final String PROGRESS_BAR_VISIBILITY = "progressBar.visibility";

	private static final String TAG = RetainedAsyncTaskActivity.class.getSimpleName();

	private LoadTask loadTask = null;
	private TextView textViewTotal = null;
	private ProgressBar progressBar = null;
	private int max = 30;

	@Override
	protected void onCreate(Bundle savedInstanceState) {


		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_async_task);

		textViewTotal = (TextView) findViewById(R.id.textview_total);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);

		// AsyncTask will not call UI thread methods from the point of it being
		// retained until onCreate is called, but I'm not quite sure of the
		// timing. I'm updating the task after I've grabbed references to the UI
		// elements the task needs to update.

		// TODO: how exactly does this mechanism work, is it guaranteed?
		// Not quite sure how it works, but it does. Uncomment the following
		// lines to convince yourself. You'll see that onCreate sleeps even
		// while LoadTask is "publishing" progress updates.

		// try {
		// Log.d(TAG, "onCreate sleeping...");
		// Thread.sleep(2000);
		// Log.d(TAG, "onCreate awake");
		// } catch (InterruptedException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

		if (getLastNonConfigurationInstance() != null) {
			loadTask = (LoadTask) getLastNonConfigurationInstance();
			loadTask.updateActivity(this);
		}

		if (savedInstanceState != null) {
			textViewTotal.setText(savedInstanceState
					.getString(TEXT_VIEW_TOTAL_TEXT));
			progressBar.setVisibility(savedInstanceState
					.getInt(PROGRESS_BAR_VISIBILITY));
			progressBar.setProgress(savedInstanceState
					.getInt(PROGRESS_BAR_PROGRESS));
		}
	}

	public void onLoadClick(View loadButton) {
		
		Toast.makeText(getApplicationContext(), "Clicked!", Toast.LENGTH_SHORT)
				.show();
		loadTask = new LoadTask(this);
		// max could be based on user input, but for now is just a hard coded
		// instance variable in this activity
		loadTask.execute(max);
	}

	@Override
	public Object onRetainNonConfigurationInstance() {

		if (loadTask != null
				&& loadTask.getStatus() != AsyncTask.Status.FINISHED) {
			// Just to prove to myself that AsyncTask won't call UI thread
			// methods when activity goes away
			loadTask.updateActivity(null);
			return loadTask;
		}

		return null;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		// Save progress bar state
		outState.putInt(PROGRESS_BAR_VISIBILITY, progressBar.getVisibility());
		outState.putInt(PROGRESS_BAR_PROGRESS, progressBar.getProgress());

		// Save status text view state
		outState.putString(TEXT_VIEW_TOTAL_TEXT,
				textViewTotal.getText() != null ? textViewTotal.getText()
						.toString() : null);
	}

	@Override
	protected void onDestroy() {
		Log.d(TAG, "onDestroy");
		super.onDestroy();

		// If we have a running task and we are finishing, then go ahead and
		// cancel it
		if (isFinishing()) {
			onCancelLoad();
		}
	}

	private void onCancelLoad() {

		if (loadTask != null
				&& loadTask.getStatus() == AsyncTask.Status.RUNNING) {
			loadTask.cancel(true);
			loadTask = null;
		}
	}

	private class LoadTask extends AsyncTask<Integer, Integer, String> {

		// Important to only access this field on the UI thread or methods that
		// run in the UI thread
		private RetainedAsyncTaskActivity activity = null;
		int i = 0;
		int sum = 0;

		LoadTask(RetainedAsyncTaskActivity activity) {
			this.activity = activity;
		}

		public void updateActivity(RetainedAsyncTaskActivity activity) {
			this.activity = activity;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			activity.textViewTotal.setText("Starting...");
			activity.progressBar.setVisibility(View.VISIBLE);
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);

			activity.textViewTotal.setText(result);
			activity.progressBar.setVisibility(View.GONE);
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			int runningTotal = values[0];
			int iteration = values[1];
			int maxIterations = values[2];
			activity.textViewTotal.setText("Current total: " + runningTotal);
			activity.progressBar.setProgress(Math.round(iteration * 100
					/ maxIterations));
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			activity.textViewTotal.setText("Cancelled!");
			Log.d(TAG, "Cancelled the task!");
			activity.progressBar.setVisibility(View.GONE);
		}

		@Override
		protected String doInBackground(Integer... params) {

			Log.d(TAG, "doInBackground");
			int max = params[0];

			while (i < max) {

				i++;
				sum += i;
				Log.d(TAG, "publishing progress: " + sum);
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
