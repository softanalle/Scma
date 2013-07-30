package com.softanalle.scma;


/**
 * @author Tommi Rintala <t2r@iki.fi>
 */


//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Camera;
//import android.hardware.Camera.PictureCallback;
//import android.hardware.Camera.PreviewCallback;
//import android.os.Environment;
import android.util.Log;
import android.view.SurfaceView;
import android.view.SurfaceHolder;
//import android.view.View;

public class Preview extends SurfaceView implements SurfaceHolder.Callback /*,
SurfaceView.OnClickListener */ {
	private final static String TAG = "Preview";

	private boolean isReady_ = false;

	SurfaceHolder mHolder;
	public Camera camera;
	
	Preview(Context context) {
		super(context);

		mHolder = getHolder();
		mHolder.addCallback(this);
		//mHolder.setType(SurfaceHolder.)
		Log.d(TAG, "create OK");
	}

	public void surfaceCreated(SurfaceHolder holder) {
		Log.d(TAG, "surfaceCreated(holder)");
		if ( camera == null ) {
			Log.d(TAG, "-camera was null, opening");
			camera = getCameraInstance();
		}
		
		try {
			
			camera.setPreviewDisplay(holder);
			// camera.setDisplayOrientation(0);
			/*
			camera.setPreviewCallback(new PreviewCallback() {
				public void onPreviewFrame(byte[] data, Camera arg1) {
					/ *
					FileOutputStream outStream = null;
					try {
						outStream = new FileOutputStream(String.format("%s/SCM/%d.jpg", Environment.getExternalStorageDirectory().getPath(), System.currentTimeMillis()));
						outStream.write(data);
						outStream.close();
						Log.d(TAG, "onPreviewFrame - wrote bytes: " + data.length);
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					} finally {
					}
					 * /
					Log.d(TAG, "onPreviewFrame() - updated preview screen");
					// TODO: commented this out, should it be like this?
					// Preview.this.invalidate();
				}
			});
			*/
			// camera.startPreview();
			isReady_ = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.d(TAG, "surfaceDestroyed(holder)");
		if ( camera != null ) {
			Log.d(TAG, "-camera was not null, releasing");
			camera.stopPreview();
			camera.release();
		}
		camera = null;
		isReady_ = false;
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int height, int width) {

		if (mHolder.getSurface() == null){
	          // preview surface does not exist
	          return;
	        }

		Log.d(TAG, "surfaceChanged(holder)");
		if ( camera == null ) {
			Log.d(TAG, "-camera was null, opening");
			camera = getCameraInstance();
		}
		
		// stop preview before making changes
        try {
            camera.stopPreview();
        } catch (Exception e){
          // ignore: tried to stop a non-existent preview
        }

		try {
		if (camera != null) {
			Camera.Parameters params = camera.getParameters();
			params.setPreviewSize(width,  height);
			camera.setParameters(params);
			camera.startPreview();
			isReady_ = true;
		} else {
			Log.d(TAG, "-camera is still null, failed to retrieve");
		}
		}catch (Exception e) {
			Log.d(TAG, "Error starting camera preview: " + e.getMessage());
		}
	}

	
	@Override
	public void onDraw(Canvas canvas) {
		super.draw(canvas);
		
		canvas.drawColor(Color.WHITE);

		Paint p = new Paint(Color.RED);
		p.setTextSize(canvas.getHeight() / 10);
		Log.d(TAG, "draw");
		canvas.drawText("PREVIEW",  canvas.getWidth() / 2,  canvas.getHeight() / 2, p);
	}


	private String mImageFilenameSuffix = "focus";
	
	public String getImageFilenameSuffix() {
		return mImageFilenameSuffix;
	}

	public void setImageFilenameSuffix(String imageFilenameSuffix) {
		mImageFilenameSuffix = imageFilenameSuffix;
	}

	
	/*
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		Log.d(TAG, "onClick()");
		camera.takePicture(null, null, 
				new PictureCallback() {


			public void onPictureTaken(byte[] data, Camera camera) {
				// TODO Auto-generated method stub
				FileOutputStream outStream = null;
				try {
					outStream = new FileOutputStream(String.format("%s/SCM/P_%d_%s.jpg", 
							Environment.getExternalStorageDirectory().getPath(), System.currentTimeMillis(), mImageFilenameSuffix));
					outStream.write(data);
					outStream.close();
					Log.d(TAG, "saveJpegCallback - wrote bytes: " + data.length);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
				}

			}
		});

	}
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
	
	public void startPreview() {
		if ( camera != null ) {
			camera.startPreview();
		}
	}
}
