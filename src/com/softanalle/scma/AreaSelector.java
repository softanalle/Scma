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
	
	public AreaSelector(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}
	public AreaSelector(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	public AreaSelector(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	private Paint mPaint;
	private float mLeft, mRight, mTop, mBottom;
	private int mWidth = 0;
	private int mHeight = 0;
	
	private void initComponent() {
		mPaint = new Paint();
		mPaint.setColor(Color.RED);
	}
	
	@Override
	public void onDraw(Canvas canvas) {
		super.draw(canvas);
		Log.d(TAG, "onDraw()");
		
		float left = (float) (0.25 * mWidth);
		float right = (float) (0.75 * mWidth);
		float top = (float) (0.25 * mHeight);
		float bottom = (float) (0.75 * mHeight);
				
		canvas.drawRect(left, top, right, bottom, mPaint);
	}
			
	
	@Override
	public void onLayout(boolean changed, int left, int top, int right, int bottom) {
		Log.d(TAG, "onLayout(" + changed + "," + left + "," + top + "," + right + "," + bottom + ")");
		int mWidth = right-left;
		int mHeight = bottom-top;				
	}
	
}
