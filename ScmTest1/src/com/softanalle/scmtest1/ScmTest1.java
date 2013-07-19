package com.softanalle.scmtest1;

/**
 * @author Tommi Rintala <t2r@iki.fi>
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import ioio.lib.api.AnalogInput;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.IOIO;
import ioio.lib.api.PwmOutput;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;
import android.app.admin.DevicePolicyManager;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;


import com.softanalle.scmctrls.LedIndicator;
//import com.softanalle.previewtest.Preview;
import com.softanalle.scmtest1.R;


// camera stuff
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;

import android.app.admin.DevicePolicyManager;

public class ScmTest1 extends IOIOActivity {

	private ToggleButton toggleButton_;
	private LedIndicator ledIndicator_;
	private Button pictureButton_;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);


		pictureButton_  = (Button) findViewById(R.id.pictureButton);
		toggleButton_ = (ToggleButton) findViewById(R.id.ToggleButton);

		ledIndicator_ = (LedIndicator) findViewById(R.id.ledIndicator1);

		// camera stuff
		mPreview = new Preview(this);
		FrameLayout tmp = (FrameLayout) findViewById(R.id.RootFrame);
		tmp.addView(mPreview);

		ledIndicator_.bringToFront();
		toggleButton_.bringToFront();
		pictureButton_.bringToFront();

		//buttonClick = (Button) findViewById(R.id.button1);

		pictureButton_.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if ( powerState_ && mPreview.isReady() ) {
					powerLedsOn();
					mPreview.camera.takePicture(shutterCallback, rawCallback, jpegCallback);
					powerLedsOff();
				}
			}
		});


		toggleButton_.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// mPreview.camera.takePicture(shutterCallback, rawCallback, jpegCallback);
				if ( ((ToggleButton) v).isChecked()) {
					powerState_ = true;
					pictureButton_.setEnabled(true);
				} else {
					powerState_ = false;
					pictureButton_.setEnabled(false);
				}						
			}
		});

		pictureButton_.setEnabled(false);
		
		Log.d(TAG, "onCreate'd");

		mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
		Log.d(TAG, "  getCameraDisabled(): " + mDPM.getCameraDisabled(null));


		enableUi(false);
	}

	private boolean powerState_ = false;
	
	private void powerLedsOn() {
		pulseWidth1_ = 1000;
		pulseWidth2_ = 1000;
		pulseWidth3_ = 1000;
		
		ledIndicator_.turnOn(0);
		ledIndicator_.turnOn(1);
		ledIndicator_.turnOn(2);
	}

	private void powerLedsOff() {
		pulseWidth1_ = 0;
		pulseWidth2_ = 0;
		pulseWidth3_ = 0;
		
		ledIndicator_.turnOff(0);
		ledIndicator_.turnOff(1);
		ledIndicator_.turnOff(2);		
	}

	private int pulseWidth1_, pulseWidth2_, pulseWidth3_;

	class Looper extends BaseIOIOLooper {
		// private AnalogInput input_;
		private PwmOutput pwmOutput1_;
		private PwmOutput pwmOutput2_;
		private PwmOutput pwmOutput3_;
		private DigitalOutput led_;
		private DigitalOutput powout_;

		@Override
		public void setup() throws ConnectionLostException {
			led_ = ioio_.openDigitalOutput(IOIO.LED_PIN, true);
			powout_ = ioio_.openDigitalOutput(6, true);
			//input_ = ioio_.openAnalogInput(40);

			pwmOutput1_ = ioio_.openPwmOutput(10, 100);
			pwmOutput2_ = ioio_.openPwmOutput(11, 100);
			pwmOutput3_ = ioio_.openPwmOutput(12, 100);

			pwmOutput1_.setPulseWidth( 0 );
			pwmOutput2_.setPulseWidth( 0 );
			pwmOutput3_.setPulseWidth( 0 );

			powout_.write( false );

			try {
				File saveDir = new File(String.format("%s/SCM", Environment.getExternalStorageDirectory().getPath()));
				if ( ! saveDir.isDirectory()) {
					saveDir.mkdir();
				}
			} catch (Exception e) {
				Log.d(TAG, "Error in creation of savedir");
			}
			enableUi(true);
		}

		@Override
		public void loop() throws ConnectionLostException, InterruptedException {

			visualize( pulseWidth1_, pulseWidth2_, pulseWidth3_ );
			
			//setNumber( 1, pulseWidth1_ );
			//setNumber( 2, pulseWidth2_ );
			//setNumber( 3, pulseWidth3_ );
			
			powout_.write( powerState_ );
			
			pwmOutput1_.setPulseWidth( pulseWidth1_ );
			pwmOutput2_.setPulseWidth( pulseWidth2_ );
			pwmOutput3_.setPulseWidth( pulseWidth3_ );

			led_.write( !powerState_ );
			
			
			 
			//ledIndicator_.setPowerState( powerState_ );
			Thread.sleep(10);
		}

		@Override
		public void disconnected() {
			enableUi(false);
		}
	}

	@Override
	protected IOIOLooper createIOIOLooper() {
		return new Looper();
	}

	private void enableUi(final boolean enable) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				/*
				seekBar1_.setEnabled(enable);
				seekBar2_.setEnabled(enable);
				seekBar3_.setEnabled(enable);
				 */

				//toggleButton_.setEnabled(enable);
				//mPreview.setEnabled(enable);

			}
		});
	}

	private void setNumber(int channel, float f) {
		final String str = String.format("%.2f", f);
		final int ch = channel;
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				/*
				switch(ch) {
				case 1:
					textView1_.setText(str);
					break;
				case 2:
					textView2_.setText(str);
					break;
				case 3:
					textView3_.setText(str);
					break;
				default:
					// no action
				}
				 */
				ledIndicator_.setPowerState( powerState_ );				
			}
		});
	}
	
	private void visualize(int pw1, int pw2, int pw3) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				ledIndicator_.setPowerState(powerState_);
			}
		});
	}

	// --------------------------------------------------------------------------------
	// camera stuff



	private static final String TAG = "ScmTest1";
	Camera mCamera;
	Preview mPreview;
	Button buttonClick;

	DevicePolicyManager mDPM;


	ShutterCallback shutterCallback = new ShutterCallback() {
		public void onShutter() {
			Log.d(TAG, "onShutter");
		}
	};

	PictureCallback rawCallback = new PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {
			Log.d(TAG, "onPictureTaken() - raw");
			/*
			FileOutputStream outStream = null;
			try {
				outStream = new FileOutputStream(String.format("%s/SCM/%d.raw",
						Environment.getExternalStorageDirectory().getPath(),
						System.currentTimeMillis()));
				outStream.write(data);
				outStream.close();
				Log.d(TAG, "onRawPictureTaken - wrote bytes: " + data.length);
				Toast.makeText(getApplicationContext(), "RAW - wrote bytes: " + data.length, Toast.LENGTH_LONG).show();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
			}
			Log.d(TAG, "onPictureTaken - raw");
			*/
		}
	};

	PictureCallback jpegCallback = new PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {
			FileOutputStream outStream = null;
			try {
				outStream = new FileOutputStream(String.format("%s/SCM/%d.jpg",
						Environment.getExternalStorageDirectory().getPath(),
						System.currentTimeMillis()));
				outStream.write(data);
				outStream.close();
				Log.d(TAG, "onPictureTaken - wrote bytes: " + data.length);
				Toast.makeText(getApplicationContext(), "JPEG - wrote bytes: " + data.length, Toast.LENGTH_LONG).show();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
			}
			Log.d(TAG, "onPictureTaken - jpeg");
		}
	};


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.options_menu, menu);
		return true;
	}
	
	@Override public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_config:
			Toast.makeText(getApplicationContext(), "Should show config menu", Toast.LENGTH_LONG).show();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}