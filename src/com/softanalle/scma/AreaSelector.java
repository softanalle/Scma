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

	private static final int INVALID_POINTER_ID = -1;

	// The ‘active pointer’ is the one currently moving our object.
	private int mActivePointerId = INVALID_POINTER_ID;
	private float mPosX;
    private float mPosY;
	private Paint mPaint;
	private float mLeft, mRight, mTop, mBottom;
	private int mWidth = 400;
	private int mHeight = 200;
	//private int mCenterX = 0;
	//private int mCenterY = 0;

	
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
	    
	    
		switch (action) {
	        case MotionEvent.ACTION_MOVE: {
	        	
	        	// Calculate the distance moved
	            final float dx = x - mPreviousX;
	            final float dy = y - mPreviousY;
	            
	            // Only move if the ScaleGestureDetector isn't processing a gesture.
	            if (!scaleGestureDetector.isInProgress()) {

	            	// Move the object
	            	mPosX += dx;
	            	mPosY += dy;

	            	// Remember this touch position for the next move event
	            	mPreviousX = x;
	            	mPreviousY = y;



	            	invalidate();
	            }
	            status = false;
	            break;
	        }
	            
	        case MotionEvent.ACTION_DOWN: {
	        	mPaint.setColor(Color.GREEN);
	        	mPreviousX = x;
	        	mPreviousY = y;
	        	
	        	// Save the ID of this pointer
	            mActivePointerId = e.getPointerId(0);

	        	// invalidate();
	        	status = true;
	        	break;
	        }
	        
	        case MotionEvent.ACTION_UP: {
	        	mPaint.setColor(Color.RED);
	            mActivePointerId = INVALID_POINTER_ID;	        	
	        	invalidate();
	        	status = false;
	        	break;
	        }
	        
	        case MotionEvent.ACTION_CANCEL: {
	            mActivePointerId = INVALID_POINTER_ID;
	            break;
	        }
	        
	        case MotionEvent.ACTION_POINTER_UP: {
	            // Extract the index of the pointer that left the touch sensor
	            final int pointerIndex = (action & MotionEvent.ACTION_POINTER_INDEX_MASK) 
	                    >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
	            final int pointerId = e.getPointerId(pointerIndex);
	            if (pointerId == mActivePointerId) {
	                // This was our active pointer going up. Choose a new
	                // active pointer and adjust accordingly.
	                final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
	                mPreviousX = e.getX(newPointerIndex);
	                mPreviousY = e.getY(newPointerIndex);
	                mActivePointerId = e.getPointerId(newPointerIndex);
	            }
	            break;
	        }
	    }

	    return status;
	}


/*
 * Scale gesture listener, at this point only scaling gesture is used. Perhaps others would
 * be nice, though?	
 */
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
