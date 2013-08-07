/**
 * 
 */
package com.softanalle.scma;

import android.app.Activity;
import android.content.Intent;
// import android.content.pm.ApplicationInfo;
// import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

/**
 * @author t2r
 *
 */
public class SplashActivity extends Activity {
	// Splash screen timer
	private static int SPLASH_TIME_OUT = 10000;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about_layout);

		TextView topText = (TextView) findViewById(R.id.textTop);
		topText.setText("1.0");
		
		new Handler().postDelayed(new Runnable() {

			/*
			 * Showing splash screen with a timer. This will be useful when you
			 * want to show case your app logo / company
			 */

			@Override
			public void run() {
				// This method will be executed once the timer is over
				// Start your app main activity
				Intent i = new Intent(SplashActivity.this, MainActivity.class);
				startActivity(i);

				// close this activity
				finish();
			}
		}, SPLASH_TIME_OUT);
	}
}
