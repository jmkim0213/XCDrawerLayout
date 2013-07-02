package net.xenix.lib.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ListView;

/**
 * 
 * @author Kim Young Soo (yskim6217@gmail.com)
 * @date 2013. 07. 02.
 * @version 1.0
 */
public class XCDrawerLayout extends FrameLayout {
	private final static int SHADOW_DEFAULT_WIDTH = 10;

	private final static int STATUS_OPEN = 0x00;
	private final static int STATUS_CLOSE = 0x01;
	
	/**
	 * X값이 Y값보다 높아야한다. (주의!!)
	 */
	private int TOUCH_SENSING_VALUE_X = 6;
	private int TOUCH_SENSING_VALUE_Y = 5;
	
	// Status 
	private int mDrawerStatus = STATUS_CLOSE;
	
	private Handler mSmoothScrollHandler;
	
	// Touch Value
	private float mBeginRawX;
	private float mLastRawX;
	private float mBeginRawY;
	
	
	// 
	private boolean mViewVerticalSliding;
	private boolean mViewHorizonSliding;
	
	private Rect mDrawerEventRect;
	private Rect mTouchEventRect;
	
	//
	private int mDrawerMaxScrollX;
	
	// 
	private boolean mTouchFullRangeMode;
	
	// Dim
	private float mDimMaxValue;
	private Paint mDimPaint;
	
	// Shadow
	private Drawable mShadowDrawable;
	
	// View
	private View mDrawerView;
	private FrameLayout mDrawerViewLayout;
	
	
	// Listener
	private OnDrawerStatusChangeListener mOnDrawerStatusChangeListener;
	
	public XCDrawerLayout(Context context) {
		super(context);
		init(context);
	}

