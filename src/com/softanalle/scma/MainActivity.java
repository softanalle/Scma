package com.softanalle.scma;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

//import ioio.lib.api.AnalogInput;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.IOIO;
import ioio.lib.api.PwmOutput;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;
import android.annotation.TargetApi;
import android.app.admin.DevicePolicyManager;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
//import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Button;
import android.widget.RelativeLayout;
//import android.widget.SeekBar;
//import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;


//import com.softanalle.scmctrls.LedIndicator;
//import com.softanalle.previewtest.Preview;
import com.softanalle.scma.R;


// camera stuff
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.Bundle;
import android.os.Environment;
//import android.preference.Preference;
//import android.preference.PreferenceFragment;
//import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.Context;
import android.content.SyncResult;
//import android.content.SharedPreferences;
//import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;

import android.app.admin.DevicePolicyManager;


public class MainActivity extends IOIOActivity 
/*implements
OnSharedPreferenceChangeListener 
 */
{
	protected final String FILEMODE_JPG = "jpg";
	protected final String FILEMODE_RAW = "raw";

	volatile static boolean mShutdown = false;
	private String mCurrentFileMode = FILEMODE_JPG;

	protected String mImageSuffix = "focus";

	private static final int defaultPulseWidth = 500; // PWM pulse width
	private static final int mLedFlashTime = 1000; // milliseconds the flash leds are on
	private static final int mLedCount = 6; // how many leds actually there are!

	private int mPulseWidth[];
	
	private static final String SETTINGS_CHANNEL0 = "CONF_CHANNEL1_PWM";
	private static final String SETTINGS_CHANNEL1 = "CONF_CHANNEL2_PWM";
	private static final String SETTINGS_CHANNEL2 = "CONF_CHANNEL3_PWM";
	//private static final String SETTINGS_CHANNEL3 = "CONF_CHANNEL4_PWM";
	private static final String SETTINGS_CHANNEL_FOCUS = "CONF_CHANNEL_FOCUS_PWM";

	private ToggleButton toggleButton_;
	private LedIndicator ledIndicator_;
	private Button pictureButton_;
	private int pulseWidthFocus_;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		pictureButton_  = (Button) findViewById(R.id.pictureButton);
		toggleButton_ = (ToggleButton) findViewById(R.id.powerButton);
		ledIndicator_ = (LedIndicator) findViewById(R.id.ledIndicator1);

		toggleButton_.setEnabled(false);
		
		// camera stuff
		mPreview = new Preview(this);
		//mPreview = new Preview(getApplicationContext());
		Log.d(TAG, "Preview created");
		
		//FrameLayout tmp = (FrameLayout) findViewById(R.id.RootFrame);
		//RelativeLayout tmp = (RelativeLayout) findViewById(R.layout.activity_main);
		RelativeLayout tmp = (RelativeLayout) findViewById(R.layout.activity_main);
		
		if ( tmp != null ) {
			Log.d(TAG, "Layout found");
		} else {		
			Log.d(TAG, "Not found layout");
		}
		
		if ( mPreview != null ) {
			Log.d(TAG, "Will add preview");
			try {
			tmp.addView(mPreview);
			} catch (Exception e) {
				Toast.makeText(getApplicationContext(),"Got exception: " + e.toString(), Toast.LENGTH_LONG).show();
			}
			
		} else {
			Log.d(TAG, "Preview creation FAILED");
		}
		
		Log.d(TAG, "Added preview");
		
		ledIndicator_.bringToFront();
		toggleButton_.bringToFront();
		pictureButton_.bringToFront();

		mPulseWidth = new int[mLedCount];
		mLedState = new boolean[mLedCount];
		for (int index=0;index<mLedCount;index++) {
			mLedState[index] = false;
			mPulseWidth[index] = defaultPulseWidth;
		}
		//buttonClick = (Button) findViewById(R.id.button1);

		pictureButton_.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				/*
				if ( powerState_ && mPreview.isReady() ) {
					powerLedsOn();
					mPreview.camera.takePicture(shutterCallback, rawCallback, jpegCallback);
					powerLedsOff();
				}
				*/
				if ( powerState_ ) {
					takeColorSeries();
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
					mShutdown = true;
				}                                               
			}
		});

		pictureButton_.setEnabled(false);

		Log.d(TAG, "onCreate'd");
		mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
		Log.d(TAG, "  getCameraDisabled(): " + mDPM.getCameraDisabled(null));

		
		// Display the fragment as the main content.
		/*
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
		 */
		/* Preferences
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		 */
		//String syncConnPref = sharedPref.getString(SettingsActivity.KEY_PREF_SYNC_CONN, "");
		/*
        pulseWidthFocus_ = sharedPref.getInt(SETTINGS_CHANNEL_FOCUS, defaultPulseWidth);

        pulseWidth1_ = sharedPref.getInt(SETTINGS_CHANNEL0, defaultPulseWidth);
        pulseWidth2_ = sharedPref.getInt(SETTINGS_CHANNEL1, defaultPulseWidth);
        pulseWidth3_ = sharedPref.getInt(SETTINGS_CHANNEL2, defaultPulseWidth);
		 */
		/*
		pulseWidth1_ = 0;
		pulseWidth2_ = 0;
		pulseWidth3_ = 0;

		pulseWidth4_ = 0;
		pulseWidth5_ = 0;
		pulseWidth6_ = 0;
		*/

		enableUi(false);
		Log.d(TAG, "onCreate - done");
	}

	private boolean powerState_ = false;
	
	// private Thread waitThread;

	private void takeColorSeries() {
		
		
		try {
			for (int index = 0; index < mLedCount; index++) {
				mLedState[index] = true;
				mPulseWidth[index] = defaultPulseWidth;
				
				/*
			    waitThread=  new Thread(){
			        @Override
			        public void run(){
			            try {
			                synchronized(this){
			                    wait(mLedFlashTime);
			                }
			            }
			            catch(InterruptedException ex){                    
			            }

			            // TODO              
			        }
			    };

			    waitThread.start();
			    */
				Thread.sleep(mLedFlashTime);
				mPulseWidth[index] = 0;
				mLedState[index] = false;
			}
			synchronized (MainActivity.class) {
				mShutdown = true;
				
			}
/*
			ledIndicator_.turnOn(0);
			pulseWidth1_ = defaultPulseWidth;
			pulseWidth2_ = 0;
			pulseWidth3_ = 0;

			Thread.sleep(mLedFlashTime);
			ledIndicator_.turnOff(0);
			
			ledIndicator_.turnOn(1);
			pulseWidth1_ = 0;
			pulseWidth2_ = defaultPulseWidth;
			pulseWidth3_ = 0;
			Thread.sleep(mLedFlashTime);
			ledIndicator_.turnOff(1);
			
			ledIndicator_.turnOn(2);
			pulseWidth1_ = 0;
			pulseWidth2_ = 0;
			pulseWidth3_ = defaultPulseWidth;
			Thread.sleep(mLedFlashTime);
			ledIndicator_.turnOff(2);
			pulseWidth3_ = 0;
			
			ledIndicator_.turnOn(3);
			pulseWidth4_ = defaultPulseWidth;
			pulseWidth5_ = 0;
			pulseWidth6_ = 0;
			Thread.sleep(mLedFlashTime);
			ledIndicator_.turnOff(3);

			ledIndicator_.turnOn(4);
			pulseWidth4_ = 0;
			pulseWidth5_ = defaultPulseWidth;
			pulseWidth6_ = 0;
			Thread.sleep(mLedFlashTime);
			ledIndicator_.turnOff(4);

			ledIndicator_.turnOn(5);
			pulseWidth4_ = 0;
			pulseWidth5_ = 0;
			pulseWidth6_ = defaultPulseWidth;
			Thread.sleep(mLedFlashTime);
			ledIndicator_.turnOff(5);

			
			pulseWidth1_ = 0;
			pulseWidth2_ = 0;
			pulseWidth3_ = 0;
			pulseWidth4_ = 0;
			pulseWidth5_ = 0;
			pulseWidth6_ = 0;
	*/		
			
			powerState_ = false;
			ledIndicator_.setPowerState(false);
			toggleButton_.setChecked(false);
			pictureButton_.setEnabled(false);
			
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	/*
	private void powerLedsOn() {
		pulseWidth1_ = defaultPulseWidth;
		pulseWidth2_ = defaultPulseWidth;
		pulseWidth3_ = defaultPulseWidth;

		ledIndicator_.turnOn(0);
		ledIndicator_.turnOn(1);
		ledIndicator_.turnOn(2);
	}
*/
	
	private void powerLedsOff() {
		synchronized (MainActivity.class) {
			mShutdown = true;	
		}
		
		for (int index = 0; index < mLedCount; index++) {
			mLedState[index] = false;
			mPulseWidth[index] = 0;
			ledIndicator_.setLedState(index, mLedState[index]);
		}
		powerState_ = false;
		
		/*
		pulseWidth1_ = 0;
		pulseWidth2_ = 0;
		pulseWidth3_ = 0;

		pulseWidth4_ = 0;
		pulseWidth5_ = 0;
		pulseWidth6_ = 0;
		
		ledIndicator_.turnOff(0);
		ledIndicator_.turnOff(1);
		ledIndicator_.turnOff(2);
		
		ledIndicator_.turnOff(3);
		ledIndicator_.turnOff(4);
		ledIndicator_.turnOff(5);
		*/               		
	}

	//private int pulseWidth1_, pulseWidth2_, pulseWidth3_, pulseWidth4_, pulseWidth5_, pulseWidth6_;



	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	class Looper extends BaseIOIOLooper {
		// private AnalogInput input_;
		private PwmOutput pwmOutput1_;  // board-1-1
		private PwmOutput pwmOutput2_;  // board-1-2
		private PwmOutput pwmOutput3_;  // board-1-3
		private PwmOutput pwmOutput4_;  // board-2-1
		private PwmOutput pwmOutput5_;  // board-2-2
		private PwmOutput pwmOutput6_;  // board-2-3
		
		private DigitalOutput led_;
		private DigitalOutput powout1_;
		private DigitalOutput powout2_;

		
		
		@Override
		public void setup() throws ConnectionLostException {
			led_ = ioio_.openDigitalOutput(IOIO.LED_PIN, true);
			powout1_ = ioio_.openDigitalOutput(6, true);
			powout2_ = ioio_.openDigitalOutput(5, true);
			//input_ = ioio_.openAnalogInput(40);

			pwmOutput1_ = ioio_.openPwmOutput(10, 100);
			pwmOutput2_ = ioio_.openPwmOutput(11, 100);
			pwmOutput3_ = ioio_.openPwmOutput(12, 100);

			pwmOutput4_ = ioio_.openPwmOutput(2, 100);
			pwmOutput5_ = ioio_.openPwmOutput(3, 100);
			pwmOutput6_ = ioio_.openPwmOutput(4, 100);

			
			pwmOutput1_.setPulseWidth( 0 );
			pwmOutput2_.setPulseWidth( 0 );
			pwmOutput3_.setPulseWidth( 0 );

			
			
			powout1_.write( false );
			powout2_.write( false );

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

			visualize();

			//setNumber( 1, pulseWidth1_ );
			//setNumber( 2, pulseWidth2_ );
			//setNumber( 3, pulseWidth3_ );

			synchronized (MainActivity.class) {
				if (!mShutdown) {
					powout1_.write( powerState_ );
					powout2_.write( powerState_ );			
					
				}
			}
			led_.write( !powerState_ );
			Thread.sleep(5);
			
			
			
			
			pwmOutput1_.setPulseWidth( mLedState[0] == false ? 0 : mPulseWidth[0] );
			pwmOutput2_.setPulseWidth( mLedState[1] == false ? 0 : mPulseWidth[1] );
			pwmOutput3_.setPulseWidth( mLedState[2] == false ? 0 : mPulseWidth[2] );
			pwmOutput4_.setPulseWidth( mLedState[3] == false ? 0 : mPulseWidth[3] );
			pwmOutput5_.setPulseWidth( mLedState[4] == false ? 0 : mPulseWidth[4] );
			pwmOutput6_.setPulseWidth( mLedState[5] == false ? 0 : mPulseWidth[5] );

			synchronized (MainActivity.class) {
				if ( mShutdown) {
					Thread.sleep(50);
					powout1_.write( powerState_ );
					powout2_.write( powerState_ );
				}
			}
						
			led_.write( !powerState_ );
			

			// Log.d(TAG, "IOIO-output: " );

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

				toggleButton_.setEnabled(enable);
				mPreview.setEnabled(enable);

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

	private boolean[] mLedState;
	
	private void visualize() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				ledIndicator_.setPowerState(powerState_);
				
				for (int index=0;index<mLedCount;index++) {
					ledIndicator_.setLedState(index, mLedState[index]);
				}
				
			}
		});
	}

	// --------------------------------------------------------------------------------
	// camera stuff


	private static String mImagePrefix = "focus";
	public static final String TAG = "SCMA";
	protected Camera mCamera;
	protected Preview mPreview;
	protected Button buttonClick;

	DevicePolicyManager mDPM;


	ShutterCallback shutterCallback = new ShutterCallback() {
		public void onShutter() {
			Log.d(TAG, "onShutter");
		}
	};

	PictureCallback rawCallback = new PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {
			// writeImageToDisc(FILEMODE_RAW, mImageSuffix, data);

			Log.d(TAG, "onPictureTaken() - raw");

			Log.d(TAG, "onPictureTaken - raw");

		}
	};
	/*
        private class AsyncWriteImageTask extends AsyncTask<String, Void, Boolean> {
            / ** The system calls this to perform work in a worker thread and
	 * delivers it the parameters given to AsyncTask.execute() * /
            protected Boolean doInBackground(String urls, byte[] data) {
                Boolean status = false;

                        FileOutputStream outStream = null;
                        try {
                                outStream = new FileOutputStream(String.format("%s/SCM/%d_%s.jpg",
                                                Environment.getExternalStorageDirectory().getPath(),
                                                System.currentTimeMillis(), mImagePrefix));
                                outStream.write(data);
                                outStream.close();
                                Log.d(TAG, "aSyncWrite - wrote bytes: " + data.length);
                                Toast.makeText(getApplicationContext(), "JPEG - wrote bytes: " + data.length, Toast.LENGTH_LONG).show();
                                status = true;
                        } catch (FileNotFoundException e) {
                                e.printStackTrace();
                        } catch (IOException e) {
                                e.printStackTrace();
                        } finally {
                        }

                return status;
            }

            /** The system calls this to perform work in the UI thread and delivers
	 * the result from doInBackground() * /
            protected void onPostExecute(Boolean result) {
                // mImageView.setImageBitmap(result);
                // TODO: do we need here some indicator?

            }

                @Override
                protected Boolean doInBackground(String... params) {
                        // TODO Auto-generated method stub
                        return null;
                }
        }
	 */

	PictureCallback jpegCallback = new PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {

			writeImageToDisc(mCurrentFileMode, mImageSuffix, data);

		}
	};

	private void writeImageToDisc(String filemode, String suffix, byte[] data) {
		Log.d(TAG, "writeImageToDisc - begin");
		FileOutputStream outStream = null;
		try {
			outStream = new FileOutputStream(String.format("%s/SCM/%d_%s.%s",
					Environment.getExternalStorageDirectory().getPath(),
					System.currentTimeMillis(), suffix, filemode));
			outStream.write(data);
			outStream.close();
			Log.d(TAG, "writeImageToDisc - wrote bytes: " + data.length);
			Toast.makeText(getApplicationContext(), "JPEG - wrote bytes: " + data.length, Toast.LENGTH_LONG).show();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
		}               
		Log.d(TAG, "writeImageToDisc - complete");
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
	/*
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                Preference keyPref = findPreference(key);
        if (key.equalsIgnoreCase(SETTINGS_CHANNEL0) or
                        key.equalsIgnoreCase(SETTINGS_CHANNEL1) or
                        key.equalsIgnoreCase(SETTINGS_CHANNEL2) or
                        key.equalsIgnoreCase(SETTINGS_CHANNEL_FOCUS)) {

            // Set summary to be the user-description for the selected value
            //keyPref.setSummary(sharedPreferences.getString(key, ""));
        } else if (key.equals(SETTINGS_CHANNEL1)) {
                pulseWidth2_ = sharedPreferences.getInt(SETTINGS_CHANNEL1, defaultPulseWidth);
        } 
    }
	 */
	/*
        private Preference findPreference(String key) {
                // TODO Auto-generated method stub
                return null;
        }
	 */
	//@Override
	//protected void onResume() {
		//super.onResume();
		/*
            getPreferenceScreen().getSharedPreferences()
                    .registerOnSharedPreferenceChangeListener(this);
		 */
	//}

	/*
        private Preference getPreferenceScreen() {
                // TODO Auto-generated method stub
                return null;
        }
	 */

	//@Override
	//protected void onPause() {
	//	super.onPause();
		/*
            getPreferenceScreen().getSharedPreferences()
                    .unregisterOnSharedPreferenceChangeListener(this);
		 */
	//}

	/*
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                        String key) {
                // TODO Auto-generated method stub

        }

        public static class SettingsFragment extends PreferenceFragment {
            @Override
            public void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);

                // Load the preferences from an XML resource
                addPreferencesFromResource(R.xml.preferences);
            }       
        }
	 */
}
