package com.kayac.slidingmenu.ui.views;


/**
Copyright 2013 - Nguyen Tien Long

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

import android.R.color;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.Scroller;
/**
 * The class which handling the slide animation, and touch-slide gesture
 * @author long-nguyen
 *
 */
public class DraggableLayout extends FrameLayout {
	
	public static interface SidingListenerInterface{
		/**
		 * Tell listener when sliding is finished
		 */
		public void onSlideFinished();
	}
	
	public static final boolean DEBUG = false;
	public static final String TAG = DraggableLayout.class.getSimpleName();
	public static final int DEFAULT_SCROLL_DURATION=150;
	public static float DEFAULT_MENU_WIDTH_PERCENT=0.75f;

	/**Menu is open from left or right? */
	private boolean mDefaultMenuSideLeft=false;
	/**Check if the menu is fully visible or not */
	private boolean mIsInNavigationMode = false;
	/**Check if user tap in menu area*/
	private boolean mIsInDragArea=false;
	/**did user make a tap action */
	private boolean mIsTap=false;
	/**full shadow when scroll**/
	private boolean mIsDimBackground=false;
	
	private boolean mIsStartedDetectIntercept=false;
	protected ScroolRunnable mScrollRunnable = new ScroolRunnable();
	protected Scroller mScroller;
	protected Handler mHandler;
	private Point screenSize;
	private Context mContext;
	private Point mDragLastPoint = new Point();
	private Point mDragCurrentPoint = new Point();
	private Point mInterceptCurrentPoint = new Point();
    private VelocityTracker vTracker = null;
	private SidingListenerInterface mSlidingListener;
	private Drawable mShadowDrawable;
	private Paint mPaint;
	private Rect mBlackShadowRect;
	
	@SuppressLint("NewApi")
	public DraggableLayout(Context context, AttributeSet attset, int defStyle) {
		super(context, attset, defStyle);
		init(context);
	}

	public DraggableLayout(Context context, AttributeSet attset) {
		super(context, attset);
		init(context);
	}

	public DraggableLayout(Context context) {
		super(context);
		init(context);
	}
	

	@TargetApi(13)
	private void init(Context context) {
		mContext = context;
		screenSize=getScreenSize(context);
		Log.d(TAG,"Screen width"+screenSize.x);
		mScroller = new Scroller(mContext, new LinearInterpolator());
		mHandler = new Handler();
		mPaint=new Paint();
		mPaint.setColor(color.background_dark);
		mBlackShadowRect=new Rect(screenSize.x, 0, 2*screenSize.x, screenSize.y);
	}
	
	public void setShadowBitmap(Bitmap shadowBitmap){
		if(shadowBitmap!=null){
			mShadowDrawable=new BitmapDrawable(mContext.getResources(),shadowBitmap);
			if(mDefaultMenuSideLeft){
				mShadowDrawable.setBounds(-shadowBitmap.getWidth(), 0, 0, screenSize.y);	
			}else{
				mShadowDrawable.setBounds(screenSize.x, 0, screenSize.x+shadowBitmap.getWidth(), screenSize.y);
			}
		}
	}
	
	/**
	 * Set the width of mainScreen when menu open
	 * @param width: size of screen in pixel when menu open
	 */
	public void setMainWindowWidthWhenMenuOpen(float width){
		if(width>0&&width<screenSize.x){
			DEFAULT_MENU_WIDTH_PERCENT=1-width/screenSize.x;
		}
	}
	
	/**
	 * Setting menu to display from left(or right side).
	 * @param isMenuLeftSide: Default is left menu
	 */
	public void setMenuSideToLeft(boolean isMenuLeftSide){
		mDefaultMenuSideLeft=isMenuLeftSide;
	}

	/**
	 * Dim background when scroll
	 */
	public void setDimBackground(boolean isDimbackground){
		mIsDimBackground=isDimbackground;
	}
	
	/**
	 * Scroll view to specific position
	 * 
	 * @param target where should scroll to
	 */
	public void smoothScroolTo(final int target) {
		mScroller.startScroll(getScrollX(), 0, target - getScrollX(), 0, DEFAULT_SCROLL_DURATION);
		mHandler.removeCallbacks(mScrollRunnable);
		mHandler.post(mScrollRunnable);
	}

