package com.softanalle.scmctrls;

import android.view.View;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.util.AttributeSet;
import android.util.Log;
// import android.widget.TextView;

public class LedIndicator extends View {
	private boolean isInitialized = false;
	private static String TAG = "LedIndicator";
	private int[] mRectColors = {
			Color.WHITE,
			Color.RED,
			Color.GREEN,
			Color.BLUE,
			Color.BLACK,
			Color.MAGENTA,
			Color.YELLOW,
			Color.CYAN
	};
	public static final int LED_WHITE = 0;
	public static final int LED_RED = 1;
	public static final int LED_GREEN = 2;
	public static final int LED_BLUE = 3;
	public static final int LED_NIR = 4;
	public static final int LED_MAGENTA = 5;

	protected static final int LED_FOCUS = 6;
	protected static final int LED_CALIBRATE = 7;

	private int mOffColor = Color.LTGRAY;

	private final static int mLedCount = 8;

	public LedIndicator(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		if (!isInitialized) initializeFirstTime();
	}
	public LedIndicator(Context context, AttributeSet attrs) {
		super(context, attrs);
		if (!isInitialized) initializeFirstTime();
	}

	public LedIndicator(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		if (!isInitialized) initializeFirstTime();
	}


	private Rect[] mLedArea;
	private ShapeDrawable[] mLeds;
	private boolean[] mActivity;
	private int mLeft = 0;
	private int mTop = 0;
	private int mHeight = 20;
	private int mWidth = 24;
	private boolean isReady;

	private void initializeFirstTime() {
		Log.d(TAG, "initializeFirstTime()");
		if (isReady) {
			Log.d(TAG, "(isReady)");
			return; // no new initialization
		}
		isInitialized = true;
		try {


			mActivity = new boolean[mLedCount];
			mLeds = new ShapeDrawable[mLedCount];
			mLedArea = new Rect[mLedCount];
			for (int i=0; i<mLedCount; i++) {
				mActivity[i] = false;
				mLeds[i] = new ShapeDrawable(new RectShape());
				//mLeds[i] = new ShapeDrawable(new OvalShape());
				// mLeds[i].getPaint().setColor(mRectColors[i]);
				mLeds[i].getPaint().setColor(mOffColor);
				int x = mLeft + 2 + i * mWidth;
				int y = mTop + 2 ;
				mLeds[i].setBounds(x, y, x + mWidth, y + mHeight);
				mLedArea[i] = new Rect(x, y, x + mWidth, y+mHeight);
			}
		} catch (Exception ex) {
			Log.e(TAG, "Error while building leds");
		} finally {
			isReady = true;
		}
	}

	@Override
	public void onLayout(boolean changed, int left, int top, int rigth, int bottom) {
		Log.d(TAG, "onLayout(" + changed + "," + left + "," + top + "," + rigth + "," + bottom + ")");          
		if (!isInitialized) initializeFirstTime();      

		if ( changed ) {
			mHeight = (int) (bottom - top) - 4;
			mWidth = mHeight;
			for (int i=0;i<mLedCount;i++) {
				int x = mLeft + i * 2 + i * mWidth;
				int y = mTop + 2;
				Log.d(TAG, "setPos:" + x + ", " + y + ", " + mWidth + ", " + mHeight);
				mLedArea[i].left = x;
				mLedArea[i].top = y;
				mLedArea[i].bottom = y+mHeight;
				mLedArea[i].right = x+mWidth;                           
				if (mLeds[i] != null)
					mLeds[i].setBounds(mLedArea[i]);
			}
		}
	}

	@Override
	public void onDraw(Canvas canvas) {
		if (isReady) {
			for (int i=0;i<mLedCount;i++) {
				if(mLeds[i] != null && canvas != null)
					mLeds[i].draw(canvas);
			}
		}
		invalidate();
	}

	public void turnOn(int index) {
		Log.d(TAG, "turnOn(" + index + ")");
		if (index >= 0 && index < mLedCount) {
			mActivity[index] = true;
			mLeds[index].getPaint().setColor(mRectColors[index]);
		}
		// invalidate led area
		invalidate(mLedArea[index]);
	}

	public void turnOff(int index) {
		Log.d(TAG, "turnOff(" + index + ")");
		if ( index >= 0 && index < mLedCount) {
			mActivity[index] = false;
			mLeds[index].getPaint().setColor(mOffColor);
		}
		// invalidate led area
		invalidate(mLedArea[index]);
	}
}



