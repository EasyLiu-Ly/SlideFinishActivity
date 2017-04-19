package com.easyliu.slidefinishrelativelayout;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.VelocityTrackerCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.RelativeLayout;
import android.widget.Scroller;

/**
 * Created by EasyLiu on 09/04/2017.
 */

public class SlideFinishRelativeLayout extends RelativeLayout {
  private static final String TAG = SlideFinishRelativeLayout.class.getSimpleName();
  private static final boolean DEBUG = false;
  private IOnSlideToFinish mOnSlideToFinish;
  private VelocityTracker mVelocityTracker;
  private int mSlideFinishVelocity;
  private int mMaximumVelocity;
  private Scroller mScroller;
  private int mTouchSlop;
  private int mDownX;
  private int mLastX;
  private int mLastY;
  private ViewGroup mParentView;
  private int mWidth;
  private boolean mIsFinishing;
  private boolean mIsOriginal;//是不是在初始位置
  private SlideMode mSlideMode;//滑动模式
  private int mSlideEdgeXMax; //
  private boolean mSlideValid;//滑动是否有效
  private boolean mIsBeingDraging;//是否正在拖动
  private int mActivePointerId = INVALID_POINTER;
  private static final int INVALID_POINTER = -1;
  private static final float TIME_FRACTION_LEFT = (float) 1.4;
  private static final float TIME_FRACTION_RIGHT = (float) 0.4;
  private static final float SLIDE_FINISH_PARTITION = (float) (1 / 3.0);
  private static final float EDGE_DOWN_X_MAX_PARTITION = (float) 1 / 10;

  public enum SlideMode {
    EDGD, //边缘
    ALL   //全部
  }

