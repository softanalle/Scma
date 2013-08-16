/**
 * 
 */
package com.softanalle.scma;

/**
 * @author Tommi Rintala <t2r@iki.fi>
 * @license GNU GPL 3.0
 * 
    Image Preview Drawer Activity for SCMA
 
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


import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
//import android.graphics.Color;
//import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author t2r
 *
 */
public class ImageActivity extends Activity {
	
	private static final String TAG = "ImageActivity";
	private TextView nameLabel_;
	private Button closeButton_, approveButton_, resetButton_;
	private AreaSelector areaSelector_;
	private ImageView imageView_;
	
	private String mImagePrefix;
	private String mWorkdir;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {        
		setTheme(android.R.style.Theme_Black_NoTitleBar_Fullscreen);
		super.onCreate(savedInstanceState);        
		
		//setTheme(android.R.style.Theme_Black_NoTitleBar_Fullscreen);
		setContentView(R.layout.activity_image);

		Intent intent = getIntent();
		
		mImagePrefix = intent.getStringExtra( MainActivity.ARG_IMAGE_PREFIX );
		mWorkdir = intent.getStringExtra( MainActivity.ARG_WORKDIR );
		
		//String image_prefix = savedInstanceState.getString(MainActivity.ARG_IMAGE_PREFIX);
		//String workingDir = savedInstanceState.getString(MainActivity.ARG_WORKDIR);
		if ( mImagePrefix == null ) {
			mImagePrefix = "[name missing]";
		}
		if ( mWorkdir == null ) {
			mWorkdir = "[path missing]";
		}
		nameLabel_ = (TextView) findViewById(R.id.textImageTitle);
		//pictureButton_  = (Button) findViewById(R.id.pictureButton);
		nameLabel_.setText(mWorkdir + "/" + mImagePrefix);

		closeButton_ = (Button) findViewById(R.id.buttonClose);
		closeButton_.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = getIntent();
				setResult(MainActivity.RESULT_IMAGE_ACTIVITY, intent);
                //close this Activity...
                finish();
			}
		});
		
		approveButton_ = (Button) findViewById(R.id.buttonAcceptImage);
		imageView_ = (ImageView) findViewById(R.id.imageView1);
		resetButton_ = (Button) findViewById(R.id.buttonResetCtrl);
		areaSelector_ = (AreaSelector) findViewById(R.id.areaSelector1);
		
		// controller Z-order
		areaSelector_.bringToFront();
		approveButton_.bringToFront();
		closeButton_.bringToFront();
		resetButton_.bringToFront();
		
		// loading of large image might take some time...
		Thread t = new Thread(new Runnable() {
			final String wd = mWorkdir;
			final String pr = mImagePrefix;
			@Override
			public void run() {
				// TODO Auto-generated method stub
				showImage(wd, pr);		
			}
		});
		t.run();
				
		// Toast.makeText(getApplicationContext(), "Implement here image loading", Toast.LENGTH_LONG).show();
		// Toast.makeText(getApplicationContext(), "Implement here image manipulation", Toast.LENGTH_LONG).show();
	}

	private Matrix mScaleMatrix = null;
	private int mHeight, mWidth;
	
	public Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {
		 
		int width = bm.getWidth();		 
		int height = bm.getHeight();
		 
		float scaleWidth = ((float) newWidth) / width;		 
		float scaleHeight = ((float) newHeight) / height;
		 
		// create a matrix for the manipulation		 
		mScaleMatrix = new Matrix();
		 
		// resize the bit map		 
		mScaleMatrix.postScale(scaleWidth, scaleHeight);
		 
		// recreate the new Bitmap		 
		Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, mScaleMatrix, false);
		 
		return resizedBitmap;
		 
		}
	
	
	
	private void showImage(String workdir, String filename) {
		String fullname = workdir + "/" + filename;
		mWidth = imageView_.getWidth();
		mHeight = imageView_.getHeight();
		
		if ( ! filename.contains("_white")) {
			filename = filename + "_white.jpg";
		}
		
		File f = new File(fullname);		
		if (f.exists()) {
			Bitmap tmp = BitmapFactory.decodeFile(fullname);
			imageView_.setImageBitmap(getResizedBitmap(tmp, mHeight, mWidth));
			
			// imageView_.setImageBitmap();
			//Drawable d = Drawable.createFromPath(fullname);			
			//imageView_.setImageDrawable(d);
			
			
		} else {
			Toast.makeText(getApplicationContext(), "Image '" + fullname + "' load failed", Toast.LENGTH_LONG).show();
			Log.e(TAG, "Image '" + fullname + "' load failed");
		}
	}
	
	//ImageView imageViewer_;
	//private Paint mPaint;
	/*
	protected void onDraw(Canvas canvas) {
		   super.onDraw(canvas);

		   // Draw the shadow
		   canvas.drawRect(left, top, right, bottom, paint);
		   );
	}
	*/
}
