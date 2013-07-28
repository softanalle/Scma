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
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
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
        
        private String mCurrentFileMode = FILEMODE_JPG;
        
        protected String mImageSuffix = "focus";
        
        private static final int defaultPulseWidth = 1000;
        
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
                toggleButton_ = (ToggleButton) findViewById(R.id.ToggleButton);

                ledIndicator_ = (LedIndicator) findViewById(R.id.ledIndicator1);

                // camera stuff
                mPreview = new Preview(this);
                //FrameLayout tmp = (FrameLayout) findViewById(R.id.RootFrame);
                RelativeLayout tmp = (RelativeLayout) findViewById(R.layout.activity_main);
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

                // Display the fragment as the main content.
                /*
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
*/
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        //String syncConnPref = sharedPref.getString(SettingsActivity.KEY_PREF_SYNC_CONN, "");
                
        pulseWidthFocus_ = sharedPref.getInt(SETTINGS_CHANNEL_FOCUS, defaultPulseWidth);
        
        pulseWidth1_ = sharedPref.getInt(SETTINGS_CHANNEL0, defaultPulseWidth);
        pulseWidth2_ = sharedPref.getInt(SETTINGS_CHANNEL1, defaultPulseWidth);
        pulseWidth3_ = sharedPref.getInt(SETTINGS_CHANNEL2, defaultPulseWidth);
        
                
                enableUi(false);

    }

private boolean powerState_ = false;
        
        private void powerLedsOn() {
                pulseWidth1_ = defaultPulseWidth;
                pulseWidth2_ = defaultPulseWidth;
                pulseWidth3_ = defaultPulseWidth;
                
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



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
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


        private static String mImagePrefix = "focus";
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
        @Override
        protected void onResume() {
            super.onResume();
            /*
            getPreferenceScreen().getSharedPreferences()
                    .registerOnSharedPreferenceChangeListener(this);
                    */
        }

        /*
        private Preference getPreferenceScreen() {
                // TODO Auto-generated method stub
                return null;
        }
        */
        
        @Override
        protected void onPause() {
            super.onPause();
            /*
            getPreferenceScreen().getSharedPreferences()
                    .unregisterOnSharedPreferenceChangeListener(this);
                    */
        }

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
