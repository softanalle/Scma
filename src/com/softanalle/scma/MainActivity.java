package com.softanalle.scma;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
//import android.widget.Button;
import android.widget.ToggleButton;

public class MainActivity extends Activity {

    private static String APPTAG = "SCMA0_1";

	private ToggleButton button1_, button2_, button3_;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		button1_ = (ToggleButton) findViewById(R.id.toggleButton1);
		button2_ = (ToggleButton) findViewById(R.id.toggleButton2);
		button3_ = (ToggleButton) findViewById(R.id.toggleButton3);

		button1_.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				donClick(v);
			}

		});
		button2_.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				donClick(v);
			}
		});
		button3_.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				donClick(v);
			}

		});
	}




	private void donClick(View v) {
		// TODO Auto-generated method stub
        Log.d(APPTAG, "Clicked: " + v);
        Log.d(APPTAG, "  => " + ((ToggleButton) v).isChecked());

	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
