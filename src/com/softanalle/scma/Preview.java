package com.softanalle.scma;


/**
 * @author Tommi Rintala <t2r@iki.fi>
 */

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.PreviewCallback;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceView;
import android.view.SurfaceHolder;
import android.view.View;

public class Preview extends SurfaceView implements SurfaceHolder.Callback,
SurfaceView.OnClickListener {
	private final static String TAG = "Preview";
	
	private boolean isReady_ = false;

	SurfaceHolder mHolder;
	public Camera camera;
	Preview(Context context) {
		super(context);

		mHolder = getHolder();
		mHolder.addCallback(this);
		//mHolder.setType(SurfaceHolder.)
	}

	public void surfaceCreated(SurfaceHolder holder) {
		camera = Camera.open();
		try {
			camera.setPreviewDisplay(holder);
			camera.setPreviewCallback(new PreviewCallback() {
				public void onPreviewFrame(byte[] data, Camera arg1) {
					/*
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
					 */
					Log.d(TAG, "onPreviewFrame() - updated preview screen");
					Preview.this.invalidate();
				}
			});
			isReady_ = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void surfaceDestroyed(SurfaceHolder holder) {
		camera.stopPreview();
		camera.release();
		camera = null;
		isReady_ = false;
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int height, int width) {
		Camera.Parameters params = camera.getParameters();
		params.setPreviewSize(width,  height);
		camera.setParameters(params);
		camera.startPreview();
		isReady_ = true;
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
					outStream = new FileOutputStream(String.format("%s/SCM/%d.jpg", Environment.getExternalStorageDirectory().getPath(), System.currentTimeMillis()));
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
	
	public boolean isReady() {
		return isReady_;
	}
}
