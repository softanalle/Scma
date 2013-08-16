/**
 * 
 */
package com.softanalle.scma;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

/**
 * @author Tommi Rintala <t2r@iki.fi>
 * @license GNU GPL 3.0
 * 
    Area selector component for SCMA
 
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

/**
 * @author t2r
 *
 */
public class AreaSelector extends View {

	private final static String TAG = "AreaSelector";
	private boolean mInitialized = false;

	// private Drawable image;
	private float scaleFactor = 1.0f;
	private ScaleGestureDetector scaleGestureDetector;

	
	public AreaSelector(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		initComponent( context );
	}
	public AreaSelector(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		initComponent( context );
	}
	public AreaSelector(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		initComponent( context );
	}
	
	private Paint mPaint;
	private float mLeft, mRight, mTop, mBottom;
	private int mWidth = 400;
	private int mHeight = 200;
	//private int mCenterX = 0;
	//private int mCenterY = 0;
	
	private void initComponent(Context context) {
		setFocusable(true);
		
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setColor(Color.RED);
		mPaint.setStyle(Paint.Style.STROKE);
	    mPaint.setStrokeJoin(Paint.Join.ROUND);
		mPaint.setStrokeWidth(6f);
		
		scaleGestureDetector = new ScaleGestureDetector(context, new ScaleListener());
		
		mInitialized = true;
		invalidate();
	}
	
	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		Log.d(TAG, "onDraw()");
		
		if ( mInitialized && canvas != null ) {
			float left = (float) (mPreviousX - mWidth / 2 * scaleFactor);
			float right = (float)  (mPreviousX + mWidth / 2 * scaleFactor);
			float top = (float) (mPreviousY - mHeight / 2 * scaleFactor);
			float bottom = (float) (mPreviousY + mHeight / 2 * scaleFactor);			
			
			canvas.drawLine(left,  top,  right,  top,  mPaint);
			canvas.drawLine(right,  top,  right,  bottom,  mPaint);
			canvas.drawLine(right, bottom, left, bottom, mPaint);
			canvas.drawLine(left, bottom, left, top, mPaint);
						
		}
	}
			
	
	@Override
	public void onLayout(boolean changed, int left, int top, int right, int bottom) {
		Log.d(TAG, "onLayout(" + changed + "," + left + "," + top + "," + right + "," + bottom + ")");
		//if ( changed ) {
			mWidth = (int) ((right-left) *  .5);
			mHeight = (int) ((bottom-top) * .5);
			mTop = top;
			mBottom = bottom;
			mLeft = left;
			mRight = right;
		//}
	}

    private float mPreviousX;
	private float mPreviousY;
	
	@Override
	public boolean onTouchEvent(MotionEvent e) {
		boolean status = false;
	    // MotionEvent reports input details from the touch screen
	    // and other input controls. In this case, you are only
	    // interested in events where the touch position changed.

		scaleGestureDetector.onTouchEvent( e );
		
		
	    float x = e.getX();
	    float y = e.getY();

	    int action = e.getAction() & MotionEvent.ACTION_MASK;
        int pointerIndex = (e.getAction() & MotionEvent.ACTION_POINTER_ID_MASK) >> MotionEvent.ACTION_POINTER_ID_SHIFT;
        int pointerId = e.getPointerId(pointerIndex);

	    
	    
	    
		switch (action) {
	        case MotionEvent.ACTION_MOVE:

	        	/*
	            float dx = x - mPreviousX;
	            float dy = y - mPreviousY;

	            // reverse direction of rotation above the mid-line
	            if (y > getHeight() / 2) {
	              dx = dx * -1 ;
	            }

	            // reverse direction of rotation to left of the mid-line
	            if (x < getWidth() / 2) {
	              dy = dy * -1 ;
	            }
*/
	            //mRenderer.mAngle += (dx + dy) * TOUCH_SCALE_FACTOR;  // = 180.0f / 320
	            //requestRender();
	        	status = true;
	            break;
	        case MotionEvent.ACTION_DOWN:
	        	mPaint.setColor(Color.GREEN);
	        	invalidate();
	        	status = true;
	        	break;
	        case MotionEvent.ACTION_UP:
	        	mPaint.setColor(Color.RED);
	        	invalidate();
	        	status = false;
	        	break;
	        	
	    }

	    
	    mPreviousX = x;
	    mPreviousY = y;
	    return status;
	}

	private class ScaleListener extends
	ScaleGestureDetector.SimpleOnScaleGestureListener {
		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			scaleFactor *= detector.getScaleFactor();

			// Don't let the object get too small or too large.
			scaleFactor = Math.max(0.1f, Math.min(scaleFactor, 5.0f));

			invalidate();
			return true;
		}
}
	
	// getters / setters
	
	/*
	 * 
	 */
	public float getCenterX() {
		return mPreviousX;
	}
	
	/*
	 * 
	 */
	public float getCenterY() {
		return mPreviousY;
	}
	
	/*
	 * 
	 */
	public void setWidth(int w) {
		mWidth = w;
		mLeft = mPreviousX - (int) (w / 2);
		mRight = mPreviousY + (int) (w / 2);
	}
	
	/*
	 * 
	 */
	public void setHeight(int h) {
		mHeight = h;
	}
}