	public XCDrawerLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
		
	}

	public XCDrawerLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}
	
	private void init(Context context) {
		float density = context.getResources().getDisplayMetrics().density;
		
		TOUCH_SENSING_VALUE_X *= density;
		TOUCH_SENSING_VALUE_Y *= density;
		
		Log.d("LOG", "TOUCH_SENSING_VALUE_X: " + TOUCH_SENSING_VALUE_X);
		Log.d("LOG", "TOUCH_SENSING_VALUE_Y: " + TOUCH_SENSING_VALUE_Y);
		
		mSmoothScrollHandler = new Handler();
		mDrawerViewLayout = new FrameLayout(context);
		
		DisplayMetrics dm = context.getResources().getDisplayMetrics();
		mDrawerEventRect = new Rect(0, 0, dm.widthPixels, dm.heightPixels);
		mTouchEventRect = new Rect();
		
		
		mDimMaxValue = 1.0F;
		mDimPaint = new Paint();
		mDimPaint.setColor(Color.BLACK);
		
		
		int[] colors = new int[2];
		float increaseValue = 255.F / 2F;
		for ( int i = 0; i < 2; i++ ) {
			colors[i] = Color.argb((int)(i * increaseValue), 0, 0, 0);
		}
		
		mShadowDrawable = new GradientDrawable(Orientation.LEFT_RIGHT, colors);
		addView(mDrawerViewLayout, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
	}
	
	
	/**********************/
	/****   Override   ****/
	/**********************/
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int childCount = getChildCount();
		for ( int i = 0; i < childCount; i++ ) {
			View view = getChildAt(i);
			if ( !view.equals(mDrawerViewLayout) ) {
				mDrawerMaxScrollX = Math.max(mDrawerMaxScrollX, view.getRight());
				view.setClickable(true);
			}
		}
		
		bringChildToFront(mDrawerViewLayout);
	}
	
	@Override
	protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);
		handleDispatchDraw(canvas);
	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if ( !mSmoothXScrolling ) {
			handleDispatchTouchEvent(ev);
		}
		

		return super.dispatchTouchEvent(ev);
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if ( mViewHorizonSliding ) {
			return true;
		}
		else {
			float currentRawX = ev.getRawX();
			float currentRawY = ev.getRawY();
			
			int currentScrollX = mDrawerViewLayout.getScrollX();
			int touchRightLimit = -currentScrollX + mDrawerEventRect.right + mDrawerEventRect.left;
			int touchLeftLimit  = -currentScrollX + mDrawerEventRect.left;
			int touchTopLimit = mDrawerEventRect.top;
			int touchBottomLimit = mDrawerEventRect.bottom;
			
		
			mTouchEventRect.set(touchLeftLimit, touchTopLimit, touchRightLimit, touchBottomLimit);
			
			if ( mDrawerStatus == STATUS_OPEN  ) {				
				if (mTouchEventRect.contains((int)currentRawX, (int)currentRawY)) {
					if (ev.getAction() == MotionEvent.ACTION_UP) {
						closeDrawerView();
						return true;
					}
					
					else if (mViewVerticalSliding) {
						return true;
					} 
				}	
			}
		}
		
		
			
		return super.onInterceptTouchEvent(ev);
	}

	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		handleOnTouchEvent(event);
		return super.onTouchEvent(event);
	}

	
	
	/**********************/
	/****    Public    ****/
	/**********************/
	public void setTouchFullRangeMode(boolean enable) {
		mTouchFullRangeMode = enable;
	} 
	
	public void setShadowDrawable(Drawable drawable) {
		mShadowDrawable = drawable;	
	}
	
	public void setDimMaxValue(float value) {
		if ( !mViewHorizonSliding ) {
			if ( mDimMaxValue != value ) {
				mDimMaxValue = value;
			}
		}
	}
	
	public void setOnDrawerStatusChangeListener(OnDrawerStatusChangeListener listener) {
		mOnDrawerStatusChangeListener = listener;
	}
	
	
	public void setDrawerView(View drawerView) {
		if ( mDrawerView != null && !mDrawerView.equals(drawerView) ) {
			mDrawerViewLayout.removeView(mDrawerView);
			mDrawerView = null;
		}
		
		mDrawerViewLayout.addView(drawerView);
		
		bringChildToFront(mDrawerViewLayout);
		drawerView.setClickable(true);
		mDrawerView = drawerView;
	}
	
	public void setDrawerViewInChild(int viewId) {
		if ( mDrawerView != null ) {
			mDrawerViewLayout.removeView(mDrawerView);
			mDrawerView = null;
		}
		View drawerView = findViewById(viewId);
		removeView(drawerView);
		mDrawerViewLayout.addView(drawerView);
		
		bringChildToFront(mDrawerViewLayout);
		drawerView.setClickable(true);
		mDrawerView = drawerView;
	}
	

	
	public void openDrawerView() {
		if ( mDrawerMaxScrollX == 0 ) {
			throw new IllegalStateException("DrawerView is no size");
		}
		
		int newDrawerStatus = STATUS_OPEN;
		smoothScrollX(-mDrawerMaxScrollX);
		
		if ( newDrawerStatus != mDrawerStatus ) {
			mDrawerStatus = newDrawerStatus;

			if ( mOnDrawerStatusChangeListener != null ) {
				mOnDrawerStatusChangeListener.onDrawerStatusChange(newDrawerStatus);
			}
		}
	}


	public void closeDrawerView() {
		if ( mDrawerMaxScrollX == 0 ) {
			throw new IllegalStateException("DrawerView is no size");
		}
		
		int newDrawerStatus = STATUS_CLOSE;
		smoothScrollX(0);
		
		if ( newDrawerStatus != mDrawerStatus ) {
			mDrawerStatus = newDrawerStatus;
			
			if ( mOnDrawerStatusChangeListener != null ) {
				mOnDrawerStatusChangeListener.onDrawerStatusChange(newDrawerStatus);
			}
		}
	}
	
	public boolean isOpenDrawerView() {
		return mDrawerStatus == STATUS_OPEN;
	}
	
	
	/***********************/
	/****    Private    ****/
	/***********************/
	// TODO Handle Override
	private void handleDispatchDraw(Canvas canvas) {
		float scrollX = -mDrawerViewLayout.getScrollX();
		if ( 0 < scrollX ) {
			int viewHeight = getHeight();

			// 그림자 그리기
			if ( mShadowDrawable != null ) {
				int shadowWidth = mShadowDrawable.getBounds().left;
				
				if ( shadowWidth == 0 ) {
					shadowWidth = SHADOW_DEFAULT_WIDTH;
				}
				
				canvas.save();
				canvas.translate(scrollX - shadowWidth, 0);
				
				mShadowDrawable.setBounds(0, 0, shadowWidth, viewHeight);
				mShadowDrawable.draw(canvas);
				
				canvas.restore();
			}
			
			// Dim 그리기
			float maxScrollX = mDrawerMaxScrollX;
			float dimMaxValue = mDimMaxValue;
			
			float scrollRatio = scrollX / maxScrollX;
			float currentDimValue = dimMaxValue - (dimMaxValue * scrollRatio);
			
			int alpha = (int)(currentDimValue * 255);
			if ( alpha > 0 ) {
				mDimPaint.setAlpha(alpha);
				canvas.drawRect(0, 0, scrollX, viewHeight, mDimPaint);
			}
		}
	}
	
	
	private void handleDispatchTouchEvent(MotionEvent event) {
		
		
		float currentRawX = event.getRawX();
		float currentRawY = event.getRawY();
		
		int currentScrollX = mDrawerViewLayout.getScrollX();
		int touchRightLimit = mTouchFullRangeMode ? getWidth() : -currentScrollX + mDrawerEventRect.right + mDrawerEventRect.left;
		int touchLeftLimit  = mTouchFullRangeMode ? 0 : -currentScrollX + mDrawerEventRect.left;
		int touchTopLimit = mTouchFullRangeMode ? 0 : mDrawerEventRect.top;
		int touchBottomLimit = mTouchFullRangeMode ? getHeight() : mDrawerEventRect.bottom;
		
		mTouchEventRect.set(touchLeftLimit, touchTopLimit, touchRightLimit, touchBottomLimit);
		
		if ( mTouchEventRect.contains((int)currentRawX, (int)currentRawY) ) { 			
			switch ( event.getAction() ) {
			case MotionEvent.ACTION_DOWN:
				handleActionDown(event);
				break;

			case MotionEvent.ACTION_MOVE:
				handleActionMove(event);
				break;
			}	
		}
	}
	
	private void handleOnTouchEvent(MotionEvent event) {
		switch ( event.getAction() ) {
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_OUTSIDE:
		case MotionEvent.ACTION_UP:
			handleActionUp(event);
		}
	}
	
	
	// TODO Handle EventAction
	private void handleActionDown(MotionEvent event) {
		float currentRawX = event.getRawX();
		float currentRawY = event.getRawY();
		
		mBeginRawX = mLastRawX = currentRawX;
		mBeginRawY  = currentRawY;
		mViewVerticalSliding = false;
		mViewHorizonSliding = false;
	}
	
	private void handleActionMove(MotionEvent event) {
		float currentRawX = event.getRawX();
		float currentRawY = event.getRawY();
		
		int currentScrollX = mDrawerViewLayout.getScrollX();
		
		int beginBaseDiffX = (int)(mBeginRawX - currentRawX);
		int beginBaseDiffY = (int)(mBeginRawY - currentRawY);
		
		if ( !mViewHorizonSliding && (((beginBaseDiffY < -TOUCH_SENSING_VALUE_Y) || (TOUCH_SENSING_VALUE_Y < beginBaseDiffY))) ) {
			mViewVerticalSliding = true;
		}
		
		else if ( !mViewVerticalSliding && (((beginBaseDiffX < -TOUCH_SENSING_VALUE_X) || (TOUCH_SENSING_VALUE_X < beginBaseDiffX))) ){
			mViewHorizonSliding = true;
		}

		if ( mViewHorizonSliding ) {
			int diff = (int)(mLastRawX - currentRawX);
		
			int changedScrollX = currentScrollX + diff;
			int newDrawerStatus = -1;
			
			if ( changedScrollX >= 0 ) {
				changedScrollX = 0;
				newDrawerStatus = STATUS_CLOSE;							
			}
			
			else if ( changedScrollX <= -mDrawerMaxScrollX ) {
				changedScrollX = -mDrawerMaxScrollX;
				newDrawerStatus = STATUS_OPEN;
			}
			
			if ( newDrawerStatus != -1 && newDrawerStatus != mDrawerStatus ) {
				mDrawerStatus = newDrawerStatus;
				
				if ( mOnDrawerStatusChangeListener != null ) {
					mOnDrawerStatusChangeListener.onDrawerStatusChange(newDrawerStatus);
				}
			}
		
			mDrawerViewLayout.scrollTo(changedScrollX, 0);
			mLastRawX = currentRawX;
		}
	}
	
	private void handleActionUp(MotionEvent event) {
		float currentRawX = event.getRawX();
		
		int currentScrollX = mDrawerViewLayout.getScrollX();
		
		if ( mViewHorizonSliding ) {
			mViewHorizonSliding = false;
			if ( -mDrawerMaxScrollX < currentScrollX && currentScrollX < 0 ) {
				
				int diff = (int)(mBeginRawX - currentRawX);
				
				if ( diff > TOUCH_SENSING_VALUE_X ) {
					closeDrawerView();
				}
				
				else if ( diff < -TOUCH_SENSING_VALUE_X ){
					openDrawerView();
				}
				
				else {
					switch ( mDrawerStatus ) {
					case STATUS_CLOSE:
						closeDrawerView();
						break;
					case STATUS_OPEN:
						openDrawerView();
						break;
					}
				}
			}
		}
	}
	
	// TODO Smooth Scrolling
	private boolean mSmoothXScrolling;
	private int mMoveXEndPosition;

	private void smoothScrollX(int x) {
		mSmoothXScrolling = true;
		mMoveXEndPosition = x;
		mSmoothScrollHandler.post(mSmoothScrollRunable);
	}

	private void endSmoothScrollX() {
		if (mSmoothXScrolling) {
			mSmoothXScrolling = false;
			mSmoothScrollHandler.removeCallbacksAndMessages(null);
		}
	}
   
   private Runnable mSmoothScrollRunable = new Runnable() {
		
		@Override
		public void run() {

			float start = mDrawerViewLayout.getScrollX();
						
			if(start != mMoveXEndPosition) {
				float acceletor = (float) Math.ceil( Math.abs(mMoveXEndPosition - start) / 5 );
				if (mMoveXEndPosition < start) 
					acceletor = -acceletor;
				
				mDrawerViewLayout.scrollBy((int)acceletor, 0);
			}
			else {
				mSmoothXScrolling = false;
			}
			
			if ( mSmoothXScrolling ) {
				if ( mMoveXEndPosition == Math.floor(mDrawerViewLayout.getScrollX()) ) { 
					mDrawerViewLayout.scrollTo(mMoveXEndPosition, 0);
				}
				
				mSmoothScrollHandler.postDelayed(mSmoothScrollRunable, 10);
			}
			else {
				endSmoothScrollX();
			}
		}
	};	
	
	public static interface OnDrawerStatusChangeListener {
		public final static int STATUS_OPEN  = 0x00;
		public final static int STATUS_CLOSE = 0x01;
		
		public void onDrawerStatusChange(int status);
	}
}
