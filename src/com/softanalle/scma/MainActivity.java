package com.softanalle.scma;

/**
 * @author Tommi Rintala <t2r@iki.fi>
 * @license GNU GPL 3.0
 * 
    Main Activity for SCMA
 
    Copyright (C) 2013  Tommi Rintala <t2r@iki.fi>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import android.os.Bundle;
// import android.app.Activity;
// import android.app.Application;
import android.view.Menu;

import java.io.File;
// import java.io.FileNotFoundException;
// import java.io.FileOutputStream;
// import java.io.IOException;

//import ioio.lib.api.AnalogInput;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.IOIO;
import ioio.lib.api.PwmOutput;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;
// import android.annotation.TargetApi;
import android.app.admin.DevicePolicyManager;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
//import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.text.Layout;
import android.util.Log;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
//import android.widget.SeekBar;
//import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;


import com.google.code.microlog4android.Logger;
import com.google.code.microlog4android.LoggerFactory;
import com.google.code.microlog4android.config.PropertyConfigurator;
//import com.softanalle.scmctrls.LedIndicator;
//import com.softanalle.previewtest.Preview;
import com.softanalle.scma.R;


// camera stuff
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

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
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.SyncResult;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
//import android.content.SharedPreferences;
//import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.FrameLayout;

import android.app.admin.DevicePolicyManager;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

public class MainActivity extends IOIOActivity 
implements OnSharedPreferenceChangeListener 
 
{
	// IOIO pin settings
	private static final int IOIO_PIN_BOARD1_UP = 6;
	private static final int IOIO_PIN_BOARD2_UP = 5;
	
	private static final int IOIO_PIN_LED_WHITE  = 11;
	private static final int IOIO_PIN_LED_YELLOW = 10;
	private static final int IOIO_PIN_LED_NIR    = 12;
	private static final int IOIO_PIN_LED_GREEN  = 2;
	private static final int IOIO_PIN_LED_BLUE   = 3;
	private static final int IOIO_PIN_LED_RED    = 4;

	// if we need to do soft reset to IOIO, set this boolean to true
	private boolean doIOIOreset = false;

	private static final int LED_INDEX_GREEN = 0;
	private static final int LED_INDEX_BLUE = 1;	
	private static final int LED_INDEX_RED = 2;
	private static final int LED_INDEX_WHITE = 3;
	private static final int LED_INDEX_YELLOW = 4;
	private static final int LED_INDEX_NIR = 5;
	
	private static final int LED_INDEX_FOCUS = 6;
	private static final int LED_INDEX_CALIBRATE = 7;
	// 
	//protected final String FILEMODE_JPG = "jpg";
	//protected final String FILEMODE_RAW = "raw";

	volatile boolean mShutdown = false;
	private boolean saveModeJPEG = false;
	private boolean saveModeRAW = false;
	private int mCurrentLedIndex = 0;
	public final String[] mImageSuffix = { "GREEN", "BLUE",  "RED", "WHITE", "YELLOW", "NIR", "OTHER" };

	private final int defaultPulseWidth = 500; // PWM pulse width
	private final int mLedFlashTime = 30; // milliseconds the led takes to raise/shutdown
	private final int mLedCount = 6; // how many leds actually there are!

	private int mFocusLedIndex = 2; // led INDEX of focus color; default=3  
	private int defaultFocusPulseWidth = 300; // PWM pulse width for focus 
	
	/// Delays for application
    private static final int mCameraRetryDelay = 1000; // delay before opening camera preview
    private static final int mLedWaitDelay = 50;       // delay for waiting led's to lit
    private static final int mRawPictureDelay = 1000;  // delay for RAW image write operation
    
	
	private int mPulseWidth[];      // PWM pulse width array for leds
	private boolean[] mLedState;    // led status (on/off)
	private int[] mDefaultPulseWidth; // default pulse width for leds 
	
	
	private ToggleButton toggleButton_;
	private static LedIndicator ledIndicator_;
	private Button pictureButton_, focusButton_;
	private int focusPulseWidth_ = defaultFocusPulseWidth;

	public static String mStorageDir = "n/a";	

    // Splash screen timer
    private static final int SPLASH_TIME_OUT = 3000;

    /*
     * TODO: Focus ledille oma pulsewidth asetus:
     * - muuttuja joka kertoo onko focus led nyt päällä vaiko ei
     * - kun focus väri ledi ajetaan päälle, käytetään sitten focus PWM:ää eikä ledin normaalia voimakkuutta
     */
    
    
	// private static String mImagePrefix = "focus";
	public static final String TAG = "SCMA";
	
	public static final String KEY_PREF_FOCUSCOLOR = "conf_focusled_color";
	// public static final String KEY_PREF_DEFAULT_PULSEWIDTH = "conf_pwm_default";
	public static final String KEY_PREF_RED_PULSEWIDTH = "conf_red_pwm";
	public static final String KEY_PREF_GREEN_PULSEWIDTH = "conf_green_pwm";
	public static final String KEY_PREF_BLUE_PULSEWIDTH = "conf_blue_pwm";
	public static final String KEY_PREF_YELLOW_PULSEWIDTH = "conf_yellow_pwm";
	public static final String KEY_PREF_WHITE_PULSEWIDTH = "conf_white_pwm";
	public static final String KEY_PREF_NIR_PULSEWIDTH = "conf_nir_pwm";
	
	public static final String KEY_PREF_FOCUS_PULSEWIDTH = "conf_focus_pwm";
	public static final String KEY_PREF_SAVE_JPEG = "conf_write_jpeg";
	public static final String KEY_PREF_SAVE_RAW = "conf_write_raw";

	public static final String KEY_PREF_PREVIEW_SCALE = "conf_preview_scale";
	
	private boolean powerState_ = false;
	private String mImagePrefix = "";
	
	// protected Camera mCamera;
	protected Preview mPreview;
	protected Button buttonClick;

	DevicePolicyManager mDPM;

	public static final int RESULT_SETTINGS_ACTIVITY = 1;
	public static final int RESULT_IMAGE_ACTIVITY = 2;	

	// Bundle key names
	public static final String ARG_IMAGE_PREFIX = "arg_image_prefix";
	public static final String ARG_WORKDIR = "arg_workdir";
	public static final String ARG_IMAGE_ACTIVITY_RESULT = "arg_img_result";
	
	
    private Object lock_ = new Object();
    
    private static final Logger logger = LoggerFactory.getLogger();
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTheme(android.R.style.Theme_Black_NoTitleBar_Fullscreen);
		setContentView(R.layout.activity_main);

		PropertyConfigurator.getConfigurator(this).configure();

		logger.debug("onCreate()");
		
		pictureButton_  = (Button) findViewById(R.id.pictureButton);
		toggleButton_ = (ToggleButton) findViewById(R.id.powerButton);
		ledIndicator_ = (LedIndicator) findViewById(R.id.ledIndicator1);
		focusButton_ = (Button) findViewById(R.id.focusButton);

		
		toggleButton_.setEnabled(false);
		toggleButton_.setChecked(false);
		
		
		// camera stuff
		
		if (checkCameraHardware(getApplicationContext())) {
			// ok, we have camera
			Log.i(TAG, "Device has camera");
		} else {
			Toast.makeText(getApplicationContext(), "Camera is missing!", Toast.LENGTH_LONG).show();
		}
		
		FrameLayout tmp = (FrameLayout) findViewById(R.id.camera_preview);
		

		
		mPreview = new Preview(this);

		Log.d(TAG, "Preview created");
		
		
		
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
		
		
		
		mPulseWidth = new int[mLedCount];
		mLedState = new boolean[mLedCount];
		mDefaultPulseWidth = new int[mLedCount];
		synchronized (lock_) {
			for (int index=0;index<mLedCount;index++) {
				mLedState[index] = false;

				mDefaultPulseWidth[index] = defaultPulseWidth;
				//mPulseWidth[index] = mDefaultPulseWidth[index];
			}	
		}


		pictureButton_.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				logger.debug("pictureButton.onClick()");
				if ( powerState_ ) {
					// disable buttons -> no error clicks
					pictureButton_.setEnabled(false);
					toggleButton_.setEnabled(false);
					focusButton_.setEnabled(false);
				    Runnable runnable = new Runnable() {
				      @Override
				      public void run() {
				    	  // run the picture sequence
				    	  takeColorSeries();
				      }				      
				    };
				
				    new Thread(runnable).start();
				}
			}
		});


		toggleButton_.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {		
				logger.debug("toggleButton.onClick(" + ((ToggleButton) v).isChecked() + ")");
				// mPreview.camera.takePicture(shutterCallback, rawCallback, jpegCallback);
				if ( ((ToggleButton) v).isChecked()) {
					powerState_ = true;
					pictureButton_.setEnabled(true);
					mLedState[mFocusLedIndex] = true;
					mPulseWidth[mFocusLedIndex] = defaultFocusPulseWidth;
					mCurrentLedIndex = mFocusLedIndex;
					
					// make sure we don't go to screenlock while working
					/*
					PowerManager pm = (PowerManager) getApplicationContext().getSystemService( Context.POWER_SERVICE );
					wl = pm.newWakeLock(
							PowerManager.SCREEN_DIM_WAKE_LOCK
							| PowerManager.ON_AFTER_RELEASE,
							TAG);					
					wl.acquire();
					*/
				} else {
					powerState_ = false;
					pictureButton_.setEnabled(false);
					mShutdown = true;
					mLedState[mFocusLedIndex] = false;
					
					// release screen un-locker
					//wl.release();

				}                                               
			}
		});

		focusButton_.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				logger.debug("focusButton.onClick()");
				try {
					ledIndicator_.setLedState(LED_INDEX_FOCUS, true);
					Thread.sleep(10);
					focusCamera();
					Thread.sleep(10);
					ledIndicator_.setLedState(LED_INDEX_FOCUS, false);
				} catch (Exception e) {

				}
			}
		});
		
		pictureButton_.setEnabled(false);

		Log.d(TAG, "onCreate'd");
		mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
		Log.d(TAG, "  getCameraDisabled(): " + mDPM.getCameraDisabled(null));

		

		getSettings();
		/*
		List<Integer> modes = mPreview.getSupportedPictureFormats();
		String lista = "";
		if ( modes != null && modes.size() > 0) {
			for (Integer i : modes) {
				lista += Integer.toString(i) + " ";
			}
		} else {
			lista = "List query failed";
		}
		Toast.makeText(getApplicationContext(), "Supported Picture formats: " + lista, Toast.LENGTH_LONG).show();
		*/
		enableUi(false);
		Log.d(TAG, "onCreate - done");

		this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}

