package com.softanalle.scma;


/**
 * @author Tommi Rintala <t2r@iki.fi>
 * @license GNU GPL 3.0
 * 
    Camera Preview controller for SCMA
 
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


//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.acl.LastOwnerException;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
import android.media.audiofx.BassBoost.Settings;
//import android.hardware.Camera.PictureCallback;
//import android.hardware.Camera.PreviewCallback;
//import android.os.Environment;
import android.util.Log;
import android.view.SurfaceView;
import android.view.SurfaceHolder;
import android.widget.Toast;
//import android.view.View;

public class Preview extends SurfaceView 
implements SurfaceHolder.Callback
/* ,
Camera.AutoFocusCallback
*/
/*,
SurfaceView.OnClickListener */ {
	private final static String TAG = "Preview";

	private boolean isPreview_ = false;
	private boolean isReady_ = false;

	private String mImageFilenameSuffix = "focus";
	
	// support option arrays
	private List<String> whiteBalanceModes_ = null;
	private List<Integer> supportedPictureFormats_ = null;

	
	SurfaceHolder mHolder;
	public Camera camera;
	
	/*
	 * @param context Control context
	 */
	Preview(Context context) {
		super(context);

		mHolder = getHolder();
		mHolder.addCallback(this);
		
		// mPreviewRectPaint.setColor(Color.RED);
		
		//mHolder.setType(SurfaceHolder.)
		//Log.d(TAG, "create OK");
	}

	/*
	 * Called when surface is created
	 * @see android.view.SurfaceHolder.Callback#surfaceCreated(android.view.SurfaceHolder)
	 */
	public void surfaceCreated(SurfaceHolder holder) {
		//Log.d(TAG, "surfaceCreated(holder)");
		if ( camera == null ) {
			Log.d(TAG, "-camera was null, opening");
			// camera = getCameraInstance();
			reclaimCamera();
		}
		if ( camera == null ) {
			Toast.makeText(getContext(), "Unable to obtain camera!", Toast.LENGTH_LONG).show();
		} else {
			try {

				camera.setPreviewDisplay(holder);
				
				isReady_ = true;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/*
	 * Called when surface is destroyed
	 * @see android.view.SurfaceHolder.Callback#surfaceDestroyed(android.view.SurfaceHolder)
	 */
	public void surfaceDestroyed(SurfaceHolder holder) {
		//Log.d(TAG, "surfaceDestroyed(holder)");
		if ( camera != null ) {
			Log.d(TAG, "-camera was not null, releasing");
			stopPreview();
			camera.release();
			isPreview_ = false;
		}
		camera = null;
		isReady_ = false;
	}

	
	/*
	 * on surface change event
	 * @see android.view.SurfaceHolder.Callback#surfaceChanged(android.view.SurfaceHolder, int, int, int)
	 */
	public void surfaceChanged(SurfaceHolder holder, int format, int height, int width) {

		if (mHolder.getSurface() == null){
			// preview surface does not exist
			return;
		}

		// Log.d(TAG, "surfaceChanged(holder)");
		if ( camera == null ) {
			Log.d(TAG, "-camera was null, opening");
			camera = getCameraInstance();
		}

		// stop preview before making changes
		try {
			stopPreview();
			isPreview_ = false;
		} catch (Exception e){
			// ignore: tried to stop a non-existent preview
		}



		try {
			if (camera != null) {


				Camera.Parameters params = camera.getParameters();
				params.setPreviewSize(width,  height);

				whiteBalanceModes_ = params.getSupportedWhiteBalance();
				supportedPictureFormats_ = params.getSupportedPictureFormats();

				params.set("rawsave-mode", "1");

				// no compression!
				params.setJpegQuality(100);

				List<Camera.Size> previewSizes = params.getSupportedPreviewSizes();

				for (Size p : previewSizes) {
					if ( p.width == 640 ) {
						params.setPreviewSize(p.width,  p.height);
						break;
					}
				}

				// turn autofocus off
				//params.setFocusMode(Parameters.FOCUS_MODE_FIXED);

				params.setFocusMode(Parameters.FOCUS_MODE_MACRO);

				if (supportedPictureFormats_.contains(ImageFormat.RGB_565)) {			
					params.setPictureFormat(ImageFormat.RGB_565);
				} else if ( supportedPictureFormats_.contains(ImageFormat.YV12)) {
					params.setPictureFormat(ImageFormat.YV12);
				} else {
					params.setPictureFormat(ImageFormat.NV21);
				}

				// turn flash off
				params.setFlashMode(Parameters.FLASH_MODE_OFF);

				if ( params.isAutoWhiteBalanceLockSupported()) {
					params.setAutoWhiteBalanceLock(true);
				} else {
					Toast.makeText(getContext(), "Unable to lock AutoWhiteBalance", Toast.LENGTH_LONG).show();
				}

				if ( params. isAutoExposureLockSupported() ) {
					params.setAutoExposureLock(true);
				} else {
					Toast.makeText(getContext(), "Unable to lock AutoExposure", Toast.LENGTH_LONG).show();
				}

				// we don't work with GPS data
				params.removeGpsData();

				camera.setParameters(params);
				startPreview();
				isReady_ = true;
			} else {
				Log.e(TAG, "-camera is still null, failed to retrieve");
			}
		} catch (Exception e) {
			Log.e(TAG, "Error starting camera preview: " + e.getMessage());
		}
	}

	
	/**
	 * Get the image filename suffix
	 * @return
	 */
	public String getImageFilenameSuffix() {
		return mImageFilenameSuffix;
	}

	/**
	 * Set the image filename suffix
	 * @param imageFilenameSuffix
	 */
	public void setImageFilenameSuffix(String imageFilenameSuffix) {
		mImageFilenameSuffix = imageFilenameSuffix;
	}

	
	/**
	 * Is the control ready 
	 * @return
	 */
	public boolean isReady() {
		return isReady_;
	}
	
	/** A safe way to get an instance of the Camera object. */
	public static Camera getCameraInstance(){
	    Camera c = null;
	    try {
	        c = Camera.open(); // attempt to get a Camera instance
	    }
	    catch (Exception e){
	        // Camera is not available (in use or does not exist)
	    }
	    return c; // returns null if camera is unavailable
	}
	
	/**
	 * Activate camera preview
	 */
	public void startPreview() {
		if ( camera != null && !isPreview_) {
			camera.startPreview();
			isPreview_ = true;
		}
	}

	/**
	 * Stop camera preview
	 */
	public void stopPreview() {
		if ( camera != null ) {
			camera.stopPreview();
			isPreview_ = false;
		}
	}
	
	public void doFocus() {
		
		//camera.cancelAutoFocus();
		camera.autoFocus(null);
		
	}

	/*
	@Override
	public void onAutoFocus(boolean success, Camera camera) {
		Toast.makeText(getContext(), "Focus complete", Toast.LENGTH_LONG).show();
		//
		
	}
*/
	/**
	 * @return the whiteBalanceModes_
	 */
	public List<String> getWhiteBalanceModes() {
		return whiteBalanceModes_;
	}
	
	//public List<Integer> getSupportedPictureFormats () {
	//	return supportedPictureFormats_;
	//}
	
	/**
	 * Save a byte vector to a file
	 * @param filename the name of file to write
	 * @param data byte vector of data
	 * @return success of operation
	 */
	private boolean writeImageToDisc(String filename, byte[] data) {
		//Log.d(TAG, "writeImageToDisc - begin");
		FileOutputStream outStream = null;
		try {			
			outStream = new FileOutputStream( filename );			
			outStream.write(data);
			outStream.close();
			//Log.d(TAG, "writeImageToDisc - wrote bytes: " + data.length);
			Toast.makeText(getContext(), filename + " - wrote bytes: " + data.length, Toast.LENGTH_SHORT).show();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} finally {
		}               
		//Log.d(TAG, "writeImageToDisc - complete");
		return true;
	}
	
	/**
	 * Take the calibration picture and save
	 * @param jpg
	 * @param raw
	 * @param storagePath
	 */
	public void takeCalibrationPicture(final boolean jpg, final boolean raw, final String storagePath) {
		PictureCallback jpegCallback = null;
		PictureCallback rawCallback = null;
		final String filename = storagePath + "/whiteref";
		startPreview();
		if ( jpg ) {
			jpegCallback = new PictureCallback() {

				private String mJpegFilename = filename;
				@Override public void onPictureTaken(byte[] data, Camera camera) {
					try {
						writeImageToDisc(mJpegFilename + ".JPG", data);					
						Thread.sleep(200);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						Toast.makeText(getContext(), "Error while saving JPEG file: " + e, Toast.LENGTH_LONG).show();
					}
				}
			};
		}
		
		if ( raw ) {			
			rawCallback = new PictureCallback() {
				private String mRawFilename = filename;
				@Override public void onPictureTaken(byte[] data, Camera camera) {
					try {
						if ( data != null && data.length > 0 ) {
						if (!writeImageToDisc(mRawFilename + ".RAW", data)) {
							Toast.makeText(getContext(), "Error while saving RAW file", Toast.LENGTH_LONG).show();
						}
					
						Thread.sleep(1000);
						} else {
							Toast.makeText(getContext(), "Got ZERO data for RAW image: " + mRawFilename, Toast.LENGTH_LONG).show();
						}
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						Toast.makeText(getContext(), "Error while saving RAW file: " + e, Toast.LENGTH_LONG).show();
					}
				}
			};

		}
		camera.takePicture(null,  rawCallback, null, jpegCallback);
		isPreview_ = false;
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		startPreview();
	}
	
	/**
	 * take picture and store JPEG and RAW images
	 * @param doJPEG store JPEG image
	 * @param doRAW store RAW image
	 * @param filename the full filename for image, without suffix (.jpg, .raw)
	 */
	public void takePicture(final boolean doJPEG, final boolean doRAW, final String filename) {
		PictureCallback jpegCallback = null;
		PictureCallback rawCallback = null;		
		if ( doJPEG ) {
			//startPreview();
			jpegCallback = new PictureCallback() {

				private String mJpegFilename = filename;
				@Override public void onPictureTaken(byte[] data, Camera camera) {
					try {
						writeImageToDisc(mJpegFilename + ".JPG", data);
					
						Thread.sleep(200);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						Toast.makeText(getContext(), "Error while saving JPEG file: " + e, Toast.LENGTH_LONG).show();
					}
				}
			};
		}
		
		if ( doRAW ) {
			if ( doJPEG) { startPreview(); }
			rawCallback = new PictureCallback() {
				private String mRawFilename = filename;
				@Override public void onPictureTaken(byte[] data, Camera camera) {
					try {
						if ( data != null && data.length > 0 ) {
						if (!writeImageToDisc(mRawFilename + ".RAW", data)) {
							Toast.makeText(getContext(), "Error while saving RAW file", Toast.LENGTH_LONG).show();
						}
					
						Thread.sleep(1000);
						} else {
							Toast.makeText(getContext(), "Got ZERO data for RAW image: " + mRawFilename, Toast.LENGTH_LONG).show();
						}
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						Toast.makeText(getContext(), "Error while saving RAW file: " + e, Toast.LENGTH_LONG).show();
					}
				}
			};
		}
		//Log.i(TAG, "Take JPEG (" + doJPEG + ") and RAW (" + doRAW + ")");
		
		camera.takePicture(null,  rawCallback, jpegCallback);
		isPreview_ = false;
		//Log.i(TAG, "done: " + filename);
	}
	
	/**
	 * ShutterCallback - if we wish to accomplish something on shutter click, we do it here.
	 * remember to activate call in takePicture() -function
	 */

	public ShutterCallback shutterCallback = new ShutterCallback() {
		public void onShutter() {
			//Log.d(TAG, "onShutter");
		}
	};

	
	
	/**
	 * release camera handler, called on application onResume()
	 */
	public void releaseCamera() {
		if ( camera != null ) {
			camera.release();
			camera = null;
		}
	}
	
	/**
	 * reclaim camera, used usually on application onPause()
	 */
	public Camera reclaimCamera() {
		if ( camera == null ) {
			camera = getCameraInstance();
			if ( camera == null ) {
				Toast.makeText(getContext(), "Unable to obtain camera", Toast.LENGTH_LONG).show();
			}
		}
		return camera;
	}
	
}
