package com.softanalle.scma;

import android.os.Bundle;
import android.app.Activity;
import android.app.Application;
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
import android.os.Handler;
import android.text.Layout;
import android.util.Log;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
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
import android.preference.PreferenceManager;
//import android.preference.Preference;
//import android.preference.PreferenceFragment;
//import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.SyncResult;
import android.content.pm.PackageManager;
//import android.content.SharedPreferences;
//import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;

import android.app.admin.DevicePolicyManager;


public class MainActivity extends IOIOActivity 
implements OnSharedPreferenceChangeListener 
 
{
	// IOIO pin settings
	private static final int IOIO_PIN_BOARD1_UP = 6;
	private static final int IOIO_PIN_BOARD2_UP = 5;
	
	private static final int IOIO_PIN_LED_WHITE  = 2;
	private static final int IOIO_PIN_LED_YELLOW = 3;
	private static final int IOIO_PIN_LED_NIR    = 4;
	private static final int IOIO_PIN_LED_GREEN  = 10;
	private static final int IOIO_PIN_LED_BLUE   = 11;
	private static final int IOIO_PIN_LED_RED    = 12;


	private static final int LED_INDEX_BLUE = 0;
	private static final int LED_INDEX_GREEN = 1;
	private static final int LED_INDEX_RED = 2;
	private static final int LED_INDEX_WHITE = 3;
	private static final int LED_INDEX_YELLOW = 4;
	private static final int LED_INDEX_NIR = 5;
	// 
	//protected final String FILEMODE_JPG = "jpg";
	//protected final String FILEMODE_RAW = "raw";

	volatile boolean mShutdown = false;
	private boolean saveModeJPEG = false;
	private boolean saveModeRAW = false;
	private int mCurrentLedIndex = 0;
	public final String[] mImageSuffix = { "blue", "green", "red", "white", "yellow", "nir", "other" };

	private final int defaultPulseWidth = 500; // PWM pulse width
	private final int mLedFlashTime = 10; // milliseconds the led takes to raise/shutdown
	private final int mLedCount = 6; // how many leds actually there are!

	private int mFocusLedIndex = 2; // led INDEX of focus color; default=3  
	private int defaultFocusPulseWidth = 300; // PWM pulse width for focus 
	
	private int mPulseWidth[];      // PWM pulse width array for leds
	private boolean[] mLedState;    // led status (on/off)
	private int[] mDefaultPulseWidth; // default pulse width for leds 
	
	
	private ToggleButton toggleButton_;
	private LedIndicator ledIndicator_;
	private Button pictureButton_, focusButton_;
	private int pulseWidthFocus_;

    // Splash screen timer
    private static final int SPLASH_TIME_OUT = 3000;

	// private static String mImagePrefix = "focus";
	public static final String TAG = "SCMA";
	
	public static final String KEY_PREF_FOCUSCOLOR = "pref_focuscolor";
	public static final String KEY_PREF_DEF_PULSEWIDTH = "pref_pulsewidth_default";
	public static final String KEY_PREF_RED_PULSEWIDTH = "conf_red_pwm";
	public static final String KEY_PREF_GREEN_PULSEWIDTH = "conf_green_pwm";
	public static final String KEY_PREF_BLUE_PULSEWIDTH = "conf_blue_pwm";
	public static final String KEY_PREF_YELLOW_PULSEWIDTH = "conf_yellow_pwm";
	public static final String KEY_PREF_WHITE_PULSEWIDTH = "conf_white_pwm";
	public static final String KEY_PREF_NIR_PULSEWIDTH = "conf_nir_pwm";
	
	public static final String KEY_PREF_FOCUS_PULSEWIDTH = "conf_focus_pwm";
	public static final String KEY_PREF_SAVE_JPEG = "conf_write_jpeg";
	public static final String KEY_PREF_SAVE_RAW = "conf_write_raw";

	private boolean powerState_ = false;
	private String mImagePrefix = "";
	
	// protected Camera mCamera;
	protected Preview mPreview;
	protected Button buttonClick;

	DevicePolicyManager mDPM;

    private TextView messageView_;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTheme(android.R.style.Theme_Black_NoTitleBar_Fullscreen);
		setContentView(R.layout.activity_main);

		
		
		pictureButton_  = (Button) findViewById(R.id.pictureButton);
		toggleButton_ = (ToggleButton) findViewById(R.id.powerButton);
		ledIndicator_ = (LedIndicator) findViewById(R.id.ledIndicator1);
		focusButton_ = (Button) findViewById(R.id.focusButton);

		messageView_ = (TextView) findViewById(R.id.messageView);
		
		toggleButton_.setEnabled(false);
		toggleButton_.setChecked(false);
		
		//focusButton_.setEnabled(false);
	/*
		new Handler().postDelayed(new Runnable() {
			 
            / *
             * Showing splash screen with a timer. This will be useful when you
             * want to show case your app logo / company
             * /
 
            @Override
            public void run() {
                // This method will be executed once the timer is over
                // Start your app main activity
                Intent i = new Intent(getApplicationContext(), SplashActivity.class);
                startActivity(i);
 
                // close this activity
                finish();
            }
        }, SPLASH_TIME_OUT);
		
		*/
		
		// camera stuff
		
		if (checkCameraHardware(getApplicationContext())) {
			// ok, we have camera
			Log.i(TAG, "Device has camera");
		} else {
			Toast.makeText(getApplicationContext(), "Camera is missing!", Toast.LENGTH_LONG).show();
		}
		
		FrameLayout tmp = (FrameLayout) findViewById(R.id.camera_preview);
		

		
		mPreview = new Preview(this);
		//mPreview = new Preview(getApplicationContext());
		Log.d(TAG, "Preview created");
		/*
		Camera camera = mPreview.getCameraInstance();
		if ( camera == null ) {
			Toast.makeText(getApplicationContext(), "Camera is missing!", Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(getApplicationContext(), "We got camera instance...", Toast.LENGTH_LONG).show();
			camera.release();
		}
		*/
		
		
		
		mPreview.startPreview();
		
		if ( tmp == null ) {
			Log.d(TAG, "Layout IS NULL");
			Toast.makeText(getApplicationContext(),"Layout is NULL: ", Toast.LENGTH_LONG).show();
		} else {
		
			Log.d(TAG, "Layout found");
		} 		
			
		
		if ( mPreview != null ) {
			Log.d(TAG, "Will add preview");
			try {
				tmp.addView(mPreview);
				//tmp.addView(mPreview);
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
		focusButton_.bringToFront();
		
		messageView_.bringToFront();
		
		mPulseWidth = new int[mLedCount];
		mLedState = new boolean[mLedCount];
		mDefaultPulseWidth = new int[mLedCount];
		for (int index=0;index<mLedCount;index++) {
			mLedState[index] = false;
			
			mDefaultPulseWidth[index] = defaultPulseWidth;
			mPulseWidth[index] = mDefaultPulseWidth[index];
			
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
					mLedState[mFocusLedIndex] = true;
					mPulseWidth[mFocusLedIndex] = defaultFocusPulseWidth;
					mCurrentLedIndex = mFocusLedIndex;
				} else {
					powerState_ = false;
					pictureButton_.setEnabled(false);
					mShutdown = true;
					mLedState[mFocusLedIndex] = false;
				}                                               
			}
		});

		focusButton_.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				focusCamera();
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
		 * 
		 */
		
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		
        saveModeJPEG = sharedPref.getBoolean(KEY_PREF_SAVE_JPEG, true);
        saveModeRAW = sharedPref.getBoolean(KEY_PREF_SAVE_RAW, false);
        
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

	
	// private Thread waitThread;

	private void focusCamera() {
		// do focusing stuff
		mPreview.camera.autoFocus(null);
		// mPreview.camera
		messageView_.setText("Currently supported WhiteBalance modes: " + mPreview.getWhiteBalanceModes().toString()+
				"Currently supported modes: ");
	}
	
	private void showDebugMessage(String message) {
		messageView_.setText(message);
	}
	

	private void takeColorSeries() {
		mLedState[mFocusLedIndex] = false;
		mPulseWidth[mFocusLedIndex] = mDefaultPulseWidth[mFocusLedIndex];
		
		try {			
			mImagePrefix = Long.toString(System.currentTimeMillis());
			
			for (int index = 0; index < mLedCount; index++) {
								
				mCurrentLedIndex = index;
				mLedState[index] = true;
				mPulseWidth[index] = mDefaultPulseWidth[index];
				
				Thread.sleep(mLedFlashTime);
								
				/*
				Thread runner = new Thread(new Runnable() {
					public void run() {
					*/
				
						// Preview have to be active when picture is taken
				
				
		//		mPreview.camera.takePicture(shutterCallback, rawCallback, jpegCallback);
			
				if (saveModeJPEG) {
					mPreview.startPreview();
					Thread.sleep(10);
					String filename = Environment.getExternalStorageDirectory().getPath() + "/SCM/" +
							mImagePrefix + "_" + 
							mImageSuffix[index] + ".jpg";
					Toast.makeText(getApplicationContext(), "JPEG picture: " + filename,Toast.LENGTH_LONG).show();
					
					mPreview.takeJPEGPicture(filename);
				}
				
				if ( saveModeRAW ) {
					mPreview.startPreview();
					Thread.sleep(10);
					String filename = Environment.getExternalStorageDirectory().getPath() + "/SCM/" +
							mImagePrefix + "_" + 
							mImageSuffix[index] + ".raw";
					Toast.makeText(getApplicationContext(), "RAW picture: " + filename,Toast.LENGTH_LONG).show();
					mPreview.takeRAWPicture(filename);
				}
				//notifyAll();
					//}
				//});

				//runner.start();
				// runner.run();
				// runner.wait();
				
				// we need some pause to allow camera to retry
				Thread.sleep(4000);
								
				mPulseWidth[index] = 0;
				mLedState[index] = false;
				
				// sleep till leds are really turned off
				Thread.sleep(mLedFlashTime);
			}
			
			synchronized (MainActivity.class) {
				mShutdown = true;
				
			}
			// enable preview again
			//mPreview.startPreview();
			powerState_ = false;
			ledIndicator_.setPowerState(false);
			toggleButton_.setChecked(false);
			pictureButton_.setEnabled(false);
			
			mPreview.startPreview();
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
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
	}

	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	class Looper extends BaseIOIOLooper {
		// private AnalogInput input_;
		private PwmOutput pwmOutput1_;  // board-1-1 = pin 10
		private PwmOutput pwmOutput2_;  // board-1-2 = pin 11
		private PwmOutput pwmOutput3_;  // board-1-3 = pin 12
		private PwmOutput pwmOutput4_;  // board-2-1 = pin 2
		private PwmOutput pwmOutput5_;  // board-2-2 = pin 3
		private PwmOutput pwmOutput6_;  // board-2-3 = pin 4
		
		private DigitalOutput led_;
		private DigitalOutput powout1_;
		private DigitalOutput powout2_;


		
		@Override
		public void setup() throws ConnectionLostException {
			led_ = ioio_.openDigitalOutput(IOIO.LED_PIN, true);
			powout1_ = ioio_.openDigitalOutput(IOIO_PIN_BOARD1_UP, true);
			powout2_ = ioio_.openDigitalOutput(IOIO_PIN_BOARD2_UP, true);
			//input_ = ioio_.openAnalogInput(40);

			pwmOutput1_ = ioio_.openPwmOutput(IOIO_PIN_LED_GREEN, 100);
			pwmOutput2_ = ioio_.openPwmOutput(IOIO_PIN_LED_BLUE, 100);
			pwmOutput3_ = ioio_.openPwmOutput(IOIO_PIN_LED_RED, 100);

			pwmOutput4_ = ioio_.openPwmOutput(IOIO_PIN_LED_WHITE, 100);
			pwmOutput5_ = ioio_.openPwmOutput(IOIO_PIN_LED_YELLOW, 100);
			pwmOutput6_ = ioio_.openPwmOutput(IOIO_PIN_LED_NIR, 100);


			powout1_.write( false );
			powout2_.write( false );
			
			pwmOutput1_.setPulseWidth( 0 );
			pwmOutput2_.setPulseWidth( 0 );
			pwmOutput3_.setPulseWidth( 0 );
			pwmOutput4_.setPulseWidth( 0 );
			pwmOutput5_.setPulseWidth( 0 );
			pwmOutput6_.setPulseWidth( 0 );
						

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

			visualize(powerState_, mLedState);

			// Log.d(TAG, "IOIO-output: " );

			//ledIndicator_.setPowerState( powerState_ );
			// default 10ms
			Thread.sleep(100);
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
/*
	private void setNumber(int channel, float f) {
		final String str = String.format("%.2f", f);
		final int ch = channel;
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				
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
				 
				ledIndicator_.setPowerState( powerState_ );                             
			}
		});
	}
*/
	
	
	
	private void visualize(boolean powerState, boolean[] ledstates) {
		final boolean power_ = powerState;
		final boolean[] leds_ = ledstates.clone();
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				ledIndicator_.setPowerState(power_);
				
				for (int index=0;index<leds_.length;index++) {
					ledIndicator_.setLedState(index, leds_[index]);
				}
				
			}
		});
	}

	// --------------------------------------------------------------------------------
	// camera stuff




	public ShutterCallback shutterCallback = new ShutterCallback() {
		public void onShutter() {
			//Log.d(TAG, "onShutter");
		}
	};
