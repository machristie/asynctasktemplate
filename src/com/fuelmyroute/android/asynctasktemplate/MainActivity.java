package com.fuelmyroute.android.asynctasktemplate;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	public void startRestartingAsyncActivity(View button) {
		startActivity(new Intent(this, AsyncTaskActivity.class));
	}

	public void startRetainedAsyncActivity(View button) {
		startActivity(new Intent(this, RetainedAsyncTaskActivity.class));
	}
}
