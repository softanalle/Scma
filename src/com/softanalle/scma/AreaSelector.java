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
	
	public AreaSelector(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		initComponent();
	}
	public AreaSelector(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		initComponent();
	}
	public AreaSelector(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		initComponent();
	}
	
	private Paint mPaint;
	private float mLeft, mRight, mTop, mBottom;
	private int mWidth = 0;
	private int mHeight = 0;
	private int mCenterX = 0;
	private int mCenterY = 0;
	
	private void initComponent() {
		mPaint = new Paint();
		mPaint.setColor(Color.RED);
		mInitialized = true;
	}
	
	@Override
	public void onDraw(Canvas canvas) {
		// super.draw(canvas);
		Log.d(TAG, "onDraw()");
		
		if ( mInitialized && canvas != null ) {
			float left = (float) (0.25 * mWidth);
			float right = (float) (0.75 * mWidth);
			float top = (float) (0.25 * mHeight);
			float bottom = (float) (0.75 * mHeight);

			canvas.drawRect(left, top, right, bottom, mPaint);
		}
	}
			
	
	@Override
	public void onLayout(boolean changed, int left, int top, int right, int bottom) {
		Log.d(TAG, "onLayout(" + changed + "," + left + "," + top + "," + right + "," + bottom + ")");
		if ( !mInitialized || changed ) {
			int mWidth = right-left;
			int mHeight = bottom-top;
			mTop = top;
			mBottom = bottom;
			mLeft = left;
			mRight = right;
		}
	}

	
	public int getCenterX() {
		return mCenterX;
	}
	
	public int getCenterY() {
		return mCenterY;
	}
	
	public void setWidth(int w) {
		mWidth = w;
		mLeft = mCenterX - (int) (w / 2);
		mRight = mCenterX + (int) (w / 2);
	}
	public void setHeight(int h) {
		mHeight = h;
	}
}