/*
	public PictureCallback rawCallback = new PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {
			// writeImageToDisc(FILEMODE_RAW, mImageSuffix, data);
			if ( saveModeRAW ) {			
				String filename = Environment.getExternalStorageDirectory().getPath() + "/SCM/" +
						mImagePrefix + "_" + 
						mImageSuffix[mCurrentLedIndex] + ".raw";
				writeImageToDisc(filename, data);				
			}
		}
	};

	public PictureCallback jpegCallback = new PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {
			if ( saveModeJPEG ) {
				String filename = Environment.getExternalStorageDirectory().getPath() + "/SCM/" +
						mImagePrefix + "_" + 
						mImageSuffix[mCurrentLedIndex] + ".jpg";
				writeImageToDisc(filename, data);
			}
		}
	};
	*/
/*
	private void writeImageToDisc(String filename, byte[] data) {
		//Log.d(TAG, "writeImageToDisc - begin");
		FileOutputStream outStream = null;
		try {			
			outStream = new FileOutputStream( filename );			
			outStream.write(data);
			outStream.close();
			//Log.d(TAG, "writeImageToDisc - wrote bytes: " + data.length);
			Toast.makeText(getApplicationContext(), filename + " - wrote bytes: " + data.length, Toast.LENGTH_SHORT).show();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
		}               
		//Log.d(TAG, "writeImageToDisc - complete");
	}
*/
	@Override public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent = null;
		switch (item.getItemId()) {
		case R.id.scma_settings:
			// Toast.makeText(getApplicationContext(), "SCMA Settings menu", Toast.LENGTH_LONG).show();
			intent = new Intent(getApplicationContext(), AppPreferenceActivity.class);
			startActivity(intent);
			return true;
			
		case R.id.reset_settings:
			Toast.makeText(getApplicationContext(), "Reset settings", Toast.LENGTH_LONG).show();
			resetSettings();
			return true;
			
		case R.id.about_info:
			
            
            intent = new Intent(getApplicationContext(), SplashActivity.class);
			startActivity(intent);
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	private void resetSettings() {
		
	}
	
	private void getSettings() {
		
	}
	
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                //Preference keyPref = findPreference(key);
        	if ( key.equalsIgnoreCase(KEY_PREF_FOCUSCOLOR)) {
        		mFocusLedIndex = sharedPreferences.getInt(KEY_PREF_FOCUSCOLOR, 3);
        	}
        	if ( key.equalsIgnoreCase(KEY_PREF_RED_PULSEWIDTH)) {
        		mPulseWidth[LED_INDEX_RED] = sharedPreferences.getInt(KEY_PREF_RED_PULSEWIDTH, mDefaultPulseWidth[LED_INDEX_RED]);
        	}
        	if ( key.equalsIgnoreCase(KEY_PREF_BLUE_PULSEWIDTH)) {
        		mPulseWidth[LED_INDEX_BLUE] = sharedPreferences.getInt(KEY_PREF_BLUE_PULSEWIDTH, mDefaultPulseWidth[LED_INDEX_BLUE]);
        	}
        	if ( key.equalsIgnoreCase(KEY_PREF_GREEN_PULSEWIDTH)) {
        		mPulseWidth[LED_INDEX_GREEN] = sharedPreferences.getInt(KEY_PREF_GREEN_PULSEWIDTH, mDefaultPulseWidth[LED_INDEX_GREEN]);
        	}
        	if ( key.equalsIgnoreCase(KEY_PREF_GREEN_PULSEWIDTH)) {
        		mPulseWidth[LED_INDEX_YELLOW] = sharedPreferences.getInt(KEY_PREF_YELLOW_PULSEWIDTH, mDefaultPulseWidth[LED_INDEX_YELLOW]);
        	}
        	if ( key.equalsIgnoreCase(KEY_PREF_WHITE_PULSEWIDTH)) {
        		mPulseWidth[LED_INDEX_WHITE] = sharedPreferences.getInt(KEY_PREF_WHITE_PULSEWIDTH, mDefaultPulseWidth[LED_INDEX_WHITE]);
        	}
        	if ( key.equalsIgnoreCase(KEY_PREF_NIR_PULSEWIDTH)) {
        		mPulseWidth[LED_INDEX_NIR] = sharedPreferences.getInt(KEY_PREF_NIR_PULSEWIDTH, mDefaultPulseWidth[LED_INDEX_NIR]);
        	}        	        
    }
	 
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
	
	/** Check if this device has a camera */
	private boolean checkCameraHardware(Context context) {
	    if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
	        // this device has a camera
	        return true;
	    } else {
	        // no camera on this device
	        return false;
	    }
	}
}