  public SlideFinishRelativeLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
    mScroller = new Scroller(context, new AccelerateDecelerateInterpolator());
    final ViewConfiguration configuration = ViewConfiguration.get(context);
    mTouchSlop = configuration.getScaledTouchSlop();
    mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
    mSlideFinishVelocity = 3000;
    mIsOriginal = true;
    mSlideMode = SlideMode.EDGD;
    mSlideEdgeXMax =
        (int) (context.getResources().getDisplayMetrics().widthPixels * EDGE_DOWN_X_MAX_PARTITION);
    mSlideValid = false;
  }

  public void setSlideMode(SlideMode slideMode) {
    mSlideMode = slideMode;
  }

  public void setOnSildeToFinishListener(IOnSlideToFinish onSlideToFinish) {
    this.mOnSlideToFinish = onSlideToFinish;
  }

  public interface IOnSlideToFinish {
    void onFinish();
  }

  @Override public void computeScroll() {
    if (mScroller.computeScrollOffset()) {
      mParentView.scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
      postInvalidate();
    } else {
      if (mOnSlideToFinish != null && mIsFinishing) {
        mOnSlideToFinish.onFinish();
      }
    }
  }

  @Override public boolean onInterceptTouchEvent(MotionEvent ev) {
    int action = ev.getAction() & MotionEventCompat.ACTION_MASK;
    if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
      // 如果这个触摸被取消了，或者手指抬起来了
      mIsBeingDraging = false;
      return false;
    }
    //正在拖动
    if (action != MotionEvent.ACTION_DOWN && mIsBeingDraging) {
      return true;
    }
    switch (action) {
      case MotionEvent.ACTION_DOWN:
        mDownX = (int) ev.getRawX();
        mLastX = mDownX;
        mLastY = (int) ev.getRawY();
        mIsBeingDraging = false;
        mActivePointerId = ev.getPointerId(0);
        break;
      case MotionEvent.ACTION_MOVE:
        int moveX = (int) ev.getRawX();
        int moveY = (int) ev.getRawY();
        int deltaX = moveX - mLastX;
        int detalY = moveY - mLastY;
        if (Math.abs(deltaX) > mTouchSlop && Math.abs(detalY) < Math.abs(deltaX) * 0.5) {
          mIsBeingDraging = true;//初始化为拦截
          requestParentDisallowInterceptTouchEvent(true);//让父View不拦截事件
          if (mSlideMode == SlideMode.EDGD) {
            if (mDownX > mSlideEdgeXMax) {
              mIsBeingDraging = false;
            }
          } else {
            if (deltaX != 0 && canScroll(this, false, (int) deltaX, (int) moveX, (int) moveY)) {
              mIsBeingDraging = false;
            }
          }
        }
        break;
    }

    if (mVelocityTracker == null) {
      mVelocityTracker = VelocityTracker.obtain();
    }
    mVelocityTracker.addMovement(ev);

    return mIsBeingDraging;
  }

  private void requestParentDisallowInterceptTouchEvent(boolean disallowIntercept) {
    final ViewParent parent = getParent();
    if (parent != null) {
      parent.requestDisallowInterceptTouchEvent(disallowIntercept);
    }
  }

  /**
   * Tests scrollability within child views of v given a delta of dx.
   *
   * @param v View to test for horizontal scrollability
   * @param checkV Whether the view v passed should itself be checked for scrollability (true),
   * or just its children (false).
   * @param dx Delta scrolled in pixels
   * @param x X coordinate of the active touch point
   * @param y Y coordinate of the active touch point
   * @return true if child views of v can be scrolled by delta of dx.
   */
  protected boolean canScroll(View v, boolean checkV, int dx, int x, int y) {
    if (v instanceof ViewGroup) {
      final ViewGroup group = (ViewGroup) v;
      final int scrollX = v.getScrollX();
      final int scrollY = v.getScrollY();
      final int count = group.getChildCount();
      // Count backwards - let topmost views consume scroll distance first.
      for (int i = count - 1; i >= 0; i--) {
        // TODO: Add versioned support here for transformed views.
        // This will not work for transformed views in Honeycomb+
        final View child = group.getChildAt(i);
        if (x + scrollX >= child.getLeft() && x + scrollX < child.getRight() && y + scrollY >= child
            .getTop() && y + scrollY < child.getBottom() && canScroll(child, true, dx,
            x + scrollX - child.getLeft(), y + scrollY - child.getTop())) {
          return true;
        }
      }
    }

    return checkV && ViewCompat.canScrollHorizontally(v, -dx);
  }

  @Override public boolean onTouchEvent(MotionEvent event) {
    if (mVelocityTracker == null) {
      mVelocityTracker = VelocityTracker.obtain();
    }
    mVelocityTracker.addMovement(event);
    switch (event.getAction()) {
      case MotionEvent.ACTION_DOWN: {
        mScroller.abortAnimation();
        mSlideValid = false;
        mIsFinishing = false;
        mIsOriginal = true;
        mLastX = mDownX = (int) event.getRawX();
        mLastY = (int) event.getRawY();
        mActivePointerId = event.getPointerId(0);
        break;
      }
      case MotionEvent.ACTION_MOVE:
        int moveX = (int) event.getRawX();
        int moveY = (int) event.getRawY();
        int deltaX = moveX - mLastX;
        int detalY = moveY - mLastY;
        mLastX = moveX;
        mLastY = moveY;
        //滑动距离
        if (Math.abs(deltaX) > mTouchSlop && Math.abs(detalY) < Math.abs(deltaX) * 0.5) {
          mSlideValid = true;
          if (mSlideMode == SlideMode.EDGD) {
            if (mDownX <= mSlideEdgeXMax) {
              mSlideValid = true;
            } else {//不是从边界开始滑动
              mSlideValid = false;
            }
          }
        }
        if (mSlideValid) {
          if (deltaX > 0) {//往右
            final VelocityTracker velocityTracker = mVelocityTracker;
            velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
            float xVelocity = VelocityTrackerCompat.getXVelocity(velocityTracker, mActivePointerId);
            if (xVelocity >= mSlideFinishVelocity) {//大于某一个速度,直接finish
              mIsFinishing = true;
              mSlideValid = false;//防止下面的ACTION_UP里面的代码再次调用
              scrollToFinish();
              break;
            }
            mParentView.scrollBy(-deltaX, 0);
            mIsOriginal = false;
          } else {
            //往左
            if (!mIsOriginal) {
              //最多到最左边
              if (mParentView.getScrollX() <= 0) {
                mParentView.scrollBy(-deltaX, 0);
              }
            }
          }
        }
        break;
      case MotionEvent.ACTION_CANCEL:
        releaseVelocityTracker();
        break;
      case MotionEvent.ACTION_UP:
        releaseVelocityTracker();
        if (mSlideValid) {
          if (mParentView.getScrollX() <= (int) (-mWidth * SLIDE_FINISH_PARTITION)) {
            mIsFinishing = true;
            scrollToFinish();
          } else {
            mIsFinishing = false;
            scrollToOriginal();
          }
        }
        break;
    }
    return true;
  }

  private void releaseVelocityTracker() {
    if (null != mVelocityTracker) {
      mVelocityTracker.clear();
      mVelocityTracker.recycle();
      mVelocityTracker = null;
    }
  }

  @Override protected void onLayout(boolean changed, int l, int t, int r, int b) {
    super.onLayout(changed, l, t, r, b);
    if (changed) {
      mParentView = (ViewGroup) this.getParent();
      mWidth = this.getWidth();
    }
  }

  private void scrollToOriginal() {
    int detalX = -mParentView.getScrollX();
    mScroller.startScroll(mParentView.getScrollX(), 0, detalX, 0,
        (int) (detalX * TIME_FRACTION_LEFT));
    postInvalidate();
    mIsOriginal = true;
  }

  private void scrollToFinish() {
    int detalX = -(mParentView.getScrollX() + mWidth);
    mScroller.startScroll(mParentView.getScrollX(), 0, detalX + 1, 0,
        (int) (-detalX * TIME_FRACTION_RIGHT));
    postInvalidate();
  }
}