//	private PowerManager.WakeLock wl = null;
	
	// private Thread waitThread;

	private void focusCamera() {
		logger.debug("focusCamera()");
		mFocusOn = true;
		mFocusCount = 0;
		try {
			Thread.sleep(20);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		// do focusing stuff
		//mPreview.camera.autoFocus(null);
		mPreview.camera.autoFocus(myAutoFocusCallback);
	}
	
	
	AutoFocusCallback myAutoFocusCallback = new AutoFocusCallback(){

		  @Override
		  public void onAutoFocus(boolean arg0, Camera arg1) {
			  mLedState[mFocusLedIndex] = false;
		  }};
	
	private boolean mFocusOn = false;
	private int mFocusCount = 0;

	private void takeColorSeries() {
		logger.debug("takeColorSeries()");
		mLedState[mFocusLedIndex] = false;
		mPulseWidth[mFocusLedIndex] = mDefaultPulseWidth[mFocusLedIndex];
		
		try {			
			mImagePrefix = Long.toString(System.currentTimeMillis());
			
			for (int index = 0; index < mLedCount; index++) {
			
				synchronized (lock_) {
					mCurrentLedIndex = index;
					mLedState[index] = true;
					mPulseWidth[index] = mDefaultPulseWidth[index];
				}
				
				Thread.sleep(mLedWaitDelay);
										
				String imageFilename = mStorageDir + "/" + mImagePrefix + "_" + mImageSuffix[index];
	
				mPreview.startPreview();
				mPreview.takePicture(saveModeJPEG, saveModeRAW, imageFilename);
				if ( saveModeJPEG ) {
					Thread.sleep(mRawPictureDelay);
				}
				
				// Thread.sleep(mLedWaitDelay);
				
				if ( saveModeRAW ) {
					Thread.sleep(mRawPictureDelay);
				}
				
	
								
				Thread.sleep(mCameraRetryDelay);
				
				mPulseWidth[index] = 0;
				mLedState[index] = false;
				
				// sleep till leds are really turned off
				Thread.sleep(mLedWaitDelay);
			}
			
			synchronized (lock_) {
				mShutdown = true;
				
			}
	
			powerState_ = false;
			runOnUiThread(new Runnable() {
				final String image_name = mImagePrefix;
				@Override
				public void run() {
					ledIndicator_.setPowerState(false);
					toggleButton_.setChecked(false);
					pictureButton_.setEnabled(false);
					focusButton_.setEnabled(true);
					powerLedsOff();
					//mPreview.startPreview();

					Intent intent = new Intent(getApplicationContext(), ImageActivity.class);
					intent.putExtra( ARG_IMAGE_PREFIX, image_name );
					intent.putExtra( ARG_WORKDIR, mStorageDir );
					startActivityForResult( intent, RESULT_IMAGE_ACTIVITY );
					

				}
			});			
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}
	
	
	private void powerLedsOff() {
		logger.debug("powerLedsOff()");
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

	public void setFocusLed(boolean onoff) {
		logger.debug("setFocusLed("+onoff+")");
		ledIndicator_.setLedState(LED_INDEX_FOCUS, onoff);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		logger.debug("onCreateOptionsMenu()");
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		//Toast.makeText(getApplicationContext(), "StorageDir = " + mStorageDir, Toast.LENGTH_LONG).show();
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
			logger.debug("IOIO.setup()");
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
						

			
			mFocusCount = 0;
			mFocusOn = false;
			
			enableUi(true);
		}

		
		
		@Override
		public void loop() throws ConnectionLostException, InterruptedException {
		
			synchronized (lock_) {
				if (!mShutdown) {
					powout1_.write( powerState_ );
					powout2_.write( powerState_ );			
					
				}
			}
			led_.write( !powerState_ );
			
			//Thread.sleep(5);
			
			synchronized ( lock_ ) {
				if ( mFocusOn ) {
					mFocusCount++;					
					if ( mFocusCount < 20 ) {
					mLedState[mFocusLedIndex] = true;
					} else {
						mLedState[mFocusLedIndex] = false;
						mFocusOn = false;
						mFocusCount = 0;
					}
				}
				
				pwmOutput1_.setPulseWidth( mLedState[0] == false ? 0 : mPulseWidth[0] );
				pwmOutput2_.setPulseWidth( mLedState[1] == false ? 0 : mPulseWidth[1] );
				pwmOutput3_.setPulseWidth( mLedState[2] == false ? 0 : mPulseWidth[2] );
				pwmOutput4_.setPulseWidth( mLedState[3] == false ? 0 : mPulseWidth[3] );
				pwmOutput5_.setPulseWidth( mLedState[4] == false ? 0 : mPulseWidth[4] );
				pwmOutput6_.setPulseWidth( mLedState[5] == false ? 0 : mPulseWidth[5] );


				if ( mShutdown) {
					// wait-time, required for making sure IOIO has turned power off from the (led)controller board
					Thread.sleep(50);
					powout1_.write( powerState_ );
					powout2_.write( powerState_ );
				}
			}
		
			/*
			 *  if we need (for some reason) to do softreset to IOIO
			 */
			if ( doIOIOreset ) {
				ioio_.softReset();
				doIOIOreset = false;
			}
			
			led_.write( !powerState_ );

			visualize(powerState_, mLedState);

			// default 10ms
			Thread.sleep(10);
		}

		/*
		 * Actions what we wish to do, when (if) IOIO is disconnected, eg. due to battery failure or something else
		 * @see ioio.lib.util.BaseIOIOLooper#disconnected()
		 */
		@Override
		public void disconnected() {
			logger.debug("IOIO.disconnected()");
			enableUi(false);
			showError("Connection error.", "Connection to led controller (IOIO) was lost."); 
		}
	}

	/*
	 * Create the IOIO looper.
	 * @see ioio.lib.util.android.IOIOActivity#createIOIOLooper()
	 */
	@Override
	protected IOIOLooper createIOIOLooper() {
		logger.debug("createIOIOLooper()");
		return new Looper();
	}
	
	/*
	 * enable/disable the UI components. This is called by IOIO looper!!
	 */
	private void enableUi(final boolean enable) {
		logger.debug("enableUI()");
		runOnUiThread(new Runnable() {
			@Override
			public void run() {

				toggleButton_.setEnabled(enable);
				mPreview.setEnabled(enable);

			}
		});
	}
	
	
	/*
	 * visualize, ie. turn led indicators on/off at the UI screen
	 * @param powerState the power status of IOIO board
	 * @param ledstates[] the led states (on/off)
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

	

	void showError(String err, String msg) {
		logger.debug("showError('" + err + ", '" + msg + "')");
		new AlertDialog.Builder(this)
		.setIcon(R.drawable.ic_dialog_alert)
		.setTitle("Error")
		.setMessage(err + " " + msg) 
		/*
				.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						//Stop the activity
						//maintenancetabs.this.finish();
						int pid = android.os.Process.myPid();
						android.os.Process.killProcess(pid);
					}
				})*/
		.setNegativeButton("No", null)
		.show();		
	}

	
	
	private void copyTestAssets() {
		logger.debug("copyTestAssets()");
		AssetManager am = getResources().getAssets();
		String[] list;
		try {			
			list = am.list("test");
			File destDir = new File(mStorageDir);
			for (String s:list) {
								
				File dstFile = new File(destDir, s);
				
				InputStream in = am.open("test/" + s);
				
				FileUtils.copyInputStreamToFile(in, dstFile);

				Log.d(TAG, "Copied: " + s);
				Log.d(TAG, "Dest: " + mStorageDir);
				in.close();
				
			}
		} catch (IOException e) {

			e.printStackTrace();
			String message = "";
			if ( e.getMessage() != null ) {
				message = e.getMessage();
			}
			showError(e.toString(), message);
		} catch (Exception e) {
			String message = "";
			if ( e.getMessage() != null ) {
				message = e.getMessage();
			}
			showError(e.toString(), message);
		}

	}
	
	/*
	 * reset the settings to default values. They are defined in preferences.xml -file.
	 */
	private void resetSettings() {
		logger.debug("resetSettings()");
        PreferenceManager.setDefaultValues(this, R.xml.preferences, true);
        Toast.makeText(getApplicationContext(), "Settings reseted", Toast.LENGTH_LONG).show();
	}

	private void getSettings() {
		logger.debug("getSettings()");
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		String dump = "before";
		File tmp = Environment.getExternalStorageDirectory();
		try {
			mStorageDir = new File(tmp.getCanonicalPath() + "/SCM").getCanonicalPath();
		} catch (IOException e1) {
			String msg = "";
			if ( e1.getMessage() != null ) {
				msg = e1.getMessage();
			}
			showError(e1.toString(), msg);
		}
		
		try {
			File saveDir = new File( mStorageDir );
			if ( ! saveDir.isDirectory()) {
				saveDir.mkdir();
			}
		} catch (Exception e) {
			Log.d(TAG, "Error in creation of savedir");
		}
		
        try {
        	saveModeJPEG = sharedPref.getBoolean(KEY_PREF_SAVE_JPEG, true);
        	saveModeRAW = sharedPref.getBoolean(KEY_PREF_SAVE_RAW, false);
        	dump = "green";
        	mPulseWidth[LED_INDEX_GREEN] = Integer.parseInt(sharedPref.getString(KEY_PREF_GREEN_PULSEWIDTH, String.valueOf(mDefaultPulseWidth[LED_INDEX_GREEN])));
        	dump = "blue";
        	mPulseWidth[LED_INDEX_BLUE] = Integer.parseInt(sharedPref.getString(KEY_PREF_BLUE_PULSEWIDTH, String.valueOf(mDefaultPulseWidth[LED_INDEX_BLUE])));
        	dump = "red";
        	mPulseWidth[LED_INDEX_RED] = Integer.parseInt(sharedPref.getString(KEY_PREF_RED_PULSEWIDTH, String.valueOf(mDefaultPulseWidth[LED_INDEX_RED])));
        	dump = "yellow";
        	mPulseWidth[LED_INDEX_YELLOW] = Integer.parseInt(sharedPref.getString(KEY_PREF_YELLOW_PULSEWIDTH, String.valueOf(mDefaultPulseWidth[LED_INDEX_YELLOW])));
        	dump = "white";
        	mPulseWidth[LED_INDEX_WHITE] = Integer.parseInt(sharedPref.getString(KEY_PREF_WHITE_PULSEWIDTH, String.valueOf(mDefaultPulseWidth[LED_INDEX_WHITE])));
        	dump = "nir";
        	mPulseWidth[LED_INDEX_NIR] = Integer.parseInt(sharedPref.getString(KEY_PREF_NIR_PULSEWIDTH, String.valueOf(mDefaultPulseWidth[LED_INDEX_NIR])));

        	dump = "focusColor";

        	mFocusLedIndex= Integer.parseInt(sharedPref.getString(KEY_PREF_FOCUSCOLOR, "3"));
        	
        } catch (ClassCastException cce) {
        	Toast.makeText(getApplicationContext(), "Invalid setting for '" + dump + "': " + cce, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
        	Toast.makeText(getApplicationContext(), "Exeption: " + e, Toast.LENGTH_LONG).show();        	
        }

	}
	
	/*
	 * Respond to preference changes
	 * @see android.content.SharedPreferences.OnSharedPreferenceChangeListener#onSharedPreferenceChanged(android.content.SharedPreferences, java.lang.String)
	 */
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		logger.debug("onSharedPreferenceChanged()");
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
		
		if (key.equalsIgnoreCase(KEY_PREF_SAVE_JPEG)) {
			saveModeJPEG = sharedPreferences.getBoolean(KEY_PREF_SAVE_JPEG, true);
		}
		if (key.equalsIgnoreCase(KEY_PREF_SAVE_RAW)) {
			saveModeRAW = sharedPreferences.getBoolean(KEY_PREF_SAVE_RAW, false);
		}		
	}


	/** 
	 * Check if this device has a camera
	 * @param context The application context to be used
	 */
	private boolean checkCameraHardware(Context context) {
		logger.debug("checkCameraHardware()");
	    if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
	        // this device has a camera
	        return true;
	    } else {
	        // no camera on this device
	        return false;
	    }
	}
	
	/*
	 * Actions required when application is paused
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause () {
		super.onPause();
		logger.debug("onPause()");
		if ( mPreview != null ) {
			mPreview.releaseCamera();
		}
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		sp.unregisterOnSharedPreferenceChangeListener(this);
		this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}
	
	/*
	 * Actions required when application is resumed.
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		logger.debug("onResume()");
		if ( mPreview != null ) {
			mPreview.reclaimCamera();
		}
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		sp.registerOnSharedPreferenceChangeListener(this);
        
		getSettings();
		this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}
	
	
	/*
	public void focusComplete() {
		ledIndicator_.setLedState(LED_INDEX_FOCUS, false);
	}
	*/
	/*
	public static final int MSG_FOCUS_ON = 1;
	public static final int MSG_FOCUS_OFF = 2;
	public static final int MSG_CALIBRATE_ON = 3;
	public static final int MSG_CALIBRATE_OFF = 4;
	
	public static final Handler ledHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			switch (msg.arg1) {
			case MSG_FOCUS_ON:
				ledIndicator_.setLedState(LED_INDEX_FOCUS, false);
				
				break;
			case MSG_FOCUS_OFF:
				ledIndicator_.setLedState(LED_INDEX_FOCUS,  true);
				break;
			case MSG_CALIBRATE_OFF:
				ledIndicator_.setLedState(LED_INDEX_CALIBRATE,  false);
				break;
			case MSG_CALIBRATE_ON:
				ledIndicator_.setLedState(LED_INDEX_CALIBRATE,  true);
				break;

			}
			
			//updateUI((String)msg.obj);
		}

	};
	
	*/
	


	/*
	 * Check result codes when returning from started (sub)activities
	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
	 */
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
 
        switch (requestCode) {
        case RESULT_SETTINGS_ACTIVITY:
            getSettings();
            break;
        case RESULT_IMAGE_ACTIVITY:
        	// TODO: do something here!
        	String msg = data.getStringExtra(ARG_IMAGE_ACTIVITY_RESULT);
        	break;
        }
 
    }
	
	/*
	 * Response to key presses
	 * @see android.app.Activity#onKeyDown(int, android.view.KeyEvent)
	 */
	@Override 
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		logger.debug("onKeyDown(keyCode="+keyCode+")");
		switch (keyCode) {
			case KeyEvent.KEYCODE_BACK: {
				//Handle the back button
				//if(keyCode == KeyEvent.KEYCODE_BACK) {
				//Ask the user if they want to quit
				new AlertDialog.Builder(this)
				.setIcon(R.drawable.ic_dialog_alert)
				.setTitle("Exit?")
				.setMessage("You are about to exit the Application. " + 
						"Do you really want to exit?")
						.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								//Stop the activity
								//maintenancetabs.this.finish();
								int pid = android.os.Process.myPid();
								android.os.Process.killProcess(pid);
							}
						})
						.setNegativeButton("No", null)
						.show();
				return true;
			}
			
			// eat zoom -events
			case KeyEvent.KEYCODE_ZOOM_IN: {
				return true;
			}
			case KeyEvent.KEYCODE_ZOOM_OUT: {
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent = null;
		switch (item.getItemId()) {
		case R.id.scma_settings:
			logger.debug("Menu action: settings: begin");
			// Toast.makeText(getApplicationContext(), "SCMA Settings menu", Toast.LENGTH_LONG).show();
			intent = new Intent(getApplicationContext(), AppPreferenceActivity.class);
			startActivityForResult(intent, RESULT_SETTINGS_ACTIVITY);
			logger.debug("Menu action: settings: done");
			return true;
			
		case R.id.reset_settings:
			logger.debug("Menu action: reset settings: begin");
			resetSettings();
			logger.debug("Menu action: reset settings: done");
			return true;
			
		case R.id.calibration:
			logger.debug("Menu action: calibration: begin");
			ledIndicator_.setLedState(LED_INDEX_CALIBRATE, true);
			mPreview.takeCalibrationPicture( saveModeJPEG, saveModeRAW, mStorageDir );
			ledIndicator_.setLedState(LED_INDEX_CALIBRATE, false);
			logger.debug("Menu action: calibration: done");
			return true;
			
		case R.id.about_info:
			logger.debug("Menu action: about: begin");			            
            intent = new Intent(getApplicationContext(), SplashActivity.class);
			startActivity(intent);
			logger.debug("Menu action: about: done");
			return true;
			
		case R.id.itemCopyAssets: {
			logger.debug("Copy assets: begin");
			Thread t = new Thread(new Runnable() {
				
				@Override
				public void run() {
					copyTestAssets();
					logger.debug("Copy assets: done");
				}
			});
			t.run();
			return true;
		}
		/*
		case R.id.itemTest1: {
			
			intent = new Intent(getApplicationContext(), ImageActivity.class);
			intent.putExtra(ARG_WORKDIR, mStorageDir );
			File f = new File( mStorageDir );
			
			String foundFileName = "testimg";
			
			
			if ( foundFileName != null ) {
				intent.putExtra(ARG_IMAGE_PREFIX, foundFileName);
			}
			startActivity(intent);
			return true;
		}
		*/
		default:
			return super.onOptionsItemSelected(item);
		}
	}

}
