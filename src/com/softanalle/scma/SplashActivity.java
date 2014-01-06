/**
 * 
 */
package com.softanalle.scma;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

/**
 * @author t2r
 *
 */
public class SplashActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about_layout);
		String versionName = "n/a";
		int versionCode = 0;
		
		try {
			versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
			versionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		TextView topText = (TextView) findViewById(R.id.textTop);
		topText.setText( "version: " + versionName + " (" + versionCode + ")");
		
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
		}, MainActivity.SPLASH_TIME_OUT);
	}
}