	public class ScroolRunnable implements Runnable {
		@Override
		public void run() {
			mScroller.computeScrollOffset();
			scrollTo(mScroller.getCurrX(), 0);
			if (!mScroller.isFinished()) {
				mHandler.post(this);
			}else{
				mHandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						if(mSlidingListener!=null) {
							mSlidingListener.onSlideFinished();
							mSlidingListener=null;
						}
					}
				}, 100);
			}
		}
	};
	/**
	 * Detect which kind of scroll is being called, horizontally or vertically
	 * if horizontally, check children horizontally scroll ability
	 * otherwise in drag mode
	 */
	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		if(mIsInNavigationMode)
		{
			/** switch to navigation mode */
			return true;
		}else{
			switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					//the onTouchEvent should catch this event because it need to detect speed and start point either.
					onTouchEvent(event);
					if(DEBUG){
						Log.d(TAG,"intercept event Down");
					}
					mIsStartedDetectIntercept=false;
					mInterceptCurrentPoint.x=(int) event.getX();
					mInterceptCurrentPoint.y=(int) event.getY();
					break;
				case MotionEvent.ACTION_MOVE:
					if(DEBUG){
						Log.d(TAG,"intercept event move");
					}
					float diffX=Math.abs(event.getX()-mInterceptCurrentPoint.x);
					float diffY=Math.abs(event.getY()-mInterceptCurrentPoint.y);
					if(canScroll(this,false,(int)(event.getX()-mInterceptCurrentPoint.x),(int)event.getX(),(int)event.getY())){
						if(DEBUG){
							Log.d(TAG,"can scroll");
						}
						return false;
					}
					if(!mIsStartedDetectIntercept)
					{
						/**Check move intercept at this point, need to super because of the onClick Event */
						
						if(diffX>diffY&&diffY<50&&diffX>10){
							/** only in this case,leave the intercept check and leave touch for custom  onTouchEvent , 
							 *  other case we always check the intercept and give touch event for child view */
							mIsStartedDetectIntercept=true;
							return true;
						}else {
							if(diffY>20){
								mIsStartedDetectIntercept=true;	
								break;
							}
						}
					}
					mInterceptCurrentPoint.x = (int) event.getX();
					mInterceptCurrentPoint.y = (int) event.getY();
					break;
				case MotionEvent.ACTION_UP:
					if(DEBUG){
						Log.d(TAG,"onIntercept up");
					}
					break;
			}
			return false;
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) 
	{
		super.onTouchEvent(event);
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				/**Reset variable */
				if (DEBUG) {
					Log.d(TAG, "Down x " + mDragLastPoint.x);
				}
				mIsTap=true;
				mIsInDragArea=false;
				mDragCurrentPoint.x=mDragLastPoint.x = (int) event.getX();
				mDragCurrentPoint.y=mDragLastPoint.y = (int) event.getY();

				if(mIsInNavigationMode
					&&(mDefaultMenuSideLeft?(event.getX()>screenSize.x*DEFAULT_MENU_WIDTH_PERCENT)
											:event.getX()<screenSize.x*(1-DEFAULT_MENU_WIDTH_PERCENT)))
				{
					mIsInDragArea=true;
				}
				
				if (vTracker == null) {
					vTracker = VelocityTracker.obtain();
				} else {
					vTracker.clear();
				}
				vTracker.addMovement(event);
				
				break;
			case MotionEvent.ACTION_MOVE:
				vTracker.addMovement(event);
				if (Math.abs((int) event.getY() - mDragCurrentPoint.y) < 50) {
					int disX = ((int) event.getX() - mDragCurrentPoint.x);
					/** limit scroll to right or left*/
					if(mDefaultMenuSideLeft ? (getScrollX()-disX < 0) : !(getScrollX()-disX < 0) )
					{
						scrollBy(-disX, 0);
						if (DEBUG) {
							Log.d(TAG, "Scroll x " + disX);
						}
					}
				}
				if(Math.abs(mDragCurrentPoint.x-mDragLastPoint.x)>20||Math.abs(mDragCurrentPoint.y - mDragLastPoint.y)>20){
					/** Check tap event(close panel when tap) */
					mIsTap=false;
				}
				mDragCurrentPoint.x = (int) event.getX();
				mDragCurrentPoint.y = (int) event.getY();
				break;
			case MotionEvent.ACTION_UP:
				vTracker.addMovement(event);
				vTracker.computeCurrentVelocity(1000);
				/**Detect 3 end-action */
				if(Math.abs(vTracker.getXVelocity())>100){
					/**Flick */
					if(mDefaultMenuSideLeft ? (mDragCurrentPoint.x-mDragLastPoint.x>0) : (mDragCurrentPoint.x-mDragLastPoint.x<0) ){
						int direction=mDefaultMenuSideLeft?-1:1;
						smoothScroolTo((int) (direction*screenSize.x*DEFAULT_MENU_WIDTH_PERCENT));
						mIsInNavigationMode=true;
					}else{
						smoothScroolTo(0);
						mIsInNavigationMode=false;
					}
					if(DEBUG) Log.d(TAG,"Flicked");
				}
				else if(mIsTap){
					/**Tap */
					if(DEBUG) Log.d(TAG,"Tapped");
					smoothScroolTo(0);
					mIsInNavigationMode = false;
				}else{
					/**Drop*/
					requestNewLayout();
				}
				if (DEBUG) {
					Log.d(TAG, "up x " + mDragLastPoint.x);
				}
				break;
			case MotionEvent.ACTION_CANCEL:
				vTracker.recycle();
				break;
			}
		if(mIsInNavigationMode){
			return mIsInDragArea; 
		}else return true;
	}
	
	
	/**
	 * Detect at a specific position of view, its child can scrollHoziontally or not
	 * Also support Viewpager as its child
	 * @param v			view to check
	 * @param checkV	check it or not
	 * @param dx		Direction
	 * @param x			pos X
	 * @param y			pos Y
	 * @return
	 */
	protected boolean canScroll(View v, boolean checkV, int dx, int x, int y) {
		if (v instanceof ViewGroup) {
			final ViewGroup group = (ViewGroup) v;
			final int scrollX = v.getScrollX();
			final int scrollY = v.getScrollY();
			final int count = group.getChildCount();
			for (int i = count - 1; i >= 0; i--) {
				final View child = group.getChildAt(i);
				if (child.getVisibility()==View.VISIBLE
						&& x + scrollX >= child.getLeft()
						&& x + scrollX < child.getRight()
						&& y + scrollY >= child.getTop()
						&& y + scrollY < child.getBottom()
						&& canScroll(child, true, dx,
								x + scrollX - child.getLeft(), y + scrollY
										- child.getTop())) {
					return true;
				}
			}
		}
		return (checkV && (ViewCompat.canScrollHorizontally(v, -dx) ||	(v instanceof ViewPager)));
	}
	
	/**
	 * Tell the view to refresh its state, such as scroll to left or to right
	 */
	private void requestNewLayout(){
		int scroolXNow=getScrollX();
		int direction=mDefaultMenuSideLeft?-1:1;
		if(mDefaultMenuSideLeft?-scroolXNow>screenSize.x/2:scroolXNow>screenSize.x/2){
			smoothScroolTo((int) (direction*screenSize.x*DEFAULT_MENU_WIDTH_PERCENT));
			mIsInNavigationMode=true;
			return;
		}else{
			mIsInNavigationMode=false;
			smoothScroolTo(0);
		}
	}

	/**
	 * Tell the layout to auto smooth scroll
	 * @param _sidingListener
	 */
	public void invokeScrollAction(SidingListenerInterface _sidingListener){
		if (mIsInNavigationMode) {
			mSlidingListener=_sidingListener;
			smoothScroolTo(0);
		} else {
			int directionLeft=mDefaultMenuSideLeft?-1:1;
			smoothScroolTo((int) (directionLeft*screenSize.x * DEFAULT_MENU_WIDTH_PERCENT));
		}
		mIsInNavigationMode = !mIsInNavigationMode;
	}

	public boolean isOpeningSlideMenu()
	{
		return mIsInNavigationMode;
	}
	
	public static Point getScreenSize(Context c)
	{
		Point p=new Point();
		WindowManager wm = (WindowManager) c.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		p.x = display.getWidth();
		p.y = display.getHeight();
		return p;
	}
	
	
	public void onScrollChanged(int l,int  t,int oldl,int oldt){
		super.onScrollChanged(l, t, oldl, oldt);
		float black=180-255*getScrollX()/screenSize.x;
		mPaint.setAlpha((int)(black>0?black:0));
	}
	
	public void dispatchDraw(Canvas c){
		super.dispatchDraw(c);
		if(mShadowDrawable!=null){
			mShadowDrawable.draw(c);
		}
		if(mPaint!=null&&mBlackShadowRect!=null&&mIsDimBackground){
			c.drawRect(mBlackShadowRect, mPaint);
		}
	}
}
